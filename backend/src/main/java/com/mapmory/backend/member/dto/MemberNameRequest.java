package com.mapmory.backend.member.dto;

import jakarta.validation.constraints.NotNull;

public record MemberNameRequest(@NotNull String name) {
}
