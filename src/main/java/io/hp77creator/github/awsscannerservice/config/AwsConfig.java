package io.hp77creator.github.awsscannerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {
    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint}")
    private String endpoint;

}
