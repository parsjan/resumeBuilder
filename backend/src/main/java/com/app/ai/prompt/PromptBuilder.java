package com.app.ai.prompt;

import com.app.ai.client.dto.OpenAiRequest.Message;
import com.app.ai.dto.GenerateResumeRequest;
import com.app.ai.dto.ImproveBulletRequest;
import com.app.ai.dto.ImproveResumeRequest;
import com.app.jobanalysis.dto.JobAnalysisRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Constructs structured chat prompts for each AI feature.
 *
 * Design rules:
 *  1. Every system message instructs the model to return ONLY valid JSON.
 *  2. Every user message includes the exact JSON schema expected in the response.
 *  3. Temperature is kept at 0.3 (set in {@link com.app.ai.client.OpenAiClient})
 *     to favour deterministic structure over creativity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private static final String JSON_RULE =
            "Respond ONLY with a valid JSON object. No markdown, no code blocks, no explanation text outside the JSON.";

    private final ObjectMapper objectMapper;

    // ── Generate Resume ───────────────────────────────────────────────────

    public List<Message> buildGenerateResumePrompt(GenerateResumeRequest req) {
        String system = """
                You are an expert resume writer and ATS optimization specialist.
                You write concise, impact-driven resumes tailored to specific roles.
                """ + JSON_RULE;

        String user = """
                Generate a professional resume for the following candidate profile.

                Job Title: %s
                Experience Level: %s
                Skills: %s
                Additional Context: %s

                Return a JSON object with EXACTLY this structure:
                {
                  "summary": "2–3 sentence professional summary",
                  "experience": [
                    {
                      "title": "Job Title",
                      "company": "Company Name",
                      "duration": "Jan 2022 – Present",
                      "bullets": ["Achieved X by doing Y, resulting in Z"]
                    }
                  ],
                  "education": [
                    {
                      "degree": "B.Sc. Computer Science",
                      "institution": "University Name",
                      "year": "2020"
                    }
                  ],
                  "skills": {
                    "technical": ["Java", "Spring Boot"],
                    "soft": ["Leadership", "Communication"]
                  },
                  "keywords": ["keyword1", "keyword2"]
                }
                """.formatted(
                req.getJobTitle(),
                req.getExperienceLevel().name(),
                String.join(", ", req.getSkills()),
                req.getAdditionalContext() != null ? req.getAdditionalContext() : "None"
        );

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Improve Resume ────────────────────────────────────────────────────

    public List<Message> buildImproveResumePrompt(ImproveResumeRequest req) {
        String system = """
                You are a professional resume consultant specialising in ATS optimisation and
                executive-level career coaching. You rewrite resumes to maximise keyword match
                and demonstrate quantifiable impact.
                """ + JSON_RULE;

        String contentJson = safeSerialize(req.getCurrentContent());

        String user = """
                Improve the following resume to better match the target role.

                Target Job Title: %s
                Target Job Description: %s

                Current Resume Content (JSON):
                %s

                Return a JSON object with EXACTLY this structure:
                {
                  "improvedContent": { <same structure as the input content, preserving all keys> },
                  "changes": [
                    "Changed X to Y because …",
                    "Added keyword Z to match job description"
                  ],
                  "atsScore": 85,
                  "keywords": ["keyword1", "keyword2"]
                }

                Rules:
                - Keep the same JSON keys as the input.
                - atsScore is 0–100 estimating ATS match for the target role.
                - List at least 3 specific changes made.
                """.formatted(
                req.getTargetJobTitle() != null ? req.getTargetJobTitle() : "Not specified",
                req.getTargetJobDescription() != null ? req.getTargetJobDescription() : "Not specified",
                contentJson
        );

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Improve Bullet ────────────────────────────────────────────────────

    public List<Message> buildImproveBulletPrompt(ImproveBulletRequest req) {
        String system = """
                You are an expert at writing impactful, quantified resume bullet points
                using the CAR framework (Challenge → Action → Result).
                Strong bullets start with an active verb, contain a metric, and show business impact.
                """ + JSON_RULE;

        String user = """
                Improve the following resume bullet point for a %s position.

                Original bullet: "%s"
                Additional context: %s

                Return a JSON object with EXACTLY this structure:
                {
                  "improved": "Redesigned checkout flow using React, reducing cart abandonment by 23%%",
                  "alternatives": [
                    "Alternative version 1",
                    "Alternative version 2",
                    "Alternative version 3"
                  ],
                  "explanation": "Why these changes make the bullet stronger"
                }

                Rules:
                - Start every bullet with a strong past-tense action verb.
                - Include at least one quantified metric where possible.
                - Keep bullets under 20 words.
                - Provide exactly 3 alternatives.
                """.formatted(
                req.getJobTitle(),
                req.getBulletText(),
                req.getContext() != null ? req.getContext() : "None"
        );

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Analyse Job ───────────────────────────────────────────────────────

    public List<Message> buildJobAnalysisPrompt(JobAnalysisRequest req, Map<String, Object> resumeContent) {
        String system = """
                You are an expert ATS (Applicant Tracking System) analyst and career coach.
                You compare job descriptions against resumes to score fit and surface gaps.
                """ + JSON_RULE;

        String resumeSection = resumeContent != null
                ? "Resume Content (JSON):\n" + safeSerialize(resumeContent)
                : "No resume provided — analyse the job description only.";

        String user = """
                Analyse how well the following resume matches the job description.

                Job Title: %s
                Company: %s
                Job Description:
                %s

                %s

                Return a JSON object with EXACTLY this structure:
                {
                  "matchScore": 78,
                  "keywords": ["Java", "Spring Boot", "Microservices"],
                  "missingKeywords": ["Kubernetes", "CI/CD", "AWS"],
                  "tips": [
                    "Add quantified impact to experience bullets",
                    "Mention Kubernetes experience if applicable",
                    "Highlight leadership or mentoring examples"
                  ]
                }

                Rules:
                - matchScore is 0–100; use 0 when no resume was provided.
                - keywords are important terms from the job description present in the resume.
                - missingKeywords are important terms from the job description absent from the resume.
                - Provide 3–5 actionable tips.
                """.formatted(
                req.getJobTitle() != null ? req.getJobTitle() : "Not specified",
                req.getCompanyName() != null ? req.getCompanyName() : "Not specified",
                req.getJobDescription(),
                resumeSection
        );

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Generate Resume from Free-Text Prompt ─────────────────────────────

    public List<Message> buildGenerateFromPromptPrompt(String prompt) {
        String system = """
                You are an expert resume writer and ATS optimisation specialist.
                You convert free-text career descriptions into structured, professional resumes.
                """ + JSON_RULE;

        String user = """
                Generate a complete, professional resume from the following background description.

                Background:
                %s

                Return a JSON object with EXACTLY this structure (omit sections that have no data):
                {
                  "personalInfo": {
                    "fullName": "...", "email": "", "phone": "", "location": "", "linkedIn": "", "website": ""
                  },
                  "summary": "2–3 sentence professional summary",
                  "experience": [
                    {
                      "company": "Company Name", "role": "Job Title",
                      "startDate": "Jan 2022", "endDate": "Mar 2024", "current": false,
                      "bullets": ["Achieved X by doing Y, resulting in Z"]
                    }
                  ],
                  "education": [
                    {
                      "institution": "University Name", "degree": "B.Sc.", "field": "Computer Science",
                      "startDate": "Sep 2016", "endDate": "Jun 2020"
                    }
                  ],
                  "skills": ["Java", "Spring Boot", "React"],
                  "projects": [
                    {
                      "name": "Project Name", "description": "Short description",
                      "bullets": ["Built X using Y"], "technologies": ["React", "Node.js"]
                    }
                  ],
                  "certifications": [
                    { "name": "AWS Certified Solutions Architect", "issuer": "Amazon", "date": "2023" }
                  ]
                }

                Rules:
                - Extract all details from the description; infer reasonable values where gaps exist.
                - Write experience bullets using the CAR framework (Challenge → Action → Result).
                - Keep bullets under 20 words each; start each with a strong past-tense action verb.
                - Extract skills from the experience descriptions in addition to any explicitly listed.
                - For current: true when the candidate is currently in the role.
                - Leave email/phone/linkedIn/website empty unless explicitly provided.
                """.formatted(prompt);

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Generate Summary ──────────────────────────────────────────────────

    public List<Message> buildGenerateSummaryPrompt(Map<String, Object> sections) {
        String system = """
                You are a professional resume summary writer.
                You write concise, impactful professional summaries tailored to the candidate's experience.
                """ + JSON_RULE;

        String user = """
                Write a professional summary for the following resume content.

                Resume Content (JSON):
                %s

                Return a JSON object with EXACTLY this structure:
                {
                  "summary": "2–3 sentence professional summary highlighting key experience, skills, and value proposition"
                }

                Rules:
                - Start with the candidate's primary role or expertise.
                - Mention years of experience if derivable from dates.
                - Highlight 2–3 top technical skills or achievements.
                - Keep it under 60 words.
                """.formatted(safeSerialize(sections));

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Suggest Skills ────────────────────────────────────────────────────

    public List<Message> buildSuggestSkillsPrompt(Map<String, Object> sections) {
        String system = """
                You are a career advisor and technical skills expert.
                You identify missing but relevant skills based on a candidate's experience.
                """ + JSON_RULE;

        String user = """
                Based on the following resume content, suggest additional relevant skills the candidate likely has
                or should add to strengthen their resume.

                Resume Content (JSON):
                %s

                Return a JSON object with EXACTLY this structure:
                {
                  "skills": ["Skill1", "Skill2", "Skill3"]
                }

                Rules:
                - Suggest 5–10 skills not already listed in the resume.
                - Prioritise skills that appear in the experience/projects but are missing from the skills list.
                - Include both technical tools and relevant soft skills.
                - Return only the skill names as strings.
                """.formatted(safeSerialize(sections));

        return List.of(Message.system(system), Message.user(user));
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private String safeSerialize(Object obj) {
        if (obj == null) return "{}";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize content for prompt", e);
            return "{}";
        }
    }
}
