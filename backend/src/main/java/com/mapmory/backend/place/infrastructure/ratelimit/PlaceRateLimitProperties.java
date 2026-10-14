package com.mapmory.backend.place.infrastructure.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "place.rate-limit")
public record PlaceRateLimitProperties(
        int searchesPerMemberPerMinute,
        int selectionsPerMemberPerDay
) {
    public PlaceRateLimitProperties {
        if (searchesPerMemberPerMinute < 1 || selectionsPerMemberPerDay < 1) {
            throw new IllegalArgumentException("장소 회원 한도 설정이 올바르지 않습니다.");
        }
    }
}
