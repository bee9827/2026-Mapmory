package com.mapmory.backend.place.application.port;

/** 같은 회원의 짧은 시간 내 반복 검색을 제한한다. */
public interface PlaceSearchRateLimitPort {

    void checkSearch(Long memberId);
}
