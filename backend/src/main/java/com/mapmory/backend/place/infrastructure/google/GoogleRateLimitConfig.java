package com.mapmory.backend.place.infrastructure.google;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google 호출 한도 버킷을 만든다. 확인 순서와 에러 코드는 {@link GoogleRequestLimiter}가 정한다.
 */
@Configuration
@EnableConfigurationProperties(GoogleRateLimitProperties.class)
class GoogleRateLimitConfig {

    static final String REQUEST_PACE = "googleRequestPace";
    static final String AUTOCOMPLETE_BUDGET = "googleAutocompleteBudget";
    static final String DETAILS_BUDGET = "googleDetailsBudget";

    // 분당 한도는 Google 할당량(분당)을 넘지 않게 하는 값이라 서버마다 메모리에서 따로 센다.
    @Bean(REQUEST_PACE)
    Bucket googleRequestPace(GoogleRateLimitProperties properties) {
        int perMinute = properties.requestsPerMinute();
        return Bucket.builder()
                .addLimit(bandwidth -> bandwidth.capacity(perMinute).refillGreedy(perMinute, Duration.ofMinutes(1)))
                .build();
    }

    // 하루 한도는 무료 사용량을 지키는 값이라 서버 전체가 함께 세도록 DB 버킷(place_rate_limit_bucket)에 쓴다.
    @Bean(AUTOCOMPLETE_BUDGET)
    BucketConfiguration googleAutocompleteBudget(GoogleRateLimitProperties properties) {
        return daily(properties.autocompletePerDay());
    }

    @Bean(DETAILS_BUDGET)
    BucketConfiguration googleDetailsBudget(GoogleRateLimitProperties properties) {
        return daily(properties.detailsPerDay());
    }

    private static BucketConfiguration daily(int capacity) {
        return BucketConfiguration.builder()
                .addLimit(bandwidth -> bandwidth.capacity(capacity).refillIntervally(capacity, Duration.ofDays(1)))
                .build();
    }
}
