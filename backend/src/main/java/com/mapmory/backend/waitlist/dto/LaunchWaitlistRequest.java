package com.mapmory.backend.waitlist.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LaunchWaitlistRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식으로 입력해 주세요.")
        @Size(max = 254, message = "이메일은 254자 이하여야 합니다.")
        String email,

        @AssertTrue(message = "개인정보 수집 및 이용에 동의해 주세요.")
        boolean privacyConsent,

        @AssertTrue(message = "만 14세 이상만 출시 알림을 신청할 수 있습니다.")
        boolean ageConfirmed
) {
}
