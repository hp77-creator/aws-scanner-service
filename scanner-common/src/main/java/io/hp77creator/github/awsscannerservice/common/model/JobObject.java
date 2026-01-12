package io.hp77creator.github.awsscannerservice.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_objects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobObject {
    @EmbeddedId
    private JobObjectId id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;
    
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "queued_at", nullable = false)
    private LocalDateTime queuedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public UUID getJobId() {
        return id != null ? id.getJobId() : null;
    }
    
    public void setJobId(UUID jobId) {
        if (id == null) {
            id = new JobObjectId();
        }
        id.setJobId(jobId);
    }
    
    public String getBucket() {
        return id != null ? id.getBucket() : null;
    }
    
    public void setBucket(String bucket) {
        if (id == null) {
            id = new JobObjectId();
        }
        id.setBucket(bucket);
    }
    
    public String getKey() {
        return id != null ? id.getKey() : null;
    }
    
    public void setKey(String key) {
        if (id == null) {
            id = new JobObjectId();
        }
        id.setKey(key);
    }
    
    public String getEtag() {
        return id != null ? id.getEtag() : null;
    }
    
    public void setEtag(String etag) {
        if (id == null) {
            id = new JobObjectId();
        }
        id.setEtag(etag);
    }
    
    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (queuedAt == null) {
            queuedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
