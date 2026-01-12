package io.hp77creator.github.awsscannerservice.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * AWS SDK Configuration for S3 and SQS clients
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.endpoint:#{null}}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Support for LocalStack or custom endpoints
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
            builder.forcePathStyle(true);
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // Support for LocalStack or custom endpoints
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}
