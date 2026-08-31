package com.mapmory.backend.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

    @Test
    void includesErrorCodeInInternalMessage() {
        BusinessException exception = new BusinessException(
                TestErrorCode.TRAVEL_RECORD_NOT_FOUND
        );

        assertThat(exception.getMessage())
                .isEqualTo("[TRAVEL_RECORD_NOT_FOUND] 요청한 여행 기록이 존재하지 않습니다.");
    }

    @Test
    void keepsPublicDetailUnchanged() {
        BusinessException exception = new BusinessException(
                TestErrorCode.TRAVEL_RECORD_NOT_FOUND
        );

        assertThat(exception.getDetail())
                .isEqualTo("요청한 여행 기록이 존재하지 않습니다.");
    }

    private enum TestErrorCode implements ErrorCode {
        TRAVEL_RECORD_NOT_FOUND;

        @Override
        public ErrorKind kind() {
            return ErrorKind.NOT_FOUND;
        }

        @Override
        public String code() {
            return name();
        }

        @Override
        public String title() {
            return "여행 기록을 찾을 수 없습니다.";
        }

        @Override
        public String detail() {
            return "요청한 여행 기록이 존재하지 않습니다.";
        }
    }
}
