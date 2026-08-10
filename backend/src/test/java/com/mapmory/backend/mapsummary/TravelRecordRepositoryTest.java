package com.mapmory.backend.mapsummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.mapmory.backend.country.Country;
import com.mapmory.backend.country.CountryRepository;
import com.mapmory.backend.location.Location;
import com.mapmory.backend.location.LocationRepository;
import com.mapmory.backend.location.LocationType;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.travelrecord.CountrySummaryProjection;
import com.mapmory.backend.travelrecord.RegionSummaryProjection;
import com.mapmory.backend.travelrecord.TravelRecord;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@DisplayName("여행 기록 지도 요약 Repository")
class TravelRecordRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TravelRecordRepository travelRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;
    private Member member;
    private Country korea;
    private Location seoul;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.of("맵모리", UUID.randomUUID()));
        Member anotherMember = memberRepository.save(Member.of("다른 회원", UUID.randomUUID()));

        korea = countryRepository.save(Country.of("KR", "대한민국"));
        Country japan = countryRepository.save(Country.of("JP", "일본"));

        seoul = locationRepository.save(Location.of(
                korea, null, "11", "서울특별시", LocationType.PROVINCE
        ));
        Location busan = locationRepository.save(Location.of(
                korea, null, "26", "부산광역시", LocationType.PROVINCE
        ));
        Location jongno = locationRepository.save(Location.of(
                korea, seoul, "11110", "종로구", LocationType.DISTRICT
        ));
        Location gangnam = locationRepository.save(Location.of(
                korea, seoul, "11680", "강남구", LocationType.DISTRICT
        ));
        Location haeundae = locationRepository.save(Location.of(
                korea, busan, "26350", "해운대구", LocationType.DISTRICT
        ));

        saveRecords(member, korea, jongno, 3);
        saveRecords(member, korea, gangnam, 2);
        saveRecords(member, korea, haeundae, 1);
        saveRecords(member, japan, null, 6);
        saveRecords(anotherMember, korea, jongno, 10);

        entityManager.flush();
        entityManager.clear();
        statistics = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();
        statistics.clear();
    }

    @Nested
    @DisplayName("6.1 세계 지도 국가별 집계")
    class CountrySummary {

        @Test
        @DisplayName("현재 회원의 기록만 한 번의 쿼리로 국가별 집계한다")
        void summarizesCountriesWithSingleQuery() {
            List<CountrySummaryProjection> result =
                    travelRecordRepository.summarizeCountries(member.getId());

            assertThat(result)
                    .extracting(
                            CountrySummaryProjection::getCountryCode,
                            CountrySummaryProjection::getName,
                            CountrySummaryProjection::getCount
                    )
                    .containsExactly(
                            tuple("JP", "일본", 6L),
                            tuple("KR", "대한민국", 6L)
                    );
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("6.2 국가의 최상위 지역별 집계")
    class TopLevelRegionSummary {

        @Test
        @DisplayName("시군구 기록을 한 번의 쿼리로 직속 시도에 집계한다")
        void summarizesTopLevelRegionsWithSingleQuery() {
            List<RegionSummaryProjection> result =
                    travelRecordRepository.summarizeTopLevelRegions(member.getId(), "KR");

            assertThat(result)
                    .extracting(
                            RegionSummaryProjection::getLocationCode,
                            RegionSummaryProjection::getName,
                            RegionSummaryProjection::getCount
                    )
                    .containsExactly(
                            tuple("11", "서울특별시", 5L),
                            tuple("26", "부산광역시", 1L)
                    );
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("6.3 지역의 직속 하위 지역별 집계")
    class ChildRegionSummary {

        @Test
        @DisplayName("상위 지역의 직속 자식만 한 번의 쿼리로 집계한다")
        void summarizesChildrenWithSingleQuery() {
            List<RegionSummaryProjection> result = travelRecordRepository.summarizeChildren(
                    member.getId(),
                    "KR",
                    seoul.getId()
            );

            assertThat(result)
                    .extracting(
                            RegionSummaryProjection::getLocationCode,
                            RegionSummaryProjection::getName,
                            RegionSummaryProjection::getCount
                    )
                    .containsExactly(
                            tuple("11110", "종로구", 3L),
                            tuple("11680", "강남구", 2L)
                    );
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("공통 영속성")
    class Persistence {

        @Test
        @DisplayName("영속화된 엔티티에 생성 시각과 수정 시각을 기록한다")
        void populatesBaseEntityTimestamps() {
            assertThat(korea.getCreatedAt()).isNotNull();
            assertThat(korea.getUpdatedAt()).isNotNull();
        }
    }

    private void saveRecords(
            Member owner,
            Country country,
            Location location,
            int count
    ) {
        List<TravelRecord> records = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            records.add(TravelRecord.of(
                    owner,
                    country,
                    location,
                    "기록 " + index,
                    "",
                    null,
                    null
            ));
        }
        travelRecordRepository.saveAll(records);
    }
}
