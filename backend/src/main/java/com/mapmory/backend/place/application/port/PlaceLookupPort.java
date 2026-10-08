package com.mapmory.backend.place.application.port;

import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import java.util.List;

/** 장소 제공자에서 후보와 상세 정보를 조회하는 포트. */
public interface PlaceLookupPort {

    /** 장소 ID를 발급한 제공자 코드. 여행 기록에 함께 저장한다. */
    String providerCode(String placeId);

    List<PlaceCandidate> search(String query);

    PlaceDetails findById(String placeId);
}
