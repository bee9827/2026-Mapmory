package com.mapmory.backend.place.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.IntegrationTest;
import com.mapmory.backend.place.application.model.SuggestedRegion;
import com.mapmory.backend.place.application.model.SuggestedRegion.Area;
import com.mapmory.backend.region.Region;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class KoreanAddressRegionMatcherIntegrationTest extends IntegrationTest {

    @Autowired
    KoreanAddressRegionMatcher matcher;

    @Test
    void 주소로_찾은_시군구는_트랜잭션_밖에서도_국가와_시도까지_값으로_바꿀_수_있다() {
        Region district = matcher.match(List.of("서울특별시", "종로구")).orElseThrow();

        // 장소 선택 서비스는 트랜잭션 없이 추천 지역을 값으로 바꾼다(open-in-view: false).
        SuggestedRegion region = SuggestedRegion.from(district);

        assertThat(region).isEqualTo(new SuggestedRegion(
                new Area("KR", "대한민국"), new Area("11", "서울특별시"), new Area("11110", "종로구")));
    }
}
