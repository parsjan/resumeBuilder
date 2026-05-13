package com.app.jobanalysis.controller;

import com.app.common.ApiResponse;
import com.app.jobanalysis.dto.JobAnalysisRequest;
import com.app.jobanalysis.dto.JobAnalysisResponse;
import com.app.jobanalysis.service.JobAnalysisService;
import com.app.user.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/job-analyses")
@RequiredArgsConstructor
public class JobAnalysisController {

    private final JobAnalysisService jobAnalysisService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JobAnalysisResponse> analyze(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobAnalysisRequest request) {
        return ApiResponse.success(jobAnalysisService.analyze(principal.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<JobAnalysisResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(jobAnalysisService.listByUser(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobAnalysisResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.success(jobAnalysisService.getById(principal.getId(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        jobAnalysisService.delete(principal.getId(), id);
    }
}
