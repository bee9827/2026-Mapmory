package com.mapmory.backend.place;

public record PlaceCandidate(
        String placeId, String name, String address,
        String attribution, String attributionUrl
) {
    public PlaceCandidate(String placeId, String name, String address) {
        this(placeId, name, address, PlaceAttribution.OSM_TEXT, PlaceAttribution.OSM_URL);
    }
}
