package com.mapmory.backend.travelrecord.mapsummary.policy;

public enum MapColorLevel {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int value;

    MapColorLevel(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
