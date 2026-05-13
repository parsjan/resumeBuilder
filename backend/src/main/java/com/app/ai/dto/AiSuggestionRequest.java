package com.app.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiSuggestionRequest {

    @NotBlank
    private String section;   // e.g. "summary", "experience", "skills"

    private String currentContent;

    private String jobDescription;
}
