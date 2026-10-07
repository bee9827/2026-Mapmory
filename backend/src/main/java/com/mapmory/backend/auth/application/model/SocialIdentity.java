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

    // provider_id가 비면 서로 다른 사람이 한 회원으로 묶이므로, 어떤 제공자든 여기서 막는다.
    public SocialIdentity {
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("소셜 제공자의 회원 식별자가 비어 있습니다.");
        }
    }
}
