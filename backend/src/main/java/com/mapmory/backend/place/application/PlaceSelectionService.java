package com.mapmory.backend.place.application;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.application.model.SelectedPlace;
import com.mapmory.backend.place.application.port.PlaceLookupPort;
import com.mapmory.backend.place.application.port.PlaceRateLimitPort;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionResolver;
import org.springframework.stereotype.Service;

@Service
public class PlaceSelectionService {

    private final PlaceLookupPort placeLookupPort;
    private final PlaceRateLimitPort rateLimitPort;
    private final RegionResolver regionResolver;
    private final KoreanAddressRegionMatcher addressRegionMatcher;

    public PlaceSelectionService(
            PlaceLookupPort placeLookupPort,
            PlaceRateLimitPort rateLimitPort,
            RegionResolver regionResolver,
            KoreanAddressRegionMatcher addressRegionMatcher
    ) {
        this.placeLookupPort = placeLookupPort;
        this.rateLimitPort = rateLimitPort;
        this.regionResolver = regionResolver;
        this.addressRegionMatcher = addressRegionMatcher;
    }

    public SelectedPlace select(Long memberId, String placeId, String sessionToken) {
        rateLimitPort.checkSelection(memberId);
        PlaceDetails place = placeLookupPort.findForSelection(placeId, sessionToken);
        return new SelectedPlace(place, suggestedRegion(place));
    }

    public Region suggestedRegion(PlaceDetails place) {
        if (place.countryCode() == null) {
            return null;
        }
        try {
            if (!"KR".equals(place.countryCode())) {
                return regionResolver.resolve(place.countryCode(), null, null);
            }
            return addressRegionMatcher.match(place.addressAreas()).orElse(null);
        } catch (BusinessException ignored) {
            // DB에 국가가 없으면 사용자가 지역을 직접 선택한다.
            return null;
        }
    }
}
