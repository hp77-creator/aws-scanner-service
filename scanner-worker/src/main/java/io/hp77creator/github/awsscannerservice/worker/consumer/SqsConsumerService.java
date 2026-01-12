package io.hp77creator.github.awsscannerservice.worker.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hp77creator.github.awsscannerservice.worker.service.ScanProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SQS Consumer Service that polls messages from the queue and processes scan jobs.
 * Uses long polling (20 second wait time) and batch processing (up to 10 messages).
 */
@Service
public class SqsConsumerService {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    private static final int MAX_MESSAGES = 10; // Batch size
    private static final int WAIT_TIME_SECONDS = 20; // Long polling
    private static final int VISIBILITY_TIMEOUT = 300; // 5 minutes

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    private final SqsClient sqsClient;
    private final ScanProcessingService scanProcessingService;
    private final ObjectMapper objectMapper;

    public SqsConsumerService(SqsClient sqsClient, 
                             ScanProcessingService scanProcessingService,
                             ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.scanProcessingService = scanProcessingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Polls SQS queue for messages and processes them.
     * Runs every 5 seconds (but long polling means it waits up to 20s for messages).
     */
    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        if (queueUrl == null || queueUrl.isEmpty()) {
            log.warn("SQS queue URL not configured, skipping poll");
            return;
        }

        try {
            log.debug("Polling SQS queue: {}", queueUrl);
            
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(MAX_MESSAGES)
                .waitTimeSeconds(WAIT_TIME_SECONDS)
                .visibilityTimeout(VISIBILITY_TIMEOUT)
                .build();

            ReceiveMessageResponse receiveResponse = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = receiveResponse.messages();

            if (messages.isEmpty()) {
                log.debug("No messages received from queue");
                return;
            }

            log.info("Received {} messages from SQS", messages.size());

            for (Message message : messages) {
                processMessage(message);
            }

        } catch (Exception e) {
            log.error("Error polling SQS queue", e);
        }
    }

    /**
     * Processes a single SQS message.
     */
    private void processMessage(Message message) {
        String receiptHandle = message.receiptHandle();
        
        try {
            log.debug("Processing message: {}", message.messageId());
            
            ScanMessage scanMessage = parseMessage(message.body());
            
            scanProcessingService.processFile(
                scanMessage.jobId(),
                scanMessage.bucket(),
                scanMessage.key(),
                scanMessage.etag()
            );
            deleteMessage(receiptHandle);
            log.info("Successfully processed and deleted message: {}", message.messageId());
            
        } catch (Exception e) {
            log.error("Failed to process message: {}. Error: {}", 
                message.messageId(), e.getMessage(), e);
            
            changeMessageVisibility(receiptHandle, 60); // Retry after 1 minute
        }
    }

    /**
     * Parses the SQS message body into a ScanMessage record.
     * Expected format: {"job_id": "uuid", "bucket": "name", "key": "path", "etag": "value"}
     */
    private ScanMessage parseMessage(String messageBody) throws Exception {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> messageMap = objectMapper.readValue(messageBody, Map.class);
            
            String jobIdStr = messageMap.get("job_id");
            String bucket = messageMap.get("bucket");
            String key = messageMap.get("key");
            String etag = messageMap.get("etag");
            
            if (jobIdStr == null || bucket == null || key == null) {
                throw new IllegalArgumentException("Missing required fields in message: " + messageBody);
            }
            
            UUID jobId = UUID.fromString(jobIdStr);
            
            return new ScanMessage(jobId, bucket, key, etag);
            
        } catch (Exception e) {
            log.error("Failed to parse message body: {}", messageBody, e);
            throw new Exception("Invalid message format", e);
        }
    }

    /**
     * Deletes a message from the queue after successful processing.
     */
    private void deleteMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
            
            sqsClient.deleteMessage(deleteRequest);
            log.debug("Deleted message from queue");
            
        } catch (Exception e) {
            log.error("Failed to delete message from queue", e);
        }
    }

    /**
     * Changes the visibility timeout for a message (used for retry backoff).
     */
    private void changeMessageVisibility(String receiptHandle, int visibilityTimeoutSeconds) {
        try {
            ChangeMessageVisibilityRequest changeRequest = ChangeMessageVisibilityRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .visibilityTimeout(visibilityTimeoutSeconds)
                .build();
            
            sqsClient.changeMessageVisibility(changeRequest);
            log.debug("Changed message visibility to {} seconds", visibilityTimeoutSeconds);
            
        } catch (Exception e) {
            log.error("Failed to change message visibility", e);
        }
    }

    /**
     * Record representing a scan message from SQS.
     */
    private record ScanMessage(UUID jobId, String bucket, String key, String etag) {}
}
