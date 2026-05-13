package com.app.common;

import lombok.Getter;

/**
 * Single unchecked exception that carries an {@link ErrorCode}.
 * Throw this anywhere in the application; {@link GlobalExceptionHandler} will
 * convert it to the correct HTTP status + {@link ApiResponse}.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
    }
}
