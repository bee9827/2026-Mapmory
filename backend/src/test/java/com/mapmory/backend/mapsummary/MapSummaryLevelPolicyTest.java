package com.mapmory.backend.mapsummary;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.mapsummary.application.MapSummaryLevelPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("지도 요약 색상 단계 정책")
class MapSummaryLevelPolicyTest {

    private final MapSummaryLevelPolicy policy = new MapSummaryLevelPolicy();

    @Nested
    @DisplayName("기록 수 경계값")
    class RecordCountBoundary {

        @DisplayName("기록 수에 따라 0~3 단계의 level을 계산한다")
        @ParameterizedTest(name = "기록 {0}개 → level {1}")
        @CsvSource({
                "0, 0",
                "1, 1",
                "2, 1",
                "3, 2",
                "5, 2",
                "6, 3",
                "100, 3"
        })
        void calculatesLevelFromRecordCount(long count, int expectedLevel) {
            assertThat(policy.levelOf(count)).isEqualTo(expectedLevel);
        }
    }
}
