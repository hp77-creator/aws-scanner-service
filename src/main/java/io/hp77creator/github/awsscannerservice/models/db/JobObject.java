package io.hp77creator.github.awsscannerservice.models.db;

import io.hp77creator.github.awsscannerservice.models.JobStatus;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;

import java.util.Date;

@Entity(name = "job_objects")
@Data
public class JobObject {
    @EmbeddedId
    private JobObjectId id;
    private JobStatus status;
    private String last_error;
    private Date updated_at;
}
