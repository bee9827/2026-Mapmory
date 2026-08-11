package com.mapmory.backend.travelrecord.mapsummary;

public interface CountryMapSummaryQueryResult {

    Long getRegionId();

    String getRegionCode();

    String getName();

    long getRecordCount();
}
