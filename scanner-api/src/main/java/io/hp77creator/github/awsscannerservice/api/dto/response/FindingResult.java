package io.hp77creator.github.awsscannerservice.api.dto.response;

import io.hp77creator.github.awsscannerservice.common.model.Finding;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FindingResult {
    private List<Finding> findings;
    private long totalElements;
    private long totalPages;
    private int currentPage;
    private int pageSize;
}
