package com.mapmory.backend.travelrecord.repository;

public interface RegionMapSummaryQueryResult {

    Long getRegionId();

    String getRegionCode();

    String getName();

    long getRecordCount();
}
