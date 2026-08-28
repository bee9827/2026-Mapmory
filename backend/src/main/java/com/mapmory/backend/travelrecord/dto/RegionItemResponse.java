package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.region.Region;

public record RegionItemResponse(
        String code,
        String name
) {
    public static RegionItemResponse from(Region region) {
        return new RegionItemResponse(region.getRegionCode(), region.getName());
    }
}
