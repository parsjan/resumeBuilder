package com.app.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Domain exception ──────────────────────────────────────────────────

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("AppException [{}]: {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getErrorCode().name())
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ApiResponse.failure(error));
    }

    // ── Bean Validation (@Valid / @Validated) ─────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.name())
                .message(ErrorCode.VALIDATION_FAILED.getDefaultMessage())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(ApiResponse.failure(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> ErrorResponse.FieldError.builder()
                        .field(cv.getPropertyPath().toString())
                        .message(cv.getMessage())
                        .build())
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.name())
                .message(ErrorCode.VALIDATION_FAILED.getDefaultMessage())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(ApiResponse.failure(error));
    }

    // ── Spring Security ───────────────────────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("AuthenticationException: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.UNAUTHORIZED.name())
                .message(ErrorCode.UNAUTHORIZED.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.FORBIDDEN.name())
                .message(ErrorCode.FORBIDDEN.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(error));
    }

    // ── File upload ───────────────────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.FILE_TOO_LARGE.name())
                .message(ErrorCode.FILE_TOO_LARGE.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.failure(error));
    }

    // ── Fallback ──────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);

        ErrorResponse error = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .message(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(error));
    }
}
