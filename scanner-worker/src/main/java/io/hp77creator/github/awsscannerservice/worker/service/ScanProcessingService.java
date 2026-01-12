package io.hp77creator.github.awsscannerservice.worker.service;

import io.hp77creator.github.awsscannerservice.common.model.Finding;
import io.hp77creator.github.awsscannerservice.common.model.JobObject;
import io.hp77creator.github.awsscannerservice.common.model.JobObjectId;
import io.hp77creator.github.awsscannerservice.common.model.JobStatus;
import io.hp77creator.github.awsscannerservice.worker.repository.FindingRepository;
import io.hp77creator.github.awsscannerservice.worker.repository.JobObjectRepository;
import io.hp77creator.github.awsscannerservice.worker.scanner.FileScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service that processes individual scan jobs.
 * Downloads files from S3, scans them, saves findings, and updates job status.
 */
@Service
public class ScanProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ScanProcessingService.class);
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB limit

    private final S3Client s3Client;
    private final FileScanner fileScanner;
    private final FindingRepository findingRepository;
    private final JobObjectRepository jobObjectRepository;

    public ScanProcessingService(S3Client s3Client, FileScanner fileScanner,
                                 FindingRepository findingRepository,
                                 JobObjectRepository jobObjectRepository) {
        this.s3Client = s3Client;
        this.fileScanner = fileScanner;
        this.findingRepository = findingRepository;
        this.jobObjectRepository = jobObjectRepository;
    }

    /**
     * Processes a single file scan job.
     * 
     * @param jobId The job UUID
     * @param bucket The S3 bucket name
     * @param key The S3 object key
     * @param etag The object's ETag (for versioning)
     */
    @Transactional
    public void processFile(UUID jobId, String bucket, String key, String etag) {
        log.info("Processing file: jobId={}, bucket={}, key={}", jobId, bucket, key);
        
        JobObjectId jobObjectId = new JobObjectId(jobId, bucket, key, etag);
        try {
            updateJobObjectStatus(jobObjectId, JobStatus.PROCESSING);
            String content = downloadFileContent(bucket, key);
            List<Finding> findings = fileScanner.scanContent(jobId, bucket, key, content);
            // Save findings (idempotency handled by DB unique constraint)
            saveFindings(findings);
            updateJobObjectStatus(jobObjectId, JobStatus.SUCCEEDED);
            
            log.info("Successfully processed file: jobId={}, bucket={}, key={}, findings={}", 
                jobId, bucket, key, findings.size());
            
        } catch (Exception e) {
            log.error("Failed to process file: jobId={}, bucket={}, key={}", 
                jobId, bucket, key, e);
            updateJobObjectStatus(jobObjectId, JobStatus.FAILED);
            throw new RuntimeException("Failed to process file: " + key, e);
        }
    }

    /**
     * Downloads file content from S3 as a string.
     * Applies size limits to prevent memory issues.
     */
    private String downloadFileContent(String bucket, String key) throws IOException {
        log.debug("Downloading from S3: bucket={}, key={}", bucket, key);
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            
            // Check file size
            Long contentLength = s3Object.response().contentLength();
            if (contentLength != null && contentLength > MAX_FILE_SIZE) {
                log.warn("File too large to scan: {} bytes (max: {})", contentLength, MAX_FILE_SIZE);
                throw new IOException("File exceeds maximum size limit: " + contentLength + " bytes");
            }
            
            // Read content as string
            byte[] bytes = s3Object.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            
            log.debug("Downloaded {} bytes from S3", bytes.length);
            return content;
            
        } catch (Exception e) {
            log.error("Error downloading from S3: bucket={}, key={}", bucket, key, e);
            throw new IOException("Failed to download file from S3", e);
        }
    }

    /**
     * Saves findings to the database.
     * Handles duplicate constraint violations gracefully (idempotency).
     */
    private void saveFindings(List<Finding> findings) {
        if (findings.isEmpty()) {
            log.debug("No findings to save");
            return;
        }

        int savedCount = 0;
        int duplicateCount = 0;

        for (Finding finding : findings) {
            try {
                findingRepository.save(finding);
                savedCount++;
            } catch (DataIntegrityViolationException e) {
                // Duplicate finding (idempotency check via unique constraint)
                log.debug("Duplicate finding skipped: bucket={}, key={}, offset={}",
                    finding.getBucket(), finding.getKey(), 
                    finding.getByteOffset());
                duplicateCount++;
            } catch (Exception e) {
                log.error("Error saving finding: {}", finding, e);
                // Continue processing other findings
            }
        }

        log.info("Saved {} findings ({} duplicates skipped)", savedCount, duplicateCount);
    }

    /**
     * Updates the job_object status in the database.
     */
    private void updateJobObjectStatus(JobObjectId jobObjectId, JobStatus status) {
        try {
            JobObject jobObject = jobObjectRepository.findById(jobObjectId)
                .orElseGet(() -> {
                    // Create if not exists (shouldn't happen in normal flow)
                    log.warn("JobObject not found, creating new one: {}", jobObjectId);
                    return JobObject.builder()
                                    .id(jobObjectId)
                                            .status(status)
                            .updatedAt(LocalDateTime.now())
                                                    .build();
                });

            jobObject.setStatus(status);
            jobObjectRepository.save(jobObject);
            log.debug("Updated job_object status to {}: {}", status, jobObjectId);
            
        } catch (Exception e) {
            log.error("Failed to update job_object status: {}", jobObjectId, e);
        }
    }
}
