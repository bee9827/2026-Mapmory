package com.mapmory.backend.mapsummary.api;

public record RegionMapSummaryResponse(
        String locationCode,
        String name,
        long count,
        int level
) {
}
