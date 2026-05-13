package com.app.jobanalysis.service;

import com.app.ai.client.OpenAiClient;
import com.app.ai.parser.AiResponseParser;
import com.app.ai.prompt.PromptBuilder;
import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.jobanalysis.dto.JobAnalysisAiResponse;
import com.app.jobanalysis.dto.JobAnalysisRequest;
import com.app.jobanalysis.dto.JobAnalysisResponse;
import com.app.jobanalysis.model.JobAnalysis;
import com.app.jobanalysis.repository.JobAnalysisRepository;
import com.app.resume.model.Resume;
import com.app.resume.repository.ResumeRepository;
import com.app.resume.repository.ResumeVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobAnalysisServiceImpl implements JobAnalysisService {

    private final JobAnalysisRepository repository;
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;
    private final OpenAiClient openAiClient;
    private final PromptBuilder promptBuilder;
    private final AiResponseParser responseParser;

    // ── Analyse ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public JobAnalysisResponse analyze(UUID userId, JobAnalysisRequest request) {
        if (request.getResumeId() == null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "resumeId is required for job analysis");
        }

        Resume resume = resumeRepository
                .findByIdAndUserIdAndIsDeletedFalse(request.getResumeId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        Map<String, Object> resumeContent = versionRepository
                .findFirstByResumeIdOrderByVersionNoDesc(request.getResumeId())
                .map(v -> v.getContent())
                .orElse(null);

        log.info("Job analysis: userId={}, resumeId={}, jobTitle={}",
                userId, request.getResumeId(), request.getJobTitle());

        var prompt = promptBuilder.buildJobAnalysisPrompt(request, resumeContent);
        var raw = openAiClient.chat(prompt);
        var aiResult = responseParser.parse(raw, JobAnalysisAiResponse.class);

        // Store metadata and AI result together in the JSONB suggestions column
        Map<String, Object> suggestions = new LinkedHashMap<>();
        suggestions.put("jobTitle", request.getJobTitle());
        suggestions.put("companyName", request.getCompanyName());
        suggestions.put("keywords", aiResult.getKeywords() != null ? aiResult.getKeywords() : List.of());
        suggestions.put("missingKeywords", aiResult.getMissingKeywords() != null ? aiResult.getMissingKeywords() : List.of());
        suggestions.put("tips", aiResult.getTips() != null ? aiResult.getTips() : List.of());

        JobAnalysis analysis = JobAnalysis.builder()
                .resume(resume)
                .jobDescription(request.getJobDescription())
                .matchScore(aiResult.getMatchScore())
                .suggestions(suggestions)
                .build();

        analysis = repository.save(analysis);
        return toResponse(analysis);
    }

    // ── List ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<JobAnalysisResponse> listByUser(UUID userId) {
        return repository.findByResumeUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Get by ID ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public JobAnalysisResponse getById(UUID userId, UUID analysisId) {
        return repository.findByIdAndResumeUserId(analysisId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_ANALYSIS_NOT_FOUND));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID userId, UUID analysisId) {
        JobAnalysis analysis = repository.findByIdAndResumeUserId(analysisId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_ANALYSIS_NOT_FOUND));
        repository.delete(analysis);
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private JobAnalysisResponse toResponse(JobAnalysis a) {
        Map<String, Object> s = a.getSuggestions() != null ? a.getSuggestions() : Map.of();

        List<String> keywords = s.get("keywords") instanceof List<?> raw
                ? (List<String>) raw
                : List.of();

        return JobAnalysisResponse.builder()
                .id(a.getId())
                .jobTitle(s.get("jobTitle") instanceof String t ? t : null)
                .companyName(s.get("companyName") instanceof String c ? c : null)
                .keywords(keywords)
                .matchScore(a.getMatchScore())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
