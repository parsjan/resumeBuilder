package com.app.ai.service;

import com.app.ai.client.OpenAiClient;
import com.app.ai.client.dto.OpenAiResponse;
import com.app.ai.dto.GenerateResumeRequest;
import com.app.ai.dto.GenerateResumeResponse;
import com.app.ai.dto.ImproveBulletRequest;
import com.app.ai.dto.ImproveBulletResponse;
import com.app.ai.dto.ImproveResumeRequest;
import com.app.ai.dto.ImproveResumeResponse;
import com.app.ai.parser.AiResponseParser;
import com.app.ai.prompt.PromptBuilder;
import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.resume.model.ResumeVersion;
import com.app.resume.repository.ResumeVersionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final OpenAiClient openAiClient;
    private final PromptBuilder promptBuilder;
    private final AiResponseParser responseParser;
    private final ResumeVersionRepository versionRepository;
    private final ObjectMapper objectMapper;

    // ── Generate Resume from Free-Text Prompt ─────────────────────────────

    @Override
    public Map<String, Object> generateResumeFromPrompt(UUID userId, String prompt) {
        log.info("AI generate resume from prompt: userId={}", userId);
        OpenAiResponse raw = openAiClient.chat(promptBuilder.buildGenerateFromPromptPrompt(prompt));
        String json = responseParser.parseRawJson(raw);
        try {
            Map<String, Object> sections = objectMapper.readValue(json, new TypeReference<>() {});
            return addIdsToSections(sections);
        } catch (Exception e) {
            log.error("Failed to parse generate-resume response", e);
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE, "AI response could not be parsed");
        }
    }

    // ── Generate Summary ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public String generateSummary(UUID userId, UUID resumeId) {
        ResumeVersion version = loadVersionForUser(userId, resumeId);
        log.info("AI generate summary: userId={}, resumeId={}", userId, resumeId);
        OpenAiResponse raw = openAiClient.chat(
                promptBuilder.buildGenerateSummaryPrompt(version.getContent()));
        try {
            String json = responseParser.parseRawJson(raw);
            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<>() {});
            Object summary = result.get("summary");
            return summary instanceof String s ? s : "";
        } catch (Exception e) {
            log.error("Failed to parse generate-summary response", e);
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE, "AI response could not be parsed");
        }
    }

    // ── Suggest Skills ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<String> suggestSkills(UUID userId, UUID resumeId) {
        ResumeVersion version = loadVersionForUser(userId, resumeId);
        log.info("AI suggest skills: userId={}, resumeId={}", userId, resumeId);
        OpenAiResponse raw = openAiClient.chat(
                promptBuilder.buildSuggestSkillsPrompt(version.getContent()));
        try {
            String json = responseParser.parseRawJson(raw);
            Map<String, Object> result = objectMapper.readValue(json, new TypeReference<>() {});
            Object skills = result.get("skills");
            if (skills instanceof List<?> list) {
                return list.stream()
                        .filter(o -> o instanceof String)
                        .map(o -> (String) o)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to parse suggest-skills response", e);
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE, "AI response could not be parsed");
        }
    }

    // ── Generate Resume ───────────────────────────────────────────────────

    @Override
    public GenerateResumeResponse generateResume(UUID userId, GenerateResumeRequest request) {
        log.info("AI generate resume: userId={}, jobTitle={}, level={}",
                userId, request.getJobTitle(), request.getExperienceLevel());

        OpenAiResponse raw = openAiClient.chat(
                promptBuilder.buildGenerateResumePrompt(request));

        return responseParser.parse(raw, GenerateResumeResponse.class);
    }

    // ── Improve Resume ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ImproveResumeResponse improveResume(UUID userId, ImproveResumeRequest request) {
        // If a resumeId is supplied, load the latest version content from the DB
        // and use it as the base — overrides whatever was sent in the request body.
        if (request.getResumeId() != null) {
            var latestVersion = versionRepository
                    .findFirstByResumeIdOrderByVersionNoDesc(request.getResumeId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND,
                            "No version found for resume: " + request.getResumeId()));

            // Guard: the resume must belong to the requesting user.
            var resume = latestVersion.getResume();
            if (!resume.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }

            request.setCurrentContent(latestVersion.getContent());
            log.debug("Loaded resume content from DB for resumeId={}, versionNo={}",
                    request.getResumeId(), latestVersion.getVersionNo());
        }

        log.info("AI improve resume: userId={}, targetRole={}",
                userId, request.getTargetJobTitle());

        OpenAiResponse raw = openAiClient.chat(
                promptBuilder.buildImproveResumePrompt(request));

        return responseParser.parse(raw, ImproveResumeResponse.class);
    }

    // ── Improve Bullet ────────────────────────────────────────────────────

    @Override
    public ImproveBulletResponse improveBullet(UUID userId, ImproveBulletRequest request) {
        log.info("AI improve bullet: userId={}, jobTitle={}", userId, request.getJobTitle());

        OpenAiResponse raw = openAiClient.chat(
                promptBuilder.buildImproveBulletPrompt(request));

        return responseParser.parse(raw, ImproveBulletResponse.class);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Load the latest resume version and verify ownership. */
    private ResumeVersion loadVersionForUser(UUID userId, UUID resumeId) {
        ResumeVersion version = versionRepository
                .findFirstByResumeIdOrderByVersionNoDesc(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND,
                        "No version found for resume: " + resumeId));
        if (!version.getResume().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return version;
    }

    /**
     * Post-process AI-generated sections to ensure every array item has an {@code id}.
     * The AI omits ids; without them, frontend remove/reorder operations break.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> addIdsToSections(Map<String, Object> sections) {
        for (String key : List.of("experience", "education", "projects", "certifications")) {
            Object val = sections.get(key);
            if (val instanceof List<?> items) {
                List<Map<String, Object>> enriched = new ArrayList<>();
                for (Object item : items) {
                    if (item instanceof Map<?, ?> m) {
                        Map<String, Object> entry = new LinkedHashMap<>((Map<String, Object>) m);
                        entry.putIfAbsent("id", generateId());
                        enriched.add(entry);
                    }
                }
                sections.put(key, enriched);
            }
        }
        return sections;
    }

    private String generateId() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5);
    }
}
