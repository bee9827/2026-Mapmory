package com.mapmory.backend.upload.storage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * S3 저장 위치.
 *
 * keyPrefix는 공용 버킷에서 우리 객체를 한 곳에 모으기 위한 값이다.
 * 전용 버킷을 쓰는 환경에서는 비워 두면 된다.
 */
@Validated
@ConfigurationProperties(prefix = "upload.storage.s3")
public record S3StorageProperties(
        @NotBlank String bucket,
        @NotBlank String region,
        String keyPrefix
) {

    /**
     * 앞뒤 슬래시를 제거한 prefix. 값이 없으면 빈 문자열이다.
     *
     * 운영자가 콘솔의 경로 표기를 그대로 옮겨 적어 "/mapmory" 나 "mapmory/" 가 들어와도
     * 같은 위치를 가리키게 한다.
     */
    public String normalizedKeyPrefix() {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return "";
        }
        return keyPrefix.strip()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
    }
}
