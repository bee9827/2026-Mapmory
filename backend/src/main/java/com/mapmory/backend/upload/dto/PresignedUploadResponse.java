package com.mapmory.backend.upload.dto;

public record PresignedUploadResponse(
        String objectKey,
        String presignedUrl,
        String method,
        String contentType,
        long expiresIn
) {
}
