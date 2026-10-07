package com.mapmory.backend.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.auth.application.model.SocialIdentity;
import com.mapmory.backend.auth.application.port.SocialIdentityPort;
import com.mapmory.backend.auth.exception.AuthErrorCode;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.AuthProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class SocialIdentityPortsTest {

    @Test
    void 제공자에_맞는_포트로_토큰을_확인한다() {
        SocialIdentityPorts ports = new SocialIdentityPorts(List.of(
                new FakePort(AuthProvider.KAKAO, "kakao-id"),
                new FakePort(AuthProvider.GOOGLE, "google-sub")
        ));

        SocialIdentity identity = ports.verify(AuthProvider.GOOGLE, "token");

        assertThat(identity.providerId()).isEqualTo("google-sub");
    }

    @Test
    void 포트가_없는_제공자면_UNSUPPORTED_SOCIAL_PROVIDER다() {
        SocialIdentityPorts ports = new SocialIdentityPorts(List.of(new FakePort(AuthProvider.KAKAO, "kakao-id")));

        assertUnsupported(ports, AuthProvider.GOOGLE);
        assertUnsupported(ports, AuthProvider.GUEST);
    }

    @Test
    void 같은_제공자의_포트가_둘이면_만들_수_없다() {
        List<SocialIdentityPort> duplicated = List.of(
                new FakePort(AuthProvider.KAKAO, "first"),
                new FakePort(AuthProvider.KAKAO, "second")
        );

        assertThatThrownBy(() -> new SocialIdentityPorts(duplicated))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void provider가_null인_포트는_원인을_밝히며_실패한다() {
        List<SocialIdentityPort> unnamed = List.of(new FakePort(null, "id"));

        assertThatThrownBy(() -> new SocialIdentityPorts(unnamed))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("provider()가 null");
    }

    private void assertUnsupported(SocialIdentityPorts ports, AuthProvider provider) {
        assertThatThrownBy(() -> ports.verify(provider, "token"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER));
    }

    private record FakePort(AuthProvider provider, String providerId) implements SocialIdentityPort {

        @Override
        public SocialIdentity verify(String token) {
            return new SocialIdentity(providerId, null);
        }
    }
}
