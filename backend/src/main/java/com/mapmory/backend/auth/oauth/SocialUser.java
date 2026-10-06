package com.mapmory.backend.auth.oauth;

/**
 * 소셜 로그인 제공자에게서 확인한 사용자 신원.
 *
 * providerId : 제공자가 발급한 계정 고유 식별자. 회원의 provider_id로 사용한다.
 * nickname   : 표시 이름. 사용자가 동의하지 않으면 없을 수 있다.
 */
public record SocialUser(
        String providerId,
        String nickname
) {
}
