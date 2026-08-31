package com.mapmory.backend.tag.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(@NotBlank String name) {
}
