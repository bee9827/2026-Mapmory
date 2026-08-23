package com.mapmory.backend.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

class TagNameTest {

    @Test
    void 표시_이름과_중복_비교_키를_만든다() {
        TagName tagName = TagName.from("  Date   Course  ");

        assertThat(tagName.displayName()).isEqualTo("Date Course");
        assertThat(tagName.nameKey()).isEqualTo("date course");
    }

    @Test
    void 표시_이름을_NFC로_정규화한다() {
        TagName tagName = TagName.from("가");

        assertThat(tagName.displayName()).isEqualTo("가");
    }

    @Test
    void 유효하지_않은_이름을_거부한다() {
        assertInvalidName("   ");
        assertInvalidName("#연인");
        assertInvalidName("친구\u0000");
        assertInvalidName("가".repeat(31));
    }

    @Test
    void 표시_이름과_비교_키가_일치하지_않는_직접_생성을_거부한다() {
        assertThatThrownBy(() -> new TagName("친구", "friend"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(TagErrorCode.INVALID_TAG_NAME);
    }

    private void assertInvalidName(String rawName) {
        assertThatThrownBy(() -> TagName.from(rawName))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(TagErrorCode.INVALID_TAG_NAME);
    }
}
