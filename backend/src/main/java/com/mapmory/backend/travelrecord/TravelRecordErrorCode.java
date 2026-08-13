package com.mapmory.backend.travelrecord;

import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;

public enum TravelRecordErrorCode implements ErrorCode {

    REGION_REQUIRED(
            ErrorKind.INVALID_INPUT,
            "REGION_REQUIRED",
            "필수 지역 정보가 없습니다.",
            "하위 지역을 조회하려면 상위 지역 코드를 함께 입력해야 합니다."
    ),
    INVALID_REGION_CODE(
            ErrorKind.INVALID_INPUT,
            "VALIDATION_ERROR",
            "요청 값이 올바르지 않습니다.",
            "지역 코드 형식이 올바르지 않습니다."
    ),
    COUNTRY_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            "COUNTRY_NOT_FOUND",
            "국가를 찾을 수 없습니다.",
            "요청한 국가 코드에 해당하는 국가가 없습니다."
    ),
    REGION_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            "REGION_NOT_FOUND",
            "지역을 찾을 수 없습니다.",
            "요청한 지역 코드에 해당하는 지역이 없습니다."
    ),
    INVALID_REGION_HIERARCHY(
            ErrorKind.INVALID_INPUT,
            "INVALID_REGION_HIERARCHY",
            "지역 계층이 올바르지 않습니다.",
            "요청한 지역은 선택한 상위 지역에 속하지 않습니다."
    ),
    INVALID_PAGINATION(
            ErrorKind.INVALID_INPUT,
            "VALIDATION_ERROR",
            "요청 값이 올바르지 않습니다.",
            "페이지 번호 또는 페이지 크기가 올바르지 않습니다."
    ),
    MEMBER_NOT_FOUND(
            ErrorKind.NOT_FOUND,
            "MEMBER_NOT_FOUND",
            "회원을 찾을 수 없습니다.",
            "요청한 회원 ID에 해당하는 회원이 없습니다."
    );

    private final ErrorKind kind;
    private final String code;
    private final String title;
    private final String detail;

    TravelRecordErrorCode(ErrorKind kind, String code, String title, String detail) {
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
