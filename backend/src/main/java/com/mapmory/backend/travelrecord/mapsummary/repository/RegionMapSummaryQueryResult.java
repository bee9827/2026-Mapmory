package com.mapmory.backend.travelrecord.mapsummary.repository;

public interface RegionMapSummaryQueryResult {

    Long getRegionId();

    String getRegionCode();

    String getName();

    String getRegionType();

    long getRecordCount();
}
