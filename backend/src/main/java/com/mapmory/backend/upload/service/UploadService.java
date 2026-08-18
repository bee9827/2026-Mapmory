package com.mapmory.backend.upload.service;

import com.mapmory.backend.upload.dto.CreatePresignedUrlsRequest;
import com.mapmory.backend.upload.dto.CreatePresignedUrlsResponse;
import com.mapmory.backend.upload.dto.PresignedUploadResponse;
import com.mapmory.backend.upload.dto.UploadFileRequest;
import com.mapmory.backend.upload.policy.ObjectKeyGenerator;
import com.mapmory.backend.upload.policy.UploadPolicy;
import com.mapmory.backend.upload.policy.UploadPolicyProperties;
import com.mapmory.backend.upload.storage.PresignedUrlProvider;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

    private static final String UPLOAD_METHOD = "PUT";

    private final UploadPolicy uploadPolicy;
    private final ObjectKeyGenerator objectKeyGenerator;
    private final PresignedUrlProvider presignedUrlProvider;
    private final Duration presignedUrlExpiration;

    public UploadService(
            UploadPolicy uploadPolicy,
            ObjectKeyGenerator objectKeyGenerator,
            PresignedUrlProvider presignedUrlProvider,
            UploadPolicyProperties properties
    ) {
        this.uploadPolicy = uploadPolicy;
        this.objectKeyGenerator = objectKeyGenerator;
        this.presignedUrlProvider = presignedUrlProvider;
        this.presignedUrlExpiration = properties.presignedUrlExpiration();
    }

    public CreatePresignedUrlsResponse createPresignedUrls(
            Long memberId,
            CreatePresignedUrlsRequest request
    ) {
        uploadPolicy.validateFileCount(request.files().size());
        request.files().forEach(file ->
                uploadPolicy.validateFile(file.fileName(), file.contentType(), file.fileSize()));

        List<PresignedUploadResponse> uploads = request.files().stream()
                .map(file -> createPresignedUpload(memberId, file))
                .toList();
        return new CreatePresignedUrlsResponse(uploads);
    }

    private PresignedUploadResponse createPresignedUpload(Long memberId, UploadFileRequest file) {
        String objectKey = objectKeyGenerator.generate(memberId, file.fileName());
        URI presignedUrl = presignedUrlProvider.createPresignedPutUrl(
                objectKey,
                file.contentType(),
                file.fileSize(),
                presignedUrlExpiration
        );
        return new PresignedUploadResponse(
                objectKey,
                presignedUrl.toString(),
                UPLOAD_METHOD,
                file.contentType(),
                presignedUrlExpiration.toSeconds()
        );
    }
}
