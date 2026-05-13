package com.app.ai.client;

import com.app.ai.client.dto.OpenAiRequest;
import com.app.ai.client.dto.OpenAiResponse;
import com.app.ai.config.OpenAiProperties;
import com.app.common.AppException;
import com.app.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Low-level HTTP client for the OpenAI Chat Completions endpoint.
 *
 * Retry strategy:
 *  - 429 (rate limit): NOT retried — surface immediately as {@link ErrorCode#AI_RATE_LIMIT_EXCEEDED}.
 *  - 5xx / network errors: up to {@code app.openai.max-retries} attempts with
 *    exponential backoff starting at 1 s.
 *  - Timeout: propagated as {@link ErrorCode#AI_SERVICE_UNAVAILABLE}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Qualifier("openAiWebClient")
    private final WebClient webClient;

    private final OpenAiProperties props;

    // ── Public API ────────────────────────────────────────────────────────

    public OpenAiResponse chat(List<OpenAiRequest.Message> messages) {
        OpenAiRequest body = OpenAiRequest.builder()
                .model(props.getModel())
                .messages(messages)
                .maxTokens(props.getMaxTokens())
                .build();

        log.debug("Sending {} message(s) to OpenAI model={}", messages.size(), props.getModel());

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, resp -> {
                    log.warn("OpenAI rate limit hit");
                    return Mono.error(new AppException(ErrorCode.AI_RATE_LIMIT_EXCEEDED));
                })
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class).flatMap(err -> {
                            log.error("OpenAI 4xx error: {}", err);
                            return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE,
                                    "OpenAI rejected the request: " + err));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                    log.error("OpenAI 5xx error — will retry");
                    return Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE));
                })
                .bodyToMono(OpenAiResponse.class)
                .retryWhen(Retry.backoff(props.getMaxRetries(), Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(8))
                        .filter(this::isRetryable)
                        .doBeforeRetry(sig -> log.warn(
                                "Retrying OpenAI call (attempt {}): {}",
                                sig.totalRetries() + 1, sig.failure().getMessage()))
                        .onRetryExhaustedThrow((spec, sig) -> {
                            log.error("OpenAI retries exhausted after {} attempts", props.getMaxRetries());
                            return new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
                        }))
                .timeout(Duration.ofSeconds(props.getTimeoutSeconds()),
                        Mono.error(new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE,
                                "OpenAI request timed out after " + props.getTimeoutSeconds() + "s")))
                .doOnSuccess(r -> log.debug("OpenAI response received, tokens_used={}",
                        r.getUsage() != null ? r.getUsage().getTotalTokens() : "?"))
                .block();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Only 5xx and network-level errors are safe to retry. Rate limits are not. */
    private boolean isRetryable(Throwable t) {
        if (t instanceof AppException ex) {
            return ex.getErrorCode() == ErrorCode.AI_SERVICE_UNAVAILABLE;
        }
        // WebClientRequestException covers DNS, connection-refused, etc.
        return t instanceof WebClientRequestException;
    }
}
