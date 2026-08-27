package com.mapmory.backend.upload.policy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "upload.policy")
public record UploadPolicyProperties(
        @NotNull
        @DataSizeUnit(DataUnit.BYTES)
        DataSize maxFileSize,

        @Positive int maxFilesPerRequest,

        @NotNull
        @DurationMin(seconds = 1)
        @DurationMax(seconds = 604_800)
        Duration presignedUrlExpiration
) {
}
