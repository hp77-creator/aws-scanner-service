package io.hp77creator.github.awsscannerservice.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hp77creator.github.awsscannerservice.api.repository.JobObjectRepository;
import io.hp77creator.github.awsscannerservice.api.repository.JobRepository;
import io.hp77creator.github.awsscannerservice.common.model.Job;
import io.hp77creator.github.awsscannerservice.common.model.JobObject;
import io.hp77creator.github.awsscannerservice.common.model.JobObjectId;
import io.hp77creator.github.awsscannerservice.common.model.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service to handle S3 scanning jobs - enumerates objects and enqueues to SQS
 */
@Service
@Slf4j
public class ScanService {
    
    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final JobRepository jobRepository;
    private final JobObjectRepository jobObjectRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;
    
    public ScanService(S3Client s3Client, 
                      SqsClient sqsClient, 
                      JobRepository jobRepository,
                      JobObjectRepository jobObjectRepository,
                      ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
        this.jobRepository = jobRepository;
        this.jobObjectRepository = jobObjectRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Create a scan job and return immediately
     */
    public Job createScanJob(String bucket, String prefix) {
        Job job = new Job();
        job.setJobId(UUID.randomUUID());
        job.setBucket(bucket);
        job.setPrefix(prefix);
        job.setCreatedAt(new Date());
        job.setUpdatedAt(new Date());
        
        return jobRepository.save(job);
    }
    
    /**
     * Asynchronously enumerate S3 objects and enqueue to SQS
     * This runs in a separate thread pool
     */
    @Async
    public void enumerateAndEnqueue(UUID jobId, String bucket, String prefix) {
        log.info("Starting S3 enumeration for job: {}, bucket: {}, prefix: {}", 
                jobId, bucket, prefix != null ? prefix : "<root>");
        
        int objectCount = 0;
        int errorCount = 0;
        
        try {
            var requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .maxKeys(1000); // Process in batches of 1000
            
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }
            
            var paginator = s3Client.listObjectsV2Paginator(requestBuilder.build());
            
            for (var response : paginator) {
                for (S3Object s3Object : response.contents()) {
                    try {
                        // Skip directories (objects ending with /)
                        if (s3Object.key().endsWith("/")) {
                            continue;
                        }
                        
                        // Create job_object record
                        JobObjectId jobObjectId = new JobObjectId();
                        jobObjectId.setJobId(jobId);
                        jobObjectId.setBucket(bucket);
                        jobObjectId.setKey(s3Object.key());
                        jobObjectId.setEtag(s3Object.eTag());
                        
                        JobObject jobObject = new JobObject();
                        jobObject.setId(jobObjectId);
                        jobObject.setStatus(JobStatus.QUEUED);
                        jobObject.setUpdatedAt(new Date());
                        
                        jobObjectRepository.save(jobObject);
                        
                        // Send SQS message
                        String messageBody = createSqsMessage(jobId, bucket, s3Object.key(), s3Object.eTag());
                        sqsClient.sendMessage(SendMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .messageBody(messageBody)
                                .build());
                        
                        objectCount++;
                        
                        if (objectCount % 100 == 0) {
                            log.info("Enqueued {} objects for job {}", objectCount, jobId);
                        }
                        
                    } catch (Exception e) {
                        log.error("Error processing object: {} for job: {}", s3Object.key(), jobId, e);
                        errorCount++;
                    }
                }
            }
            
            log.info("Completed S3 enumeration for job: {}. Total objects: {}, Errors: {}", 
                    jobId, objectCount, errorCount);
            
        } catch (Exception e) {
            log.error("Fatal error during S3 enumeration for job: {}", jobId, e);
        }
    }
    
    /**
     * Create SQS message body with job information
     */
    private String createSqsMessage(UUID jobId, String bucket, String key, String etag) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("job_id", jobId.toString());
            message.put("bucket", bucket);
            message.put("key", key);
            message.put("etag", etag);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create SQS message", e);
        }
    }
}
