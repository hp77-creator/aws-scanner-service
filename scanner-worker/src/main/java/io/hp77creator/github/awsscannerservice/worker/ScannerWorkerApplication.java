package io.hp77creator.github.awsscannerservice.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("io.hp77creator.github.awsscannerservice.common.model")
@EnableJpaRepositories("io.hp77creator.github.awsscannerservice.worker.repository")
@EnableScheduling
public class ScannerWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScannerWorkerApplication.class, args);
    }
}
