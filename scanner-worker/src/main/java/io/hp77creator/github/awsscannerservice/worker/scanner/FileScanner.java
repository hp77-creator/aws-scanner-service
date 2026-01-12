package io.hp77creator.github.awsscannerservice.worker.scanner;

import io.hp77creator.github.awsscannerservice.common.model.Finding;
import io.hp77creator.github.awsscannerservice.worker.scanner.detector.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Scans file content using all registered detectors and creates Finding entities.
 * Handles context extraction and data masking for each match.
 */
@Component
public class FileScanner {

    private static final Logger log = LoggerFactory.getLogger(FileScanner.class);
    private static final int CONTEXT_WINDOW = 50; // Characters before and after match

    private final List<Detector> detectors;

    public FileScanner(List<Detector> detectors) {
        this.detectors = detectors;
        log.info("Initialized FileScanner with {} detectors: {}", 
            detectors.size(), 
            detectors.stream().map(Detector::getDetectorName).toList());
    }

    /**
     * Scans the provided content and returns all findings.
     * 
     * @param jobId The job ID this scan belongs to
     * @param bucket The S3 bucket name
     * @param key The S3 object key
     * @param content The file content to scan
     * @return List of Finding entities
     */
    public List<Finding> scanContent(UUID jobId, String bucket, String key, String content) {
        List<Finding> findings = new ArrayList<>();

        if (content == null || content.isEmpty()) {
            log.debug("Empty content for {}/{}", bucket, key);
            return findings;
        }

        for (Detector detector : detectors) {
            try {
                List<Detector.DetectorMatch> matches = detector.scan(content);
                
                for (Detector.DetectorMatch match : matches) {
                    Finding finding = createFinding(
                        jobId, bucket, key, 
                        detector.getDetectorName(),
                        match, content
                    );
                    findings.add(finding);
                }
                
                if (!matches.isEmpty()) {
                    log.info("Detector {} found {} matches in {}/{}", 
                        detector.getDetectorName(), matches.size(), bucket, key);
                }
            } catch (Exception e) {
                log.error("Error running detector {} on {}/{}: {}", 
                    detector.getDetectorName(), bucket, key, e.getMessage(), e);
            }
        }

        return findings;
    }

    /**
     * Creates a Finding entity with masked value and context.
     */
    private Finding createFinding(UUID jobId, String bucket, String key, 
                                  String detectorType, Detector.DetectorMatch match,
                                  String fullContent) {
        
        String maskedValue = maskSensitiveData(match.matchedValue());
        String context = extractContext(fullContent, match.startOffset(), match.endOffset());

        return Finding.builder()
                .jobId(jobId)
                .bucket(bucket)
                .key(key)
                .detectorType(detectorType)
                .context(context)
                .maskedMatch(maskedValue)
                .byteOffset((long) match.startOffset())
                .build();
    }

    /**
     * Masks sensitive data, showing only first and last 4 characters.
     * For shorter values, shows proportionally less.
     */
    private String maskSensitiveData(String value) {
        if (value == null || value.length() <= 8) {
            // For very short values, mask everything except first and last char
            if (value != null && value.length() > 2) {
                return value.charAt(0) + "***" + value.charAt(value.length() - 1);
            }
            return "****";
        }

        String first4 = value.substring(0, 4);
        String last4 = value.substring(value.length() - 4);
        int maskedLength = value.length() - 8;
        String masking = "*".repeat(Math.min(maskedLength, 10));
        
        return first4 + masking + last4;
    }

    /**
     * Extracts context around the match (50 chars before and after).
     */
    private String extractContext(String content, int startOffset, int endOffset) {
        int contextStart = Math.max(0, startOffset - CONTEXT_WINDOW);
        int contextEnd = Math.min(content.length(), endOffset + CONTEXT_WINDOW);
        
        String context = content.substring(contextStart, contextEnd);
        
        // Add ellipsis if we truncated
        if (contextStart > 0) {
            context = "..." + context;
        }
        if (contextEnd < content.length()) {
            context = context + "...";
        }
        
        // Limit total context length to prevent DB issues
        if (context.length() > 255) {
            context = context.substring(0, 252) + "...";
        }
        
        return context;
    }
}
