package com.mapmory.backend.auth.infrastructure.google.config;

import com.mapmory.backend.auth.infrastructure.google.client.GoogleIdTokenValidators;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableConfigurationProperties(GoogleProperties.class)
public class GoogleClientConfig {

    /**
     * 구글 JWKS로 서명을 검증하는 디코더. 공개키는 디코더가 내려받아 캐시하고,
     * 처음 보는 kid가 오면 다시 내려받는다.
     *
     * JWKS 호출에도 공통 HTTP 타임아웃이 적용되도록 Spring Boot가 구성한 Builder를 사용한다.
     */
    @Bean
    public JwtDecoder googleIdTokenDecoder(GoogleProperties googleProperties, RestTemplateBuilder builder) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(googleProperties.jwkSetUri())
                .restOperations(builder.build())
                .build();
        decoder.setJwtValidator(GoogleIdTokenValidators.create(googleProperties.clientIds()));
        return decoder;
    }
}
