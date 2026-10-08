package com.mapmory.backend.place.infrastructure.kakao;

/** Kakao 키워드 검색 결과 한 건. */
public record KakaoPlace(String id, String name, String address, double latitude, double longitude) {
}
