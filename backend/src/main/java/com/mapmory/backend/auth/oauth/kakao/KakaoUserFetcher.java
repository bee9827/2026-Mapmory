package com.mapmory.backend.auth.oauth.kakao;

import com.mapmory.backend.auth.oauth.SocialUser;
import com.mapmory.backend.auth.oauth.SocialUserFetcher;
import com.mapmory.backend.member.AuthProvider;
import org.springframework.stereotype.Component;

@Component
public class KakaoUserFetcher implements SocialUserFetcher {

    private final KakaoApiClient kakaoApiClient;

    public KakaoUserFetcher(KakaoApiClient kakaoApiClient) {
        this.kakaoApiClient = kakaoApiClient;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public SocialUser fetch(String kakaoAccessToken) {
        KakaoUserResponse kakaoUser = kakaoApiClient.fetchUser(kakaoAccessToken);
        return new SocialUser(String.valueOf(kakaoUser.id()), kakaoUser.nickname());
    }
}
