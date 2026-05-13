package com.app.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Deserialises from the OpenAI Chat Completions response body.
 * Reference: https://platform.openai.com/docs/api-reference/chat/object
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiResponse {

    private String id;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Raw JSON string from the first choice's message content. */
    public String extractContent() {
        if (choices == null || choices.isEmpty()) return null;
        return choices.get(0).getMessage().getContent();
    }

    /** True only when the model finished naturally (not cut off by token limit). */
    public boolean isFinished() {
        return choices != null
                && !choices.isEmpty()
                && "stop".equals(choices.get(0).getFinishReason());
    }

    // ── Nested types ──────────────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private int index;
        private Message message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;

        @JsonProperty("completion_tokens")
        private int completionTokens;

        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}
