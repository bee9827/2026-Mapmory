package com.mapmory.backend.place.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.application.model.SelectedPlace;
import com.mapmory.backend.place.application.model.SuggestedRegion;
import com.mapmory.backend.place.application.model.SuggestedRegion.Area;
import com.mapmory.backend.place.web.dto.PlaceSelectionResponse;
import com.mapmory.backend.travelrecord.dto.RegionItemResponse;
import org.junit.jupiter.api.Test;

class PlaceSelectionResponseTest {

    @Test
    void 추천_지역의_국가_시도_시군구를_응답한다() {
        SelectedPlace selected = new SelectedPlace(
                new PlaceDetails("park-1", "여의도한강공원", "KR", 37.528, 126.932, null, null),
                new SuggestedRegion(
                        new Area("KR", "대한민국"), new Area("11", "서울특별시"), new Area("11560", "영등포구")));

        PlaceSelectionResponse response = PlaceSelectionResponse.from(selected);

        assertThat(response.suggestedRegion().country()).isEqualTo(new RegionItemResponse("KR", "대한민국"));
        assertThat(response.suggestedRegion().province()).isEqualTo(new RegionItemResponse("11", "서울특별시"));
        assertThat(response.suggestedRegion().district()).isEqualTo(new RegionItemResponse("11560", "영등포구"));
        assertThat(response.manualRegionRequired()).isFalse();
    }

    @Test
    void 국가만_추천하면_시도와_시군구는_비운다() {
        SelectedPlace selected = new SelectedPlace(
                new PlaceDetails("tokyo-1", "도쿄타워", "JP", null, null, null, null),
                new SuggestedRegion(new Area("JP", "일본"), null, null));

        PlaceSelectionResponse response = PlaceSelectionResponse.from(selected);

        assertThat(response.suggestedRegion().country()).isEqualTo(new RegionItemResponse("JP", "일본"));
        assertThat(response.suggestedRegion().province()).isNull();
        assertThat(response.suggestedRegion().district()).isNull();
    }
}
