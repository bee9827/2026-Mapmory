package com.mapmory.backend.upload.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ObjectKeyGeneratorTest {

    private final ObjectKeyGenerator objectKeyGenerator = new ObjectKeyGenerator();

    @ParameterizedTest
    @CsvSource({
            "JPEG, jpg",
            "PNG, png",
            "WEBP, webp",
            "HEIC, heic"
    })
    void 회원과_검증된_파일_형식으로_Object_Key를_생성한다(
            UploadFileType fileType,
            String extension
    ) {
        String objectKey = objectKeyGenerator.generate(10L, fileType);

        assertThat(objectKey).matches(
                "travel-records/10/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-"
                        + "[0-9a-f]{4}-[0-9a-f]{12}\\." + extension
        );
    }
}
