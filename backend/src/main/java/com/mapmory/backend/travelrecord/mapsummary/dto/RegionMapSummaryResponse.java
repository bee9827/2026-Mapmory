package com.mapmory.backend.travelrecord.mapsummary.dto;

import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import com.mapmory.backend.travelrecord.mapsummary.policy.MapColorLevel;
import com.mapmory.backend.travelrecord.mapsummary.repository.RegionMapSummaryQueryResult;

public record RegionMapSummaryResponse(
        Long regionId,
        String code,
        RegionType regionType,
        String name,
        long count,
        MapColorLevel level
) {

    public static RegionMapSummaryResponse from(
            RegionMapSummaryQueryResult result,
            LevelPolicy levelPolicy
    ) {
        return new RegionMapSummaryResponse(
                result.getRegionId(),
                result.getRegionCode(),
                RegionType.valueOf(result.getRegionType()),
                result.getName(),
                result.getRecordCount(),
                levelPolicy.levelFor(result.getRecordCount())
        );
    }
}
