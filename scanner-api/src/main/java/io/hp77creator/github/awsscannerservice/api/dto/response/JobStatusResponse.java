package io.hp77creator.github.awsscannerservice.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusResponse {
    private Integer queued;
    private Integer processing;
    private Integer succeeded;
    private Integer failed;
}
