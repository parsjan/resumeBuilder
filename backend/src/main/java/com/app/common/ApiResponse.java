package com.app.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Standard envelope for every API response.
 *
 * Success:  { "success": true,  "data": {...},  "timestamp": "..." }
 * Failure:  { "success": false, "error": {...}, "timestamp": "..." }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    // ── Factory helpers ────────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorResponse error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }
}
