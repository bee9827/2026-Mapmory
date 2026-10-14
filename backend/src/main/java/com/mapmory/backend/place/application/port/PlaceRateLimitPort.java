package com.mapmory.backend.place.application.port;

/** 회원의 장소 검색과 후보 선택 횟수를 제한한다. 장소 API 인터셉터가 컨트롤러에 들어가기 전에 부른다. */
public interface PlaceRateLimitPort {

    void checkSearch(Long memberId);

    void checkSelection(Long memberId);
}
