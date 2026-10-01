package com.mapmory.backend.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionResolver;
import com.mapmory.backend.region.RegionType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceSelectionServiceTest {

    @Mock GeoapifyClient geoapifyClient;
    @Mock DistrictLocator districtLocator;
    @Mock RegionResolver regionResolver;
    @InjectMocks PlaceSelectionService service;

    @Test
    void 장소_좌표가_시군구에_포함되면_지역을_추천한다() {
        Region country = Region.of(null, null, "KR", "대한민국", RegionType.COUNTRY);
        Region province = Region.of(country, country, "11", "서울특별시", RegionType.PROVINCE);
        Region district = Region.of(province, country, "11560", "영등포구", RegionType.DISTRICT);
        when(geoapifyClient.findById("park-1"))
                .thenReturn(new PlaceDetails("park-1", "여의도한강공원", "KR", 37.528, 126.932));
        when(districtLocator.find(126.932, 37.528))
                .thenReturn(Optional.of(new DistrictLocator.DistrictMatch("11", "11560")));
        when(regionResolver.resolve("KR", "11", "11560")).thenReturn(district);

        PlaceSelectionResponse result = service.select("park-1");

        assertThat(result.manualRegionRequired()).isFalse();
        assertThat(result.suggestedRegion().district().code()).isEqualTo("11560");
    }

    @Test
    void 경계에서_지역을_찾지_못하면_직접_선택하도록_알린다() {
        when(geoapifyClient.findById("park-1"))
                .thenReturn(new PlaceDetails("park-1", "한강공원", "KR", 37.5, 127.0));
        when(districtLocator.find(127.0, 37.5)).thenReturn(Optional.empty());

        PlaceSelectionResponse result = service.select("park-1");

        assertThat(result.manualRegionRequired()).isTrue();
        assertThat(result.suggestedRegion()).isNull();
    }

    @Test
    void 제공자가_국가_코드를_주지_않으면_직접_선택하도록_알린다() {
        when(geoapifyClient.findById("place-1"))
                .thenReturn(new PlaceDetails("place-1", "섬", null, 0.0, 0.0));

        PlaceSelectionResponse result = service.select("place-1");

        assertThat(result.manualRegionRequired()).isTrue();
        assertThat(result.suggestedRegion()).isNull();
    }
}
