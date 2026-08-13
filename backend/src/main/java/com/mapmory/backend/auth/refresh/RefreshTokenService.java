package com.mapmory.backend.auth.refresh;

import com.mapmory.backend.auth.exception.AuthErrorCode;
import com.mapmory.backend.auth.jwt.JwtProperties;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.Member;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * refresh 토큰 발급·회전·폐기.
 *
 * 원문(raw)은 SecureRandom으로 생성해 클라이언트에만 반환하고, 서버는 SHA-256 해시만 저장한다.
 * 회전 시 기존 토큰을 폐기하고, 이미 폐기된 토큰이 다시 제시되면(탈취 의심) 해당 회원의
 * 유효한 토큰을 모두 폐기한다.
 */
@Service
public class RefreshTokenService {

    private static final int RAW_TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenReuseDetector refreshTokenReuseDetector;
    private final Duration refreshTokenValidity;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenReuseDetector refreshTokenReuseDetector,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenReuseDetector = refreshTokenReuseDetector;
        this.refreshTokenValidity = jwtProperties.refreshTokenValidity();
    }

    @Transactional
    public String issue(Member member) {
        String rawToken = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plus(refreshTokenValidity);
        refreshTokenRepository.save(RefreshToken.issue(member, hash(rawToken), expiresAt));
        return rawToken;
    }

    /**
     * 회전을 위한 검증. 유효하면 기존 토큰을 폐기하고 소유 회원을 돌려준다.
     */
    @Transactional
    public Member validateAndRevoke(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        LocalDateTime now = LocalDateTime.now();
        if (refreshToken.isRevoked()) {
            // 이미 폐기된 토큰의 재사용 → 탈취로 간주하고 회원의 유효 토큰을 모두 폐기한다.
            // 폐기는 별도 트랜잭션에서 커밋해야 아래 예외로 롤백되지 않는다.
            refreshTokenReuseDetector.revokeAllActiveTokens(refreshToken.getMember(), now);
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (refreshToken.isExpired(now)) {
            throw new BusinessException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        refreshToken.revoke(now);
        return refreshToken.getMember();
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> token.revoke(LocalDateTime.now()));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }
}
