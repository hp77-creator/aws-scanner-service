package io.hp77creator.github.awsscannerservice.models.response;

import io.hp77creator.github.awsscannerservice.models.JobStatus;
import lombok.Data;

@Data
public class JobStatusResponse {
    private JobStatus status;
    private Integer queued;
    private Integer running;
    private Integer completed;
}
