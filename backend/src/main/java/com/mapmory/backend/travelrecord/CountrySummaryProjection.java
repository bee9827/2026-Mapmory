package com.mapmory.backend.travelrecord;

public interface CountrySummaryProjection {

    String getCountryCode();

    String getName();

    long getCount();
}
