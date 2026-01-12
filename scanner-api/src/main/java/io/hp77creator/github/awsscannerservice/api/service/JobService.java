package io.hp77creator.github.awsscannerservice.api.service;

import io.hp77creator.github.awsscannerservice.api.dto.request.FindingResultRequest;
import io.hp77creator.github.awsscannerservice.api.dto.response.FindingResult;
import io.hp77creator.github.awsscannerservice.api.dto.response.JobStatusResponse;
import io.hp77creator.github.awsscannerservice.api.repository.FindingRepository;
import io.hp77creator.github.awsscannerservice.api.repository.JobObjectRepository;
import io.hp77creator.github.awsscannerservice.api.util.FindingSpecification;
import io.hp77creator.github.awsscannerservice.common.model.Finding;
import io.hp77creator.github.awsscannerservice.common.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobService {

    private final FindingRepository findingRepository;
    private final JobObjectRepository jobObjectRepository;

    public JobService(FindingRepository findingRepository, JobObjectRepository jobObjectRepository) {
        this.findingRepository = findingRepository;
        this.jobObjectRepository = jobObjectRepository;
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
        UUID uuid = UUID.fromString(jobId);
        
        // Count objects by status
        int queued = (int) jobObjectRepository.countByIdJobIdAndStatus(uuid, JobStatus.QUEUED);
        int processing = (int) jobObjectRepository.countByIdJobIdAndStatus(uuid, JobStatus.PROCESSING);
        int succeeded = (int) jobObjectRepository.countByIdJobIdAndStatus(uuid, JobStatus.SUCCEEDED);
        int failed = (int) jobObjectRepository.countByIdJobIdAndStatus(uuid, JobStatus.FAILED);
        
        return JobStatusResponse.builder()
                .queued(queued)
                .processing(processing)
                .succeeded(succeeded)
                .failed(failed)
                .build();
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
