package io.hp77creator.github.awsscannerservice.models.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity(name = "jobs")
@Data
public class Job {
    @Id
    private UUID id;
    private String bucket;
    private String prefix;
    private Date created_at;
    private Date updated_at;
}
