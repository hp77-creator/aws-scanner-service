package io.hp77creator.github.awsscannerservice.api.controller;

import io.hp77creator.github.awsscannerservice.api.dto.request.FindingResultRequest;
import io.hp77creator.github.awsscannerservice.api.dto.response.FindingResult;
import io.hp77creator.github.awsscannerservice.api.dto.response.JobStatusResponse;
import io.hp77creator.github.awsscannerservice.api.service.JobService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class JobController {
    
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
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
