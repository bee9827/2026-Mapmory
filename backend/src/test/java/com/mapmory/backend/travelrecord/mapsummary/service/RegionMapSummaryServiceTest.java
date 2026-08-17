package com.mapmory.backend.travelrecord.mapsummary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.region.exception.RegionErrorCode;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import com.mapmory.backend.travelrecord.mapsummary.policy.MapColorLevel;
import com.mapmory.backend.travelrecord.mapsummary.repository.RegionMapSummaryQueryResult;
import com.mapmory.backend.travelrecord.mapsummary.repository.RegionMapSummaryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Region 지도 요약 서비스")
class RegionMapSummaryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private RegionMapSummaryRepository regionMapSummaryRepository;

    private final LevelPolicy levelPolicy = LevelPolicy.standard();

    @Nested
    @DisplayName("Region 요약을 조회할 때")
    class GetSummaries {

        @Test
        @DisplayName("부모 ID가 없으면 루트 국가별 요약을 반환한다")
        void returnsRootCountrySummaries() {
            RegionMapSummaryService service = service();
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(regionMapSummaryRepository.findRegionMapSummaries(10L, null))
                    .thenReturn(List.of(result(1L, "KR", "대한민국", "COUNTRY", 3L)));

            List<RegionMapSummaryResponse> responses = service.getSummaries(10L, null);

            assertThat(responses).containsExactly(new RegionMapSummaryResponse(
                    1L,
                    "KR",
                    RegionType.COUNTRY,
                    "대한민국",
                    3L,
                    MapColorLevel.MEDIUM
            ));
            verify(regionRepository, never()).existsById(org.mockito.ArgumentMatchers.anyLong());
        }

        @DisplayName("부모 ID가 있으면 직속 하위 Region 타입의 요약을 반환한다")
        @ParameterizedTest(name = "부모 {0}의 자식 타입은 {1}이다")
        @CsvSource({
                "1, PROVINCE, 15, 49, 제주특별자치도",
                "15, DISTRICT, 150, 50110, 제주시"
        })
        void returnsDirectChildSummaries(
                long parentRegionId,
                String regionType,
                long childRegionId,
                String regionCode,
                String name
        ) {
            RegionMapSummaryService service = service();
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(regionRepository.existsById(parentRegionId)).thenReturn(true);
            when(regionMapSummaryRepository.findRegionMapSummaries(10L, parentRegionId))
                    .thenReturn(List.of(result(childRegionId, regionCode, name, regionType, 1L)));

            List<RegionMapSummaryResponse> responses = service.getSummaries(10L, parentRegionId);

            assertThat(responses).containsExactly(new RegionMapSummaryResponse(
                    childRegionId,
                    regionCode,
                    RegionType.valueOf(regionType),
                    name,
                    1L,
                    MapColorLevel.LOW
            ));
        }

        @Test
        @DisplayName("회원을 찾을 수 없으면 Region과 기록을 조회하지 않는다")
        void rejectsUnknownMember() {
            RegionMapSummaryService service = service();
            when(memberRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.getSummaries(999L, 1L))
                    .isInstanceOfSatisfying(BusinessException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
            verify(regionRepository, never()).existsById(1L);
            verify(regionMapSummaryRepository, never()).findRegionMapSummaries(999L, 1L);
        }

        @Test
        @DisplayName("부모 Region을 찾을 수 없으면 기록을 조회하지 않는다")
        void rejectsUnknownParentRegion() {
            RegionMapSummaryService service = service();
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(regionRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.getSummaries(10L, 999L))
                    .isInstanceOfSatisfying(BusinessException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_FOUND));
            verify(regionMapSummaryRepository, never()).findRegionMapSummaries(10L, 999L);
        }
    }

    private RegionMapSummaryService service() {
        return new RegionMapSummaryService(
                memberRepository,
                regionRepository,
                regionMapSummaryRepository,
                levelPolicy
        );
    }

    private static RegionMapSummaryQueryResult result(
            Long regionId,
            String regionCode,
            String name,
            String regionType,
            long recordCount
    ) {
        return new RegionMapSummaryQueryResult() {
            @Override
            public Long getRegionId() {
                return regionId;
            }

            @Override
            public String getRegionCode() {
                return regionCode;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getRegionType() {
                return regionType;
            }

            @Override
            public long getRecordCount() {
                return recordCount;
            }
        };
    }
}
