package com.mapmory.backend.auth.oauth.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String userInfoUri
) {
}
