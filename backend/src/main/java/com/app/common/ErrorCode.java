package com.app.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Centralised error codes used across all modules.
 * Each entry carries the HTTP status that should accompany it.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Generic ────────────────────────────────────────────────────────────
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Resource already exists"),

    // ── Auth ───────────────────────────────────────────────────────────────
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token is invalid"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email address is already registered"),

    // ── User ───────────────────────────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),

    // ── Resume ─────────────────────────────────────────────────────────────
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "Resume not found"),
    RESUME_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_ENTITY, "Resume limit exceeded for current plan"),

    // ── AI ─────────────────────────────────────────────────────────────────
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI service is temporarily unavailable"),
    AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI rate limit exceeded"),
    AI_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "AI returned an unexpected response format"),

    // ── File ───────────────────────────────────────────────────────────────
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "File not found"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the allowed limit"),
    FILE_TYPE_NOT_ALLOWED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "File type is not allowed"),

    // ── Job Analysis ───────────────────────────────────────────────────────
    JOB_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "Job analysis not found");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
