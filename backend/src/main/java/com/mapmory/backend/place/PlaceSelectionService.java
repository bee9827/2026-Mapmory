package com.mapmory.backend.place;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionResolver;
import com.mapmory.backend.travelrecord.dto.RegionDetailResponse;
import org.springframework.stereotype.Service;

@Service
public class PlaceSelectionService {

    private final PlaceLookupPort placeLookupPort;
    private final DistrictLocator districtLocator;
    private final RegionResolver regionResolver;

    public PlaceSelectionService(
            PlaceLookupPort placeLookupPort,
            DistrictLocator districtLocator,
            RegionResolver regionResolver
    ) {
        this.placeLookupPort = placeLookupPort;
        this.districtLocator = districtLocator;
        this.regionResolver = regionResolver;
    }

    public PlaceSelectionResponse select(String placeId) {
        PlaceDetails place = placeLookupPort.findById(placeId);
        Region region = suggestedRegion(place);
        return new PlaceSelectionResponse(
                place.placeId(),
                place.name(),
                place.countryCode(),
                region == null ? null : RegionDetailResponse.from(region),
                region == null,
                place.attribution(),
                place.attributionUrl()
        );
    }

    public Region suggestedRegion(PlaceDetails place) {
        if (place.countryCode() == null) {
            return null;
        }
        try {
            if (!"KR".equals(place.countryCode())) {
                return regionResolver.resolve(place.countryCode(), null, null);
            }
            return districtLocator.find(place.longitude(), place.latitude())
                    .map(match -> regionResolver.resolve("KR", match.provinceCode(), match.districtCode()))
                    .orElse(null);
        } catch (BusinessException ignored) {
            // DB에 국가/시군구가 없거나 경계가 오래된 경우, 사용자가 지역을 직접 선택한다.
            return null;
        }
    }
}
