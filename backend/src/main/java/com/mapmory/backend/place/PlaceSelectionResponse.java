package com.mapmory.backend.place;

import com.mapmory.backend.travelrecord.dto.RegionDetailResponse;

public record PlaceSelectionResponse(
        String placeId,
        String name,
        String countryCode,
        RegionDetailResponse suggestedRegion,
        boolean manualRegionRequired,
        String attribution,
        String attributionUrl
) {
    public PlaceSelectionResponse(
            String placeId, String name, String countryCode,
            RegionDetailResponse suggestedRegion, boolean manualRegionRequired
    ) {
        this(placeId, name, countryCode, suggestedRegion, manualRegionRequired,
                PlaceAttribution.OSM_TEXT, PlaceAttribution.OSM_URL);
    }
}
