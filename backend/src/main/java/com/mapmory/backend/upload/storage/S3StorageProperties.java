package com.mapmory.backend.upload.storage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "upload.storage.s3")
public record S3StorageProperties(
        @NotBlank String bucket,
        @NotBlank String region
) {
}
