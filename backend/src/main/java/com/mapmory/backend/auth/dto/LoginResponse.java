package com.mapmory.backend.auth.dto;

/**
 * 로그인 응답.
 *
 * 클라이언트는 accessToken만 저장해 이후 통신에 사용한다.
 * 회원 식별자(memberId 등)는 노출하지 않는다. ("내 데이터" 통신은 토큰만으로 충분)
 * isNewMember는 신규 가입 여부로, 온보딩 분기 등에 쓸 수 있다.
 */
public record LoginResponse(
        String accessToken,
        boolean isNewMember
) {
}
