package com.mapmory.backend.place;

public record PlaceDetails(
        String placeId, String name, String countryCode, double latitude, double longitude,
        String attribution, String attributionUrl
) {
    public PlaceDetails(String placeId, String name, String countryCode, double latitude, double longitude) {
        this(placeId, name, countryCode, latitude, longitude,
                PlaceAttribution.OSM_TEXT, PlaceAttribution.OSM_URL);
    }
}
