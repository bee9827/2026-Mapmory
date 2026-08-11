package com.mapmory.backend.travelrecord.repository;

public interface CountryMapSummaryQueryResult {

    Long getRegionId();

    String getRegionCode();

    String getName();

    long getRecordCount();
}
