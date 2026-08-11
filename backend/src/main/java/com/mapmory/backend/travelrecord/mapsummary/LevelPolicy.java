package com.mapmory.backend.travelrecord.mapsummary;

public record LevelPolicy(long levelOneMaxCount, long levelTwoMaxCount) {

    public LevelPolicy {
        if (levelOneMaxCount < 1) {
            throw new IllegalArgumentException("levelOneMaxCount는 1 이상이어야 합니다.");
        }
        if (levelTwoMaxCount <= levelOneMaxCount) {
            throw new IllegalArgumentException("levelTwoMaxCount는 levelOneMaxCount보다 커야 합니다.");
        }
    }

    public static LevelPolicy of(long levelOneMaxCount, long levelTwoMaxCount) {
        return new LevelPolicy(levelOneMaxCount, levelTwoMaxCount);
    }

    public static LevelPolicy standard() {
        return LevelPolicy.of(2, 5);
    }

    public int levelFor(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("count는 0 이상이어야 합니다.");
        }
        if (count == 0) {
            return 0;
        }
        if (count <= levelOneMaxCount) {
            return 1;
        }
        if (count <= levelTwoMaxCount) {
            return 2;
        }
        return 3;
    }
}
