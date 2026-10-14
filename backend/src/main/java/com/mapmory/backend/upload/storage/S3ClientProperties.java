package com.mapmory.backend.upload.storage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * S3 호출을 기다리는 한도. S3가 멈춰도 요청 스레드를 오래 쥐지 않게 한다.
 *
 * attemptTimeout은 한 번 시도의 한도이고, totalTimeout은 재시도까지 합친 호출 하나의 한도다.
 */
@Validated
@ConfigurationProperties(prefix = "upload.storage.s3.client")
public record S3ClientProperties(
        @NotNull @DurationMin(millis = 1) Duration attemptTimeout,
        @NotNull @DurationMin(millis = 1) Duration totalTimeout,
        @Positive int maxAttempts
) {
}
