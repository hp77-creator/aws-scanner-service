package io.hp77creator.github.awsscannerservice.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity(name = "findings")
@Data
public class Finding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private UUID jobId;
    private String bucket;
    private String key;
    private String detector;
    private String maskedMatch;
    private String context;
    private Integer byteOffset;
    private Date createdAt;
}
