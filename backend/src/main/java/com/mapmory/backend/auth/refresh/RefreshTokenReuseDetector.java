package com.mapmory.backend.auth.refresh;

import com.mapmory.backend.member.Member;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * refresh 토큰 재사용(탈취) 감지 시 회원의 유효 토큰을 폐기한다.
 *
 * 폐기는 별도 트랜잭션(REQUIRES_NEW)으로 커밋한다. 호출 측이 이후 401 예외를 던져
 * 바깥 트랜잭션이 롤백되어도, 이 폐기는 유지되어야 하기 때문이다.
 */
@Component
public class RefreshTokenReuseDetector {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenReuseDetector(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllActiveTokens(Member member, LocalDateTime now) {
        refreshTokenRepository.revokeAllActiveByMember(member, now);
    }
}
