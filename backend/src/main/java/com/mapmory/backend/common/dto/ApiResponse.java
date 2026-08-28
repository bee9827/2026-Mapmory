package com.mapmory.backend.common.dto;

public record ApiResponse<T>(T data) {

    public static <T> ApiResponse<T> from(T data) {
        return new ApiResponse<>(data);
    }
}
