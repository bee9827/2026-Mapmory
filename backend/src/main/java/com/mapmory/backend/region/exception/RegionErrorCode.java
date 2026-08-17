package com.mapmory.backend.region.exception;

import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;

public enum RegionErrorCode implements ErrorCode {
    REGION_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            "지역을 찾을 수 없습니다.",
            "요청한 지역이 존재하지 않습니다."
    );

    private final ErrorKind kind;
    private final String title;
    private final String detail;

    RegionErrorCode(ErrorKind kind, String title, String detail) {
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
