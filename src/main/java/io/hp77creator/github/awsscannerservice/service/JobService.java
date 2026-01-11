package io.hp77creator.github.awsscannerservice.service;

import io.hp77creator.github.awsscannerservice.models.db.Finding;
import io.hp77creator.github.awsscannerservice.models.request.FindingResultRequest;
import io.hp77creator.github.awsscannerservice.models.response.FindingResult;
import io.hp77creator.github.awsscannerservice.repository.FindingRepository;
import io.hp77creator.github.awsscannerservice.util.FindingSpecification;
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
                Sort.by("created_at").descending()
        );
        Page<Finding> page = findingRepository.findAll(spec, pageable);

        return mapToJobResult(page);
    }

    private FindingResult mapToJobResult(Page page) {
        return FindingResult.builder()
                .findings(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
