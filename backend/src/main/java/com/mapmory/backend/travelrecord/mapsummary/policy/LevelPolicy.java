package com.mapmory.backend.travelrecord.mapsummary.policy;

public record LevelPolicy(long lowMaxRecordCount, long middleMaxRecordCount) {

    private static final long NO_RECORD_COUNT = 0;
    private static final long MINIMUM_LOW_MAX_RECORD_COUNT = 1;
    private static final long DEFAULT_LOW_MAX_RECORD_COUNT = 2;
    private static final long DEFAULT_MIDDLE_MAX_RECORD_COUNT = 5;

    public LevelPolicy {
        if (lowMaxRecordCount < MINIMUM_LOW_MAX_RECORD_COUNT) {
            throw new IllegalArgumentException("lowMaxRecordCount는 1 이상이어야 합니다.");
        }
        if (middleMaxRecordCount <= lowMaxRecordCount) {
            throw new IllegalArgumentException("middleMaxRecordCount는 lowMaxRecordCount보다 커야 합니다.");
        }
    }

    public static LevelPolicy of(long lowMaxRecordCount, long middleMaxRecordCount) {
        return new LevelPolicy(lowMaxRecordCount, middleMaxRecordCount);
    }

    public static LevelPolicy standard() {
        return LevelPolicy.of(DEFAULT_LOW_MAX_RECORD_COUNT, DEFAULT_MIDDLE_MAX_RECORD_COUNT);
    }

    public MapColorLevel levelFor(long count) {
        if (count < NO_RECORD_COUNT) {
            throw new IllegalArgumentException("count는 0 이상이어야 합니다.");
        }
        if (count == NO_RECORD_COUNT) {
            return MapColorLevel.NONE;
        }
        if (count <= lowMaxRecordCount) {
            return MapColorLevel.LOW;
        }
        if (count <= middleMaxRecordCount) {
            return MapColorLevel.MIDDLE;
        }
        return MapColorLevel.HIGH;
    }
}
