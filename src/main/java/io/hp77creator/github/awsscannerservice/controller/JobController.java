package io.hp77creator.github.awsscannerservice.controller;

import io.hp77creator.github.awsscannerservice.models.request.FindingResultRequest;
import io.hp77creator.github.awsscannerservice.models.response.FindingResult;
import io.hp77creator.github.awsscannerservice.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JobController {
    JobService jobService;

    JobController(@Autowired JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/results")
    public ResponseEntity<FindingResult> getFindingsResult(final FindingResultRequest request){
        return ResponseEntity.ok(jobService.getResult(request));
    }
}
