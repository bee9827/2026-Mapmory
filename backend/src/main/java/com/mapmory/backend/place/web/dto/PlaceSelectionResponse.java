package com.mapmory.backend.place.web.dto;

import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.application.model.SelectedPlace;
import com.mapmory.backend.place.application.model.SuggestedRegion;
import com.mapmory.backend.travelrecord.dto.RegionDetailResponse;
import com.mapmory.backend.travelrecord.dto.RegionItemResponse;

public record PlaceSelectionResponse(
        String placeId,
        String name,
        String countryCode,
        RegionDetailResponse suggestedRegion,
        boolean manualRegionRequired,
        String attribution,
        String attributionUrl
) {
    public static PlaceSelectionResponse from(SelectedPlace selected) {
        PlaceDetails place = selected.place();
        SuggestedRegion region = selected.suggestedRegion();
        return new PlaceSelectionResponse(
                place.placeId(), place.name(), place.countryCode(),
                region == null ? null : toResponse(region),
                region == null, place.attribution(), place.attributionUrl());
    }

    private static RegionDetailResponse toResponse(SuggestedRegion region) {
        return new RegionDetailResponse(item(region.country()), item(region.province()), item(region.district()));
    }

    private static RegionItemResponse item(SuggestedRegion.Area area) {
        return area == null ? null : new RegionItemResponse(area.code(), area.name());
    }
}
