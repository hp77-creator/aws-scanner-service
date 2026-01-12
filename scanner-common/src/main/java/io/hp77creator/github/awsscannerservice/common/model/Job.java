package io.hp77creator.github.awsscannerservice.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity(name = "jobs")
@Data
public class Job {
    @Id
    private UUID jobId;
    private String bucket;
    private String prefix;
    private Date createdAt;
    private Date updatedAt;
}
