package com.mapmory.backend.travelrecord.dto;

/**
 * 여행 기록 API의 성공 응답을 data 필드로 감싼다.
 * 오류 응답은 이 타입을 사용하지 않고 Problem Details 형식을 유지한다.
 */
public record TravelRecordResponse<T>(T data) {

    public static <T> TravelRecordResponse<T> of(T data) {
        return new TravelRecordResponse<>(data);
    }
}
