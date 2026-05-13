package com.app.ai.controller;

import com.app.ai.dto.GenerateFromPromptRequest;
import com.app.ai.dto.GenerateResumeRequest;
import com.app.ai.dto.GenerateResumeResponse;
import com.app.ai.dto.ImproveBulletRequest;
import com.app.ai.dto.ImproveBulletResponse;
import com.app.ai.dto.ImproveResumeRequest;
import com.app.ai.dto.ImproveResumeResponse;
import com.app.ai.dto.ResumeIdRequest;
import com.app.ai.service.AiService;
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

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final JobAnalysisService jobAnalysisService;

    // POST /ai/generate-resume — free-text prompt → full sections
    @PostMapping("/generate-resume")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<?> generateResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GenerateFromPromptRequest request) {
        var sections = aiService.generateResumeFromPrompt(principal.getId(), request.getPrompt());
        return ApiResponse.success(java.util.Map.of("sections", sections));
    }

    // POST /ai/generate (structured: jobTitle, experienceLevel, skills[])
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GenerateResumeResponse> generate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GenerateResumeRequest request) {
        return ApiResponse.success(aiService.generateResume(principal.getId(), request));
    }

    // POST /ai/improve-resume — improve by resumeId
    @PostMapping("/improve-resume")
    public ApiResponse<ImproveResumeResponse> improveResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResumeIdRequest request) {
        var req = new ImproveResumeRequest();
        req.setResumeId(request.getResumeId());
        return ApiResponse.success(aiService.improveResume(principal.getId(), req));
    }

    // POST /ai/improve — improve with full request body
    @PostMapping("/improve")
    public ApiResponse<ImproveResumeResponse> improve(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ImproveResumeRequest request) {
        return ApiResponse.success(aiService.improveResume(principal.getId(), request));
    }

    // POST /ai/generate-summary
    @PostMapping("/generate-summary")
    public ApiResponse<?> generateSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResumeIdRequest request) {
        String summary = aiService.generateSummary(principal.getId(), request.getResumeId());
        return ApiResponse.success(java.util.Map.of("summary", summary));
    }

    // POST /ai/suggest-skills
    @PostMapping("/suggest-skills")
    public ApiResponse<?> suggestSkills(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResumeIdRequest request) {
        var skills = aiService.suggestSkills(principal.getId(), request.getResumeId());
        return ApiResponse.success(java.util.Map.of("skills", skills));
    }

    // POST /ai/analyze-job
    @PostMapping("/analyze-job")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JobAnalysisResponse> analyzeJob(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobAnalysisRequest request) {
        return ApiResponse.success(jobAnalysisService.analyze(principal.getId(), request));
    }

    // POST /ai/improve-bullet
    @PostMapping("/improve-bullet")
    public ApiResponse<ImproveBulletResponse> improveBullet(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ImproveBulletRequest request) {
        return ApiResponse.success(aiService.improveBullet(principal.getId(), request));
    }
}
