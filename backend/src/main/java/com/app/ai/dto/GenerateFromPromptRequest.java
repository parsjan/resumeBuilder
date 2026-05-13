package com.app.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateFromPromptRequest {

    /** Free-text description of the candidate's background, experience, and goals. */
    @NotBlank(message = "Prompt is required")
    private String prompt;
}
