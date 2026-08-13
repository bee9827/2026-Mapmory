package com.mapmory.backend.auth.kakao;

import com.mapmory.backend.auth.exception.AuthErrorCode;
import com.mapmory.backend.common.exception.BusinessException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * 카카오 사용자 정보 API 호출.
 *
 * 앱이 전달한 카카오 access token으로 GET /v2/user/me 를 호출해 회원 정보를 가져온다.
 * 카카오가 4xx/5xx를 반환하면(토큰 만료·위조 등) 인증 실패로 변환한다.
 */
@Component
@EnableConfigurationProperties(KakaoProperties.class)
public class KakaoApiClient {

    private final RestClient restClient;
    private final String userInfoUri;

    public KakaoApiClient(KakaoProperties kakaoProperties) {
        this.restClient = RestClient.create();
        this.userInfoUri = kakaoProperties.userInfoUri();
    }

    public KakaoUserResponse fetchUser(String kakaoAccessToken) {
        try {
            return restClient.get()
                    .uri(userInfoUri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);
        } catch (RestClientResponseException exception) {
            throw new BusinessException(AuthErrorCode.INVALID_KAKAO_TOKEN);
        }
    }
}
