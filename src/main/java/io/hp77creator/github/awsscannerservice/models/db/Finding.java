package io.hp77creator.github.awsscannerservice.models.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class Finding {
    @Id
    private Long id;
    private UUID job_id;
    private String bucket;
    private String key;
    private String detector;
    private String masked_match;
    private String context;
    private int byte_offset;
    private Date created_at;
}
