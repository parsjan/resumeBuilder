package com.app.ai.parser;

import com.app.ai.client.dto.OpenAiResponse;
import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validates an {@link OpenAiResponse} and deserialises its JSON content
 * into the target DTO type.
 *
 * Validation steps:
 *  1. Response must have at least one choice.
 *  2. Finish reason must be "stop" (not "length" — token limit hit).
 *  3. Content must be non-blank.
 *  4. Content must be valid JSON parseable into the target type.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Validates the raw {@link OpenAiResponse} and deserialises the embedded
     * JSON string into {@code targetType}.
     *
     * @throws AppException with {@link ErrorCode#AI_INVALID_RESPONSE} if the
     *                      response is malformed, truncated, or not valid JSON.
     */
    public <T> T parse(OpenAiResponse response, Class<T> targetType) {
        validateResponse(response);
        String content = response.extractContent();
        return deserialize(content, targetType);
    }

    /**
     * Validates and returns the raw JSON string without deserialising.
     * Useful when the caller needs to inspect the JSON before mapping.
     */
    public String parseRawJson(OpenAiResponse response) {
        validateResponse(response);
        return response.extractContent();
    }

    // ── Validation ────────────────────────────────────────────────────────

    private void validateResponse(OpenAiResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            log.error("OpenAI returned empty choices");
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE, "AI returned no choices");
        }

        String finishReason = response.getChoices().get(0).getFinishReason();
        if ("length".equals(finishReason)) {
            log.error("OpenAI response truncated by token limit");
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE,
                    "AI response was truncated — increase max_tokens or shorten the prompt");
        }

        String content = response.extractContent();
        if (content == null || content.isBlank()) {
            log.error("OpenAI returned blank content");
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE, "AI returned blank content");
        }

        assertValidJson(content);
    }

    private void assertValidJson(String content) {
        try {
            objectMapper.readValue(content, JsonNode.class);
        } catch (JsonProcessingException e) {
            log.error("OpenAI content is not valid JSON: {}", content);
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE,
                    "AI response is not valid JSON: " + e.getOriginalMessage());
        }
    }

    // ── Deserialisation ───────────────────────────────────────────────────

    private <T> T deserialize(String json, Class<T> targetType) {
        try {
            return objectMapper.readValue(json, targetType);
        } catch (JsonProcessingException e) {
            log.error("Failed to map AI JSON to {}: {}", targetType.getSimpleName(), json);
            throw new AppException(ErrorCode.AI_INVALID_RESPONSE,
                    "AI response could not be mapped to expected structure: " + e.getOriginalMessage());
        }
    }
}
