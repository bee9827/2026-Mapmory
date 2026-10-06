package com.mapmory.backend.auth.application.port;

import com.mapmory.backend.auth.application.model.SocialIdentity;
import com.mapmory.backend.member.AuthProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 소셜 로그인 제공자별 신원 확인 포트 모음.
 *
 * 제공자 하나에 포트는 하나여야 한다. 같은 제공자의 포트가 둘이면 어느 쪽으로 확인할지
 * 정할 수 없으므로 애플리케이션이 뜰 때 실패시킨다.
 */
@Component
public class SocialIdentityPorts {

    private final Map<AuthProvider, SocialIdentityPort> ports = new EnumMap<>(AuthProvider.class);

    public SocialIdentityPorts(List<SocialIdentityPort> socialIdentityPorts) {
        socialIdentityPorts.forEach(this::register);
    }

    private void register(SocialIdentityPort port) {
        SocialIdentityPort duplicate = ports.putIfAbsent(port.provider(), port);
        if (duplicate != null) {
            throw new IllegalStateException("같은 제공자의 소셜 로그인 포트가 둘 이상입니다: " + port.provider());
        }
    }

    public SocialIdentity verify(AuthProvider provider, String token) {
        return find(provider).verify(token);
    }

    private SocialIdentityPort find(AuthProvider provider) {
        SocialIdentityPort port = ports.get(provider);
        if (port == null) {
            throw new IllegalArgumentException("소셜 로그인을 지원하지 않는 제공자입니다: " + provider);
        }
        return port;
    }
}
