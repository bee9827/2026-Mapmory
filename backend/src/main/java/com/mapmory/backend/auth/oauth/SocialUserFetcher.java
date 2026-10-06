package com.mapmory.backend.auth.oauth;

import com.mapmory.backend.member.AuthProvider;

/**
 * 앱이 전달한 소셜 토큰으로 사용자 신원을 확인한다.
 *
 * 제공자마다 확인 방법(사용자 정보 API 호출, ID token 서명 검증 등)은 다르지만,
 * 가입·승격 규칙은 같으므로 AuthService는 이 인터페이스만 보고 처리한다.
 */
public interface SocialUserFetcher {

    AuthProvider provider();

    SocialUser fetch(String token);
}
