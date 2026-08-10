package com.mapmory.backend.mapsummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.country.CountryRepository;
import com.mapmory.backend.location.Location;
import com.mapmory.backend.location.LocationRepository;
import com.mapmory.backend.mapsummary.api.CountryMapSummaryResponse;
import com.mapmory.backend.mapsummary.api.RegionMapSummaryResponse;
import com.mapmory.backend.mapsummary.application.MapSummaryLevelPolicy;
import com.mapmory.backend.mapsummary.application.MapSummaryService;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.travelrecord.CountrySummaryProjection;
import com.mapmory.backend.travelrecord.RegionSummaryProjection;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("지도 요약 서비스")
class MapSummaryServiceTest {

    private MemberRepository memberRepository;
    private CountryRepository countryRepository;
    private LocationRepository locationRepository;
    private TravelRecordRepository travelRecordRepository;
    private MapSummaryService mapSummaryService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        countryRepository = mock(CountryRepository.class);
        locationRepository = mock(LocationRepository.class);
        travelRecordRepository = mock(TravelRecordRepository.class);
        mapSummaryService = new MapSummaryService(
                memberRepository,
                countryRepository,
                locationRepository,
                travelRecordRepository,
                new MapSummaryLevelPolicy()
        );
    }

    @Nested
    @DisplayName("6.1 세계 지도 국가별 집계")
    class CountrySummary {

        @Test
        @DisplayName("Repository 집계 결과에 level을 계산해 응답으로 변환한다")
        void mapsCountrySummariesAndLevels() {
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(travelRecordRepository.summarizeCountries(10L)).thenReturn(List.of(
                    new CountryProjection("JP", "일본", 2),
                    new CountryProjection("KR", "대한민국", 6)
            ));

            assertThat(mapSummaryService.summarizeCountries(10L)).containsExactly(
                    new CountryMapSummaryResponse("JP", "일본", 2, 1),
                    new CountryMapSummaryResponse("KR", "대한민국", 6, 3)
            );
            verify(travelRecordRepository).summarizeCountries(10L);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 Repository 집계 쿼리를 실행하지 않는다")
        void rejectsUnknownMemberBeforeQuery() {
            when(memberRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> mapSummaryService.summarizeCountries(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(MapSummaryErrorCode.MEMBER_NOT_FOUND);
            verifyNoInteractions(travelRecordRepository);
        }
    }

    @Nested
    @DisplayName("6.2 국가의 최상위 지역별 집계")
    class TopLevelRegionSummary {

        @Test
        @DisplayName("회원과 국가를 검증한 뒤 최상위 지역 집계를 요청한다")
        void validatesAndMapsTopLevelRegions() {
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(countryRepository.existsByCode("KR")).thenReturn(true);
            when(travelRecordRepository.summarizeTopLevelRegions(10L, "KR"))
                    .thenReturn(List.of(new RegionProjection("11", "서울특별시", 5)));

            assertThat(mapSummaryService.summarizeTopLevelRegions(10L, "KR"))
                    .containsExactly(new RegionMapSummaryResponse("11", "서울특별시", 5, 2));
            verify(travelRecordRepository).summarizeTopLevelRegions(10L, "KR");
        }

        @Test
        @DisplayName("존재하지 않는 국가이면 Repository 집계 쿼리를 실행하지 않는다")
        void rejectsUnknownCountryBeforeQuery() {
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(countryRepository.existsByCode("US")).thenReturn(false);

            assertThatThrownBy(() -> mapSummaryService.summarizeTopLevelRegions(10L, "US"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(MapSummaryErrorCode.COUNTRY_NOT_FOUND);
            verifyNoInteractions(travelRecordRepository);
        }
    }

    @Nested
    @DisplayName("6.3 지역의 직속 하위 지역별 집계")
    class ChildRegionSummary {

        @Test
        @DisplayName("검증한 상위 지역 ID로 직속 하위 지역 집계를 요청한다")
        void validatesParentAndMapsChildren() {
            Location parent = mock(Location.class);
            when(parent.getId()).thenReturn(100L);
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(countryRepository.existsByCode("KR")).thenReturn(true);
            when(locationRepository.findByCountryCodeAndRegionCode("KR", "11"))
                    .thenReturn(Optional.of(parent));
            when(travelRecordRepository.summarizeChildren(10L, "KR", 100L))
                    .thenReturn(List.of(new RegionProjection("11110", "종로구", 3)));

            assertThat(mapSummaryService.summarizeChildren(10L, "KR", "11"))
                    .containsExactly(new RegionMapSummaryResponse("11110", "종로구", 3, 2));
            verify(travelRecordRepository).summarizeChildren(10L, "KR", 100L);
        }

        @Test
        @DisplayName("상위 지역이 없으면 Repository 집계 쿼리를 실행하지 않는다")
        void rejectsUnknownParentBeforeQuery() {
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(countryRepository.existsByCode("KR")).thenReturn(true);
            when(locationRepository.findByCountryCodeAndRegionCode("KR", "99"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> mapSummaryService.summarizeChildren(10L, "KR", "99"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(exception -> ((BusinessException) exception).getErrorCode())
                    .isEqualTo(MapSummaryErrorCode.LOCATION_NOT_FOUND);
            verifyNoInteractions(travelRecordRepository);
        }
    }

    private record CountryProjection(
            String countryCode,
            String name,
            long count
    ) implements CountrySummaryProjection {

        @Override
        public String getCountryCode() {
            return countryCode;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getCount() {
            return count;
        }
    }

    private record RegionProjection(
            String locationCode,
            String name,
            long count
    ) implements RegionSummaryProjection {

        @Override
        public String getLocationCode() {
            return locationCode;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getCount() {
            return count;
        }
    }
}
