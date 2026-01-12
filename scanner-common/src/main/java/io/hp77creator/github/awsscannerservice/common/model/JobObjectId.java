package io.hp77creator.github.awsscannerservice.common.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobObjectId implements Serializable {
    private UUID jobId;
    private String bucket;
    private String key;
    private String etag;
}
