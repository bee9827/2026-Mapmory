package com.mapmory.backend.upload.storage;

import com.mapmory.backend.common.monitoring.MonitoredOperation;
import com.mapmory.backend.common.monitoring.OperationTimer;
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
    private final OperationTimer operationTimer;

    public S3PresignedUrlProvider(
            S3Presigner s3Presigner,
            S3StorageProperties properties,
            OperationTimer operationTimer
    ) {
        this.s3Presigner = s3Presigner;
        this.bucket = properties.bucket();
        this.operationTimer = operationTimer;
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

        return operationTimer.record(
                MonitoredOperation.S3_PRESIGN,
                () -> URI.create(s3Presigner.presignPutObject(presignRequest).url().toString())
        );
    }
}
