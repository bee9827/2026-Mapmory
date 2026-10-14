package com.mapmory.backend.place.infrastructure.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.places.rate-limit")
public record GoogleRateLimitProperties(
        int autocompletePerDay,
        int detailsPerDay,
        int requestsPerMinute
) {
    public GoogleRateLimitProperties {
        if (autocompletePerDay < 1 || detailsPerDay < 1 || requestsPerMinute < 1) {
            throw new IllegalArgumentException("Google 요청 한도 설정이 올바르지 않습니다.");
        }
    }
}
