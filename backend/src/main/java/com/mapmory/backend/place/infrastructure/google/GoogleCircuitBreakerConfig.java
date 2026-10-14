package com.mapmory.backend.place.infrastructure.google;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google 호출 앞에 두는 서킷 브레이커를 만든다. 무엇을 실패로 세는지와 열렸을 때의 동작은 {@link GooglePlacesClient}가 정한다.
 */
@Configuration
@EnableConfigurationProperties(GoogleCircuitBreakerProperties.class)
class GoogleCircuitBreakerConfig {

    @Bean
    CircuitBreaker googlePlacesCircuitBreaker(GoogleCircuitBreakerProperties properties) {
        return circuitBreaker(properties, Clock.systemUTC());
    }

    static CircuitBreaker circuitBreaker(GoogleCircuitBreakerProperties properties, Clock clock) {
        return CircuitBreaker.ofDefaults("googlePlaces");
    }
}
