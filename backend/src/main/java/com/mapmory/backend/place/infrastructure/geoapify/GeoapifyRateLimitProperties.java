package com.mapmory.backend.place.infrastructure.geoapify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geoapify.rate-limit")
public record GeoapifyRateLimitProperties(
        int searchesPerMemberPerMinute,
        int searchesPerDay,
        int requestsPerDay,
        int requestsPerSecond
) {
    public GeoapifyRateLimitProperties {
        if (searchesPerMemberPerMinute < 1 || searchesPerDay < 1
                || requestsPerDay <= searchesPerDay || requestsPerSecond < 1) {
            throw new IllegalArgumentException("Geoapify 요청 한도 설정이 올바르지 않습니다.");
        }
    }
}
