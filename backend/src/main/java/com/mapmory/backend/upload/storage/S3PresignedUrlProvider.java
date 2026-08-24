package com.mapmory.backend.upload.storage;

import java.net.URI;
import java.time.Duration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3PresignedUrlProvider implements PresignedUrlProvider {

    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3PresignedUrlProvider(
            S3Presigner s3Presigner,
            S3StorageProperties properties
    ) {
        this.s3Presigner = s3Presigner;
        this.bucket = properties.bucket();
    }

    @Override
    public URI createPresignedPutUrl(
            String objectKey,
            String contentType,
            long contentLength,
            Duration expiration
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build();

        return URI.create(s3Presigner.presignPutObject(presignRequest).url().toString());
    }
}
