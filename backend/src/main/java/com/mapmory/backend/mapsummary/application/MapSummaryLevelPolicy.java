package com.mapmory.backend.mapsummary.application;

import org.springframework.stereotype.Component;

@Component
public record MapSummaryLevelPolicy() {

    public int levelOf(long count) {
        if (count <= 0) {
            return 0;
        }
        if (count <= 2) {
            return 1;
        }
        if (count <= 5) {
            return 2;
        }
        return 3;
    }
}
