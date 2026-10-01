package com.mapmory.backend.place;

import java.util.List;

/** 장소 제공자에서 후보와 상세 정보를 조회하는 포트. */
public interface PlaceLookupPort {

    String providerCode();

    List<PlaceCandidate> search(String query);

    PlaceDetails findById(String placeId);
}
