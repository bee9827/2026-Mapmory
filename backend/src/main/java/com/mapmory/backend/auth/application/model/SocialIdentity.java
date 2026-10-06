package com.mapmory.backend.auth.application.model;

/**
 * 소셜 제공자가 확인해 준 신원.
 *
 * providerId : 제공자 안에서 바뀌지 않는 회원 식별자. provider_id로 저장한다.
 * name       : 표시 이름. 사용자가 동의하지 않으면 없을 수 있다.
 */
public record SocialIdentity(
        String providerId,
        String name
) {
}
