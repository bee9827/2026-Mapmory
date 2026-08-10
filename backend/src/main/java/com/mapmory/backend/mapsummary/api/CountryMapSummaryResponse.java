package com.mapmory.backend.mapsummary.api;

public record CountryMapSummaryResponse(
        String countryCode,
        String name,
        long count,
        int level
) {
}
