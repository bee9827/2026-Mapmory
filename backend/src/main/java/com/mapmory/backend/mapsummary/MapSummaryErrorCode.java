package com.mapmory.backend.mapsummary;

import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;

public enum MapSummaryErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            null,
            "회원을 찾을 수 없습니다.",
            "요청한 회원이 존재하지 않습니다."
    ),
    COUNTRY_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            "COUNTRY_NOT_FOUND",
            "국가를 찾을 수 없습니다.",
            "요청한 국가가 존재하지 않습니다."
    ),
    LOCATION_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            "LOCATION_NOT_FOUND",
            "지역을 찾을 수 없습니다.",
            "요청한 상위 지역이 존재하지 않습니다."
    );

    private final ErrorKind kind;
    private final String code;
    private final String title;
    private final String detail;

    MapSummaryErrorCode(ErrorKind kind, String code, String title, String detail) {
        this.kind = kind;
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    @Override
    public ErrorKind kind() {
        return kind;
    }

    @Override
    public String code() {
        return code;
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
