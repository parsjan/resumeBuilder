package com.app.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Serialises to the OpenAI Chat Completions request body.
 * Reference: https://platform.openai.com/docs/api-reference/chat/create
 */
@Data
@Builder
public class OpenAiRequest {

    private String model;
    private List<Message> messages;

    @JsonProperty("max_tokens")
    private int maxTokens;

    /** 0 = deterministic, 1 = creative. 0.3 gives stable structured JSON. */
    @Builder.Default
    private double temperature = 0.3;

    /**
     * Forces the model to emit valid JSON.
     * Must be accompanied by a system/user message that explicitly asks for JSON.
     */
    @JsonProperty("response_format")
    @Builder.Default
    private ResponseFormat responseFormat = ResponseFormat.JSON_OBJECT;

    // ── Nested types ──────────────────────────────────────────────────────

    @Data
    @Builder
    public static class Message {
        private String role;    // "system" | "user" | "assistant"
        private String content;

        public static Message system(String content) {
            return Message.builder().role("system").content(content).build();
        }

        public static Message user(String content) {
            return Message.builder().role("user").content(content).build();
        }
    }

    @Data
    public static class ResponseFormat {
        private final String type;

        public static final ResponseFormat JSON_OBJECT = new ResponseFormat("json_object");
    }
}
