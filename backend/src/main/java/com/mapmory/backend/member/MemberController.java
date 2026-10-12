package com.mapmory.backend.member;

import com.mapmory.backend.auth.security.LoginMember;
import com.mapmory.backend.common.dto.ApiResponse;
import com.mapmory.backend.member.dto.MemberNameRequest;
import com.mapmory.backend.member.dto.MemberResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ApiResponse<MemberResponse> findMe(@LoginMember Member member) {
        return ApiResponse.from(MemberResponse.from(member));
    }

    @PatchMapping
    public ApiResponse<MemberResponse> rename(
            @LoginMember Member member,
            @Valid @RequestBody MemberNameRequest request
    ) {
        return ApiResponse.from(MemberResponse.from(memberService.rename(member.getId(), request.name())));
    }
}
