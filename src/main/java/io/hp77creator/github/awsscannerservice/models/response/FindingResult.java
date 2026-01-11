package io.hp77creator.github.awsscannerservice.models.response;

import io.hp77creator.github.awsscannerservice.models.db.Finding;
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
