package io.hp77creator.github.awsscannerservice.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan("io.hp77creator.github.awsscannerservice.common.model")
@EnableJpaRepositories("io.hp77creator.github.awsscannerservice.api.repository")
@EnableAsync
public class ScannerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScannerApiApplication.class, args);
    }
}
