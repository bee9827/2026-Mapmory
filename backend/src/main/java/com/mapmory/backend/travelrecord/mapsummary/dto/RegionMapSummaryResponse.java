package com.mapmory.backend.travelrecord.mapsummary.dto;

import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import com.mapmory.backend.travelrecord.mapsummary.policy.MapColorLevel;
import com.mapmory.backend.travelrecord.repository.RegionMapSummaryQueryResult;

public record RegionMapSummaryResponse(
        Long regionId,
        String regionCode,
        RegionType regionType,
        String name,
        long count,
        MapColorLevel level
) {

    public static RegionMapSummaryResponse from(
            RegionMapSummaryQueryResult result,
            RegionType regionType,
            LevelPolicy levelPolicy
    ) {
        return new RegionMapSummaryResponse(
                result.getRegionId(),
                result.getRegionCode(),
                regionType,
                result.getName(),
                result.getRecordCount(),
                levelPolicy.levelFor(result.getRecordCount())
        );
    }
}
