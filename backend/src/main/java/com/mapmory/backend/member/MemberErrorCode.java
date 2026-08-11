package com.mapmory.backend.member;

import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;

public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND;

    @Override
    public ErrorKind kind() {
        return ErrorKind.NOT_FOUND;
    }

    @Override
    public String code() {
        return null;
    }

    @Override
    public String title() {
        return "회원을 찾을 수 없습니다.";
    }

    @Override
    public String detail() {
        return "요청한 회원이 존재하지 않습니다.";
    }
}
