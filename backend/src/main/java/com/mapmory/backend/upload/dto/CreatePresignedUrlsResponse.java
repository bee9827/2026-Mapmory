package com.mapmory.backend.upload.dto;

import java.util.List;

public record CreatePresignedUrlsResponse(
        List<PresignedUploadResponse> uploads
) {
}
