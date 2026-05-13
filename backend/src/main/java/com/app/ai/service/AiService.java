package com.app.ai.service;

import com.app.ai.dto.GenerateResumeRequest;
import com.app.ai.dto.GenerateResumeResponse;
import com.app.ai.dto.ImproveBulletRequest;
import com.app.ai.dto.ImproveBulletResponse;
import com.app.ai.dto.ImproveResumeRequest;
import com.app.ai.dto.ImproveResumeResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AiService {

    /** Generate a resume from a free-text background description. Returns sections map. */
    Map<String, Object> generateResumeFromPrompt(UUID userId, String prompt);

    /** Generate a complete resume from structured fields (job title, level, skills). */
    GenerateResumeResponse generateResume(UUID userId, GenerateResumeRequest request);

    /** Improve an existing resume against a target job description. */
    ImproveResumeResponse improveResume(UUID userId, ImproveResumeRequest request);

    /** Generate a professional summary for the given resume. */
    String generateSummary(UUID userId, UUID resumeId);

    /** Suggest additional skills based on the given resume's content. */
    List<String> suggestSkills(UUID userId, UUID resumeId);

    /** Rewrite a single bullet point using the CAR framework. */
    ImproveBulletResponse improveBullet(UUID userId, ImproveBulletRequest request);
}
