package io.hp77creator.github.awsscannerservice.models.request;

import lombok.Data;

@Data
public class FindingResultRequest {
    private String bucketId;
    private String prefix;
    private Integer limit = 10;
    private Integer page = 0;
}
