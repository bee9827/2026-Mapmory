package com.mapmory.backend.auth.infrastructure.apple;

import com.mapmory.backend.auth.application.model.SocialIdentity;
import com.mapmory.backend.auth.application.port.SocialIdentityPort;
import com.mapmory.backend.member.AuthProvider;
import org.springframework.stereotype.Component;

/**
 * 애플 identity token을 검증해 sub를 꺼낸다.
 *
 * 토큰에 이름이 없으므로 신원의 이름은 비워 둔다. 이름은 앱이 최초 로그인 때만 받아 요청에 함께 보낸다. (ADR 0020)
 */
@Component
public class AppleIdentityAdapter implements SocialIdentityPort {

    private final AppleIdentityTokenVerifier appleIdentityTokenVerifier;

    AppleIdentityAdapter(AppleIdentityTokenVerifier appleIdentityTokenVerifier) {
        this.appleIdentityTokenVerifier = appleIdentityTokenVerifier;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.APPLE;
    }

    @Override
    public SocialIdentity verify(String identityToken) {
        AppleUser appleUser = appleIdentityTokenVerifier.verify(identityToken);
        return new SocialIdentity(appleUser.subject(), null);
    }
}
