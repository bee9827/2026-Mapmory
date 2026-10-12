package com.mapmory.backend.member.dto;

import com.mapmory.backend.member.Member;

public record MemberResponse(String name) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getName());
    }
}
