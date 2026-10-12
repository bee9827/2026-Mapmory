package com.mapmory.backend.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @Test
    void 앞뒤_공백을_지운다() {
        assertThat(MemberName.from("  티온 ")).isEqualTo("티온");
    }

    @Test
    void 이십자까지_허용하고_이모지는_한_글자로_센다() {
        assertThat(MemberName.from("가".repeat(20))).hasSize(20);
        assertThat(MemberName.from("😀".repeat(20))).isEqualTo("😀".repeat(20));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "가나다라마바사아자차카타파하가나다라마바사", "티\n온", "티\u0000온"})
    void 비었거나_길거나_제어_문자가_있으면_거부한다(String rawName) {
        assertInvalid(rawName);
    }

    @Test
    void null이면_거부한다() {
        assertInvalid(null);
    }

    private void assertInvalid(String rawName) {
        assertThatThrownBy(() -> MemberName.from(rawName))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_MEMBER_NAME));
    }
}
