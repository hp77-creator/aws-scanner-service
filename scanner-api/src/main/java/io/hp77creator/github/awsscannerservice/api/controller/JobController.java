package io.hp77creator.github.awsscannerservice.api.controller;

import io.hp77creator.github.awsscannerservice.api.dto.request.FindingResultRequest;
import io.hp77creator.github.awsscannerservice.api.dto.request.ScanRequest;
import io.hp77creator.github.awsscannerservice.api.dto.response.FindingResult;
import io.hp77creator.github.awsscannerservice.api.dto.response.JobStatusResponse;
import io.hp77creator.github.awsscannerservice.api.dto.response.ScanResponse;
import io.hp77creator.github.awsscannerservice.api.service.JobService;
import io.hp77creator.github.awsscannerservice.api.service.ScanService;
import io.hp77creator.github.awsscannerservice.common.model.Job;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class JobController {
    
    private final JobService jobService;
    private final ScanService scanService;

    public JobController(JobService jobService, ScanService scanService) {
        this.jobService = jobService;
        this.scanService = scanService;
    }

    @PostMapping("/scan")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ScanResponse createScan(@Valid @RequestBody ScanRequest request) {
        Job job = scanService.createScanJob(request.getBucket(), request.getPrefix());
        scanService.enumerateAndEnqueue(job.getJobId(), request.getBucket(), request.getPrefix());
        
        return ScanResponse.builder()
                .jobId(job.getJobId())
                .bucket(job.getBucket())
                .prefix(job.getPrefix())
                .message("Scan job created successfully. Use job_id to check status.")
                .build();
    }

    @GetMapping("/results")
    public FindingResult getFindingsResult(final FindingResultRequest request){
        return jobService.getResult(request);
    }

    @GetMapping("/jobs/{jobId}")
    public JobStatusResponse getJobStatus(@PathVariable("jobId") String jobId) {
        return jobService.getJobStatus(jobId);
    }
}
