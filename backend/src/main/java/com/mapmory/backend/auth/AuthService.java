package com.mapmory.backend.auth;

import com.mapmory.backend.auth.dto.LoginResponse;
import com.mapmory.backend.auth.jwt.JwtProvider;
import com.mapmory.backend.auth.kakao.KakaoApiClient;
import com.mapmory.backend.auth.kakao.KakaoUserResponse;
import com.mapmory.backend.member.AuthProvider;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_NAME = "회원";

    private final KakaoApiClient kakaoApiClient;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public AuthService(
            KakaoApiClient kakaoApiClient,
            MemberRepository memberRepository,
            JwtProvider jwtProvider
    ) {
        this.kakaoApiClient = kakaoApiClient;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public LoginResponse loginWithKakao(String kakaoAccessToken) {
        KakaoUserResponse kakaoUser = kakaoApiClient.fetchUser(kakaoAccessToken);
        String providerId = String.valueOf(kakaoUser.id());

        Optional<Member> existing =
                memberRepository.findByProviderAndProviderId(AuthProvider.KAKAO, providerId);
        boolean isNewMember = existing.isEmpty();
        Member member = existing.orElseGet(() -> register(providerId, kakaoUser.nickname()));

        String accessToken = jwtProvider.issueAccessToken(member.getId());
        return new LoginResponse(accessToken, isNewMember);
    }

    private Member register(String providerId, String nickname) {
        Member member = Member.ofOAuth(
                AuthProvider.KAKAO,
                providerId,
                resolveName(nickname),
                UUID.randomUUID()
        );
        return memberRepository.save(member);
    }

    private String resolveName(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return DEFAULT_NAME;
        }
        return nickname;
    }
}
