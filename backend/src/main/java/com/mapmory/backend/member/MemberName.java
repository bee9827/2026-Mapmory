package com.mapmory.backend.member;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;

/** 사용자가 정하는 회원 이름의 규칙. 소셜 제공자에서 받은 이름(최대 50자)에는 적용하지 않는다. */
final class MemberName {

    static final int MAX_LENGTH = 20;

    private MemberName() {
    }

    static String from(String rawName) {
        if (rawName == null) {
            throw invalid();
        }
        String name = rawName.strip();
        int length = name.codePointCount(0, name.length());
        if (length == 0 || length > MAX_LENGTH || name.codePoints().anyMatch(Character::isISOControl)) {
            throw invalid();
        }
        return name;
    }

    private static BusinessException invalid() {
        return new BusinessException(MemberErrorCode.INVALID_MEMBER_NAME);
    }
}
