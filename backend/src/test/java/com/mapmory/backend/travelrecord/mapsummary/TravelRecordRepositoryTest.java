package com.mapmory.backend.travelrecord.mapsummary;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.support.MySqlTestContainerConfig;
import com.mapmory.backend.travelrecord.TravelRecord;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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

        @Test
        @DisplayName("국가와 하위 Region의 현재 회원 기록을 합산하고 0건인 국가도 반환한다")
        void aggregatesCurrentMemberRecordsAndIncludesEmptyCountries() {
            Member member = persist(Member.of("회원", UUID.randomUUID()));
            Member otherMember = persist(Member.of("다른 회원", UUID.randomUUID()));
            Region visitedCountry = persist(Region.of(null, null, "X1", "방문 국가", RegionType.COUNTRY));
            Region emptyCountry = persist(Region.of(null, null, "X2", "미방문 국가", RegionType.COUNTRY));
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

            List<CountryMapSummaryQueryResult> results =
                    travelRecordRepository.findCountryMapSummaries(member.getId());

            assertThat(results)
                    .filteredOn(result -> result.getRegionCode().startsWith("X"))
                    .extracting(
                            CountryMapSummaryQueryResult::getRegionCode,
                            CountryMapSummaryQueryResult::getRecordCount
                    )
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple("X1", 2L),
                            org.assertj.core.groups.Tuple.tuple("X2", 0L)
                    );
            assertThat(emptyCountry.getId()).isNotNull();
        }
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
