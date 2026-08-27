package com.mapmory.backend.common.exception;

import java.io.Serial;
import java.util.Objects;

public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, requireErrorCode(errorCode).detail(), null);
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, null);
    }

    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(message(errorCode, detail), cause);
        this.errorCode = requireErrorCode(errorCode);
        this.detail = Objects.requireNonNull(detail, "detail must not be null");
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }

    private static String message(ErrorCode errorCode, String detail) {
        return "[%s] %s".formatted(
                requireErrorCode(errorCode).code(),
                Objects.requireNonNull(detail, "detail must not be null")
        );
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
