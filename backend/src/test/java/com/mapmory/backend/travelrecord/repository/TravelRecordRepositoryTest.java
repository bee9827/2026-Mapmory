package com.mapmory.backend.travelrecord.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.support.MySqlTestContainerConfig;
import com.mapmory.backend.travelrecord.TravelRecord;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(MySqlTestContainerConfig.class)
@DisplayName("여행 기록 Repository")
class TravelRecordRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TravelRecordRepository travelRecordRepository;

    @Nested
    @DisplayName("국가별 지도 요약을 조회할 때")
    class FindCountryMapSummaries {

        private Member member;
        private Region visitedCountry;
        private Region emptyCountry;

        @BeforeEach
        void setUp() {
            member = persist(Member.of("회원", UUID.randomUUID()));
            Member otherMember = persist(Member.of("다른 회원", UUID.randomUUID()));
            visitedCountry = persist(Region.of(null, null, "X1", "방문 국가", RegionType.COUNTRY));
            emptyCountry = persist(Region.of(null, null, "X2", "미방문 국가", RegionType.COUNTRY));
            Region province = persist(Region.of(
                    visitedCountry,
                    visitedCountry,
                    "X1-P1",
                    "방문 국가의 지역",
                    RegionType.PROVINCE
            ));
            Region district = persist(Region.of(
                    province,
                    visitedCountry,
                    "X1-D1",
                    "방문 국가의 세부 지역",
                    RegionType.DISTRICT
            ));
            persist(record(member, visitedCountry, "국가 기록"));
            persist(record(member, district, "세부 지역 기록"));
            persist(record(otherMember, district, "다른 회원 기록"));
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("국가와 하위 Region의 기록을 합산하고 0건 국가의 Projection도 반환한다")
        void aggregatesRecordsAndReturnsEmptyCountryProjection() {
            List<RegionMapSummaryQueryResult> results =
                    travelRecordRepository.findCountryMapSummaries(member.getId());

            assertThat(results)
                    .filteredOn(result -> Set.of("X1", "X2").contains(result.getRegionCode()))
                    .extracting(
                            RegionMapSummaryQueryResult::getRegionId,
                            RegionMapSummaryQueryResult::getRegionCode,
                            RegionMapSummaryQueryResult::getName,
                            RegionMapSummaryQueryResult::getRecordCount
                    )
                    .containsExactly(
                            tuple(visitedCountry.getId(), "X1", "방문 국가", 2L),
                            tuple(emptyCountry.getId(), "X2", "미방문 국가", 0L)
                    );
        }

        @Test
        @DisplayName("다른 회원의 기록과 국가가 아닌 Region 결과는 포함하지 않는다")
        void excludesOtherMemberRecordsAndNonCountryRows() {
            List<RegionMapSummaryQueryResult> results =
                    travelRecordRepository.findCountryMapSummaries(member.getId());

            assertThat(results)
                    .extracting(RegionMapSummaryQueryResult::getRegionCode)
                    .doesNotContain("X1-P1", "X1-D1");
            assertThat(resultFor(results, "X1").getRecordCount())
                    .as("다른 회원의 기록은 국가 집계에 포함하지 않는다")
                    .isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("시도별 지도 요약을 조회할 때")
    class FindProvinceMapSummaries {

        private Member member;
        private Region country;
        private Region visitedProvince;
        private Region emptyProvince;

        @BeforeEach
        void setUp() {
            member = persist(Member.of("회원", UUID.randomUUID()));
            Member otherMember = persist(Member.of("다른 회원", UUID.randomUUID()));
            country = persist(Region.of(null, null, "Y1", "조회 국가", RegionType.COUNTRY));
            visitedProvince = persist(Region.of(
                    country,
                    country,
                    "Y1-P1",
                    "방문 시도",
                    RegionType.PROVINCE
            ));
            emptyProvince = persist(Region.of(
                    country,
                    country,
                    "Y1-P2",
                    "미방문 시도",
                    RegionType.PROVINCE
            ));
            Region district = persist(Region.of(
                    visitedProvince,
                    country,
                    "Y1-D1",
                    "방문 세부 지역",
                    RegionType.DISTRICT
            ));
            persist(record(member, visitedProvince, "시도 기록"));
            persist(record(member, district, "세부 지역 기록"));
            persist(record(otherMember, district, "다른 회원 기록"));
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("시도와 직속 하위 Region의 기록을 합산하고 0건 시도의 Projection도 반환한다")
        void aggregatesRecordsAndReturnsEmptyProvinceProjection() {
            List<RegionMapSummaryQueryResult> results =
                    travelRecordRepository.findProvinceMapSummaries(member.getId(), country.getId());

            assertThat(results)
                    .extracting(
                            RegionMapSummaryQueryResult::getRegionId,
                            RegionMapSummaryQueryResult::getRegionCode,
                            RegionMapSummaryQueryResult::getName,
                            RegionMapSummaryQueryResult::getRecordCount
                    )
                    .containsExactly(
                            tuple(visitedProvince.getId(), "Y1-P1", "방문 시도", 2L),
                            tuple(emptyProvince.getId(), "Y1-P2", "미방문 시도", 0L)
                    );
        }

        @Test
        @DisplayName("다른 회원의 기록과 시도가 아닌 Region 결과는 포함하지 않는다")
        void excludesOtherMemberRecordsAndNonProvinceRows() {
            List<RegionMapSummaryQueryResult> results =
                    travelRecordRepository.findProvinceMapSummaries(member.getId(), country.getId());

            assertThat(results)
                    .extracting(RegionMapSummaryQueryResult::getRegionCode)
                    .doesNotContain("Y1-D1");
            assertThat(resultFor(results, "Y1-P1").getRecordCount())
                    .as("다른 회원의 기록은 시도 집계에 포함하지 않는다")
                    .isEqualTo(2L);
        }
    }

    private RegionMapSummaryQueryResult resultFor(
            List<RegionMapSummaryQueryResult> results,
            String regionCode
    ) {
        return results.stream()
                .filter(result -> result.getRegionCode().equals(regionCode))
                .findFirst()
                .orElseThrow();
    }

    private TravelRecord record(Member member, Region region, String title) {
        return TravelRecord.of(
                member,
                region,
                title,
                "내용",
                LocalDate.of(2026, 8, 11),
                null
        );
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
}
