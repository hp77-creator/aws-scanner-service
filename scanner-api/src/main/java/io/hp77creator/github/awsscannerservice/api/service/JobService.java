package io.hp77creator.github.awsscannerservice.api.service;

import io.hp77creator.github.awsscannerservice.api.dto.request.FindingResultRequest;
import io.hp77creator.github.awsscannerservice.api.dto.response.FindingResult;
import io.hp77creator.github.awsscannerservice.api.dto.response.JobStatusResponse;
import io.hp77creator.github.awsscannerservice.api.repository.FindingRepository;
import io.hp77creator.github.awsscannerservice.api.util.FindingSpecification;
import io.hp77creator.github.awsscannerservice.common.model.Finding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    private final FindingRepository findingRepository;

    public JobService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public FindingResult getResult(final FindingResultRequest request){
        Specification<Finding> spec = Specification.where(
                FindingSpecification.hasBucket(request.getBucketId())
        ).and(
                FindingSpecification.hasKeyStartingWith(request.getPrefix())
        );
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getLimit(),
                Sort.by("createdAt").descending()
        );
        Page<Finding> page = findingRepository.findAll(spec, pageable);

        return mapToJobResult(page);
    }

    public JobStatusResponse getJobStatus(String jobId) {
        // TODO: Implement job status retrieval
        return new JobStatusResponse();
    }

    private FindingResult mapToJobResult(Page<Finding> page) {
        return FindingResult.builder()
                .findings(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
