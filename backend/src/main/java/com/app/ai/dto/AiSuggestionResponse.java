package com.app.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiSuggestionResponse {

    private String section;
    private String suggestedContent;
    private List<String> keywords;
    private int confidenceScore;
}
