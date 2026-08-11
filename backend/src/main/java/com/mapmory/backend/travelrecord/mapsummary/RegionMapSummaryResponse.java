package com.mapmory.backend.travelrecord.mapsummary;

import com.mapmory.backend.region.RegionType;

public record RegionMapSummaryResponse(
        Long regionId,
        String regionCode,
        RegionType regionType,
        String name,
        long count,
        int level
) {

    public static RegionMapSummaryResponse from(
            CountryMapSummaryQueryResult result,
            LevelPolicy levelPolicy
    ) {
        return new RegionMapSummaryResponse(
                result.getRegionId(),
                result.getRegionCode(),
                RegionType.COUNTRY,
                result.getName(),
                result.getRecordCount(),
                levelPolicy.levelFor(result.getRecordCount())
        );
    }
}
