package com.mapmory.backend.travelrecord.mapsummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("지도 색상 단계 정책")
class LevelPolicyTest {

    private final LevelPolicy levelPolicy = LevelPolicy.standard();

    @Nested
    @DisplayName("기록 수를 색상 단계로 변환할 때")
    class CalculateLevel {

        @DisplayName("경계값에 맞는 단계를 반환한다")
        @ParameterizedTest(name = "기록 수 {0}건은 {1} 단계다")
        @CsvSource({
                "0, NONE",
                "1, LOW",
                "2, LOW",
                "3, MEDIUM",
                "5, MEDIUM",
                "6, HIGH"
        })
        void returnsLevelForBoundary(long count, MapColorLevel expectedLevel) {
            assertThat(levelPolicy.levelFor(count)).isEqualTo(expectedLevel);
        }

        @Test
        @DisplayName("음수 기록 수는 허용하지 않는다")
        void rejectsNegativeCount() {
            assertThatThrownBy(() -> levelPolicy.levelFor(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("count는 0 이상이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("정책을 생성할 때")
    class CreatePolicy {

        @Test
        @DisplayName("LOW 최대 기록 수는 1 이상이어야 한다")
        void rejectsInvalidLevelOneMaximum() {
            assertThatThrownBy(() -> LevelPolicy.of(0, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("MEDIUM 최대 기록 수는 LOW 최대 기록 수보다 커야 한다")
        void rejectsInvalidLevelTwoMaximum() {
            assertThatThrownBy(() -> LevelPolicy.of(2, 2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
