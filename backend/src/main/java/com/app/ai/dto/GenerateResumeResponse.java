package com.app.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Structured resume content returned by the AI generate feature.
 * Maps directly from the JSON the model produces.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateResumeResponse {

    private String summary;
    private List<ExperienceEntry> experience;
    private List<EducationEntry> education;
    private Skills skills;
    private List<String> keywords;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExperienceEntry {
        private String title;
        private String company;
        private String duration;
        private List<String> bullets;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EducationEntry {
        private String degree;
        private String institution;
        private String year;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Skills {
        private List<String> technical;
        private List<String> soft;
    }

    /** Converts this response to a flat {@code Map<String, Object>} for JSONB storage. */
    public Map<String, Object> toContentMap() {
        return Map.of(
                "summary", summary != null ? summary : "",
                "experience", experience != null ? experience : List.of(),
                "education", education != null ? education : List.of(),
                "skills", skills != null ? skills : Map.of(),
                "keywords", keywords != null ? keywords : List.of()
        );
    }
}
