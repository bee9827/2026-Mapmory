package com.mapmory.backend.upload.storage;

import java.net.URI;
import java.time.Duration;

public interface PresignedUrlProvider {

    URI createPresignedPutUrl(
            String objectKey,
            String contentType,
            long contentLength,
            Duration expiration
    );

    URI createPresignedGetUrl(
            String objectKey,
            Duration expiration
    );
}
