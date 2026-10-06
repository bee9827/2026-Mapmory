package com.mapmory.backend.auth.google;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

/**
 * 구글이 안내하는 ID token 검증 규칙 중 서명 외의 항목.
 *
 * - exp/nbf : 만료되지 않았을 것
 * - iss     : accounts.google.com 또는 https://accounts.google.com
 * - aud     : 우리 앱의 OAuth 클라이언트 ID 중 하나 (다른 앱용으로 발급된 토큰 재사용 차단)
 */
public final class GoogleIdTokenValidators {

    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private GoogleIdTokenValidators() {
    }

    public static OAuth2TokenValidator<Jwt> create(List<String> clientIds) {
        return new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtClaimValidator<Object>(JwtClaimNames.ISS,
                        issuer -> issuer != null && ISSUERS.contains(issuer.toString())),
                new JwtClaimValidator<Collection<String>>(JwtClaimNames.AUD,
                        audience -> audience != null && audience.stream().anyMatch(clientIds::contains))
        );
    }
}
