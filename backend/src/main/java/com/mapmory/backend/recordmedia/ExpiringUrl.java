package com.mapmory.backend.recordmedia;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

public record ExpiringUrl(
        String url,
        long expiresIn
) {
    public ExpiringUrl {
        Objects.requireNonNull(url, "url must not be null");
        if (url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("expiresIn must be positive");
        }
    }

    public static ExpiringUrl from(URI url, Duration expiration) {
        return new ExpiringUrl(url.toString(), expiration.toSeconds());
    }
}
