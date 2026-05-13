package com.app.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateResumeRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel experienceLevel;

    @NotEmpty(message = "At least one skill is required")
    private List<String> skills;

    /** Optional free-text context: industry, company type, extra requirements, etc. */
    private String additionalContext;

    public enum ExperienceLevel {
        ENTRY, MID, SENIOR, LEAD, EXECUTIVE
    }
}
