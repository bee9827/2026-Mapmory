package com.mapmory.backend.auth.application.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.auth.application.model.SocialIdentity;
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
    void 포트가_없는_제공자면_예외가_발생한다() {
        SocialIdentityPorts ports = new SocialIdentityPorts(List.of(new FakePort(AuthProvider.KAKAO, "kakao-id")));

        assertThatThrownBy(() -> ports.verify(AuthProvider.GOOGLE, "token"))
                .isInstanceOf(IllegalArgumentException.class);
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

    private record FakePort(AuthProvider provider, String providerId) implements SocialIdentityPort {

        @Override
        public SocialIdentity verify(String token) {
            return new SocialIdentity(providerId, null);
        }
    }
}
