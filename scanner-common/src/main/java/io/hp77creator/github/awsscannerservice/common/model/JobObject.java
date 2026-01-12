package io.hp77creator.github.awsscannerservice.common.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.Date;

@Entity(name = "job_objects")
@Data
public class JobObject {
    @EmbeddedId
    private JobObjectId id;
    
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    
    private String lastError;
    private Date updatedAt;
}
