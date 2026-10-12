package com.mapmory.backend.auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * identityToken : 앱이 Sign in with Apple로 받은 identity token(JWT)
 * name          : 애플은 이름을 최초 인증 때 앱에만 한 번 준다. 그때 받은 이름을 보내고, 이후 로그인에서는 비운다.
 */
public record AppleLoginRequest(
        @NotBlank
        String identityToken,

        @Size(max = 100)
        String name
) {
}
