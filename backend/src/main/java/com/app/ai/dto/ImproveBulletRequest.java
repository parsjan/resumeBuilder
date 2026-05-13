package com.app.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImproveBulletRequest {

    @NotBlank(message = "Bullet text is required")
    private String bulletText;

    @NotBlank(message = "Job title is required for context")
    private String jobTitle;

    /** Optional extra context: tech stack, team size, industry, etc. */
    private String context;
}
