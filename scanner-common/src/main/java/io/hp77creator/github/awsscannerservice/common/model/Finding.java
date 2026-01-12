package io.hp77creator.github.awsscannerservice.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "findings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Finding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_id", nullable = false)
    private UUID jobId;
    
    @Column(nullable = false)
    private String bucket;
    
    @Column(nullable = false)
    private String key;
    
    @Column(name = "detector_type", nullable = false)
    private String detectorType;
    
    @Column(name = "masked_match", nullable = false)
    private String maskedMatch;
    
    @Column(columnDefinition = "TEXT")
    private String context;
    
    @Column(name = "byte_offset", nullable = false)
    private Long byteOffset;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
