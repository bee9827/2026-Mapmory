package com.mapmory.backend.auth.refresh;

import com.mapmory.backend.member.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :now where r.member = :member and r.revokedAt is null")
    void revokeAllActiveByMember(@Param("member") Member member, @Param("now") LocalDateTime now);
}
