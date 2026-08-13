package com.mapmory.backend.auth.exception;

import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;

public enum AuthErrorCode implements ErrorCode {

    INVALID_ACCESS_TOKEN(
            ErrorKind.AUTHENTICATION_REQUIRED,
            "유효하지 않은 인증 토큰입니다.",
            "인증 토큰이 유효하지 않습니다."
    ),
    EXPIRED_ACCESS_TOKEN(
            ErrorKind.AUTHENTICATION_REQUIRED,
            "만료된 인증 토큰입니다.",
            "인증 토큰이 만료되었습니다. 토큰을 갱신하거나 다시 로그인하세요."
    ),
    ACCESS_DENIED(
            ErrorKind.ACCESS_DENIED,
            "접근 권한이 없습니다.",
            "요청한 리소스에 접근할 권한이 없습니다."
    ),
    INVALID_KAKAO_TOKEN(
            ErrorKind.AUTHENTICATION_REQUIRED,
            "카카오 인증에 실패했습니다.",
            "카카오 access token이 유효하지 않습니다."
    );

    private final ErrorKind kind;
    private final String title;
    private final String detail;

    AuthErrorCode(ErrorKind kind, String title, String detail) {
        this.kind = kind;
        this.title = title;
        this.detail = detail;
    }

    @Override
    public ErrorKind kind() {
        return kind;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String detail() {
        return detail;
    }
}
