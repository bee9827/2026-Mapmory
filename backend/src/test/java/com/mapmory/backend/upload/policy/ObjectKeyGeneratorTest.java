package com.mapmory.backend.upload.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.upload.UploadErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ObjectKeyGeneratorTest {

    private final ObjectKeyGenerator objectKeyGenerator = new ObjectKeyGenerator();

    @ParameterizedTest
    @CsvSource({
            "photo.jpg, jpg",
            "photo.jpeg, jpeg",
            "photo.PNG, png",
            "photo.webp, webp",
            "photo.heic, heic"
    })
    void 회원과_파일명으로_Object_Key를_생성한다(String fileName, String extension) {
        String objectKey = objectKeyGenerator.generate(10L, fileName);

        assertThat(objectKey).matches(
                "travel-records/10/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-"
                        + "[0-9a-f]{4}-[0-9a-f]{12}\\." + extension
        );
    }

    @Test
    void 지원하지_않는_확장자는_거절한다() {
        assertThatThrownBy(() -> objectKeyGenerator.generate(10L, "photo.gif"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(UploadErrorCode.INVALID_FILE_TYPE));
    }
}
