package io.hp77creator.github.awsscannerservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScanRequest {
    
    @NotBlank(message = "Bucket name is required")
    private String bucket;
    
    private String prefix; // Optional - scan specific prefix
}
