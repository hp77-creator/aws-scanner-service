package io.hp77creator.github.awsscannerservice.worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * AWS SDK configuration for the Scanner Worker.
 * Configures S3 and SQS clients with support for LocalStack endpoints.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.endpoint:}")
    private String awsEndpoint;

    /**
     * Creates an S3 client bean for downloading files to scan.
     * Supports both AWS and LocalStack endpoints.
     */
    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // For LocalStack: set custom endpoint
        if (awsEndpoint != null && !awsEndpoint.isEmpty()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }

    /**
     * Creates an SQS client bean for polling messages from the queue.
     * Supports both AWS and LocalStack endpoints.
     */
    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // For LocalStack: set custom endpoint
        if (awsEndpoint != null && !awsEndpoint.isEmpty()) {
            builder.endpointOverride(URI.create(awsEndpoint));
        }

        return builder.build();
    }
}
