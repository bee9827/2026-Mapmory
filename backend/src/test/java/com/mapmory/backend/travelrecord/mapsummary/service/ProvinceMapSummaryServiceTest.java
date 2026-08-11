package com.mapmory.backend.travelrecord.mapsummary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import com.mapmory.backend.member.repository.MemberRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.region.exception.RegionErrorCode;
import com.mapmory.backend.region.repository.RegionRepository;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import com.mapmory.backend.travelrecord.mapsummary.policy.MapColorLevel;
import com.mapmory.backend.travelrecord.repository.RegionMapSummaryQueryResult;
import com.mapmory.backend.travelrecord.repository.TravelRecordRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("시도별 지도 요약 서비스")
class ProvinceMapSummaryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private TravelRecordRepository travelRecordRepository;

    private final LevelPolicy levelPolicy = LevelPolicy.standard();

    @Nested
    @DisplayName("시도별 요약을 조회할 때")
    class GetProvinceSummaries {

        @Test
        @DisplayName("국가의 시도별 기록 수와 색상 단계를 반환한다")
        void returnsProvinceSummaries() {
            ProvinceMapSummaryService service = service();
            Region country = Region.of(null, null, "KR", "대한민국", RegionType.COUNTRY);
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(regionRepository.findById(1L)).thenReturn(Optional.of(country));
            when(travelRecordRepository.findProvinceMapSummaries(10L, 1L))
                    .thenReturn(List.of(result(15L, "49", "제주특별자치도", 3L)));

            List<RegionMapSummaryResponse> responses = service.getProvinceSummaries(10L, 1L);

            assertThat(responses).containsExactly(new RegionMapSummaryResponse(
                    15L,
                    "49",
                    RegionType.PROVINCE,
                    "제주특별자치도",
                    3L,
                    MapColorLevel.MEDIUM
            ));
        }

        @Test
        @DisplayName("회원을 찾을 수 없으면 Region과 기록을 조회하지 않는다")
        void rejectsUnknownMember() {
            ProvinceMapSummaryService service = service();
            when(memberRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.getProvinceSummaries(999L, 1L))
                    .isInstanceOfSatisfying(BusinessException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
            verify(regionRepository, never()).findById(1L);
            verify(travelRecordRepository, never()).findProvinceMapSummaries(999L, 1L);
        }

        @Test
        @DisplayName("상위 Region을 찾을 수 없으면 기록을 조회하지 않는다")
        void rejectsUnknownParentRegion() {
            ProvinceMapSummaryService service = service();
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(regionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProvinceSummaries(10L, 999L))
                    .isInstanceOfSatisfying(BusinessException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_FOUND));
            verify(travelRecordRepository, never()).findProvinceMapSummaries(10L, 999L);
        }

        @Test
        @DisplayName("상위 Region이 국가가 아니면 기록을 조회하지 않는다")
        void rejectsNonCountryParentRegion() {
            ProvinceMapSummaryService service = service();
            Region province = Region.of(null, null, "49", "제주특별자치도", RegionType.PROVINCE);
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(regionRepository.findById(15L)).thenReturn(Optional.of(province));

            assertThatThrownBy(() -> service.getProvinceSummaries(10L, 15L))
                    .isInstanceOfSatisfying(BusinessException.class, exception ->
                            assertThat(exception.getErrorCode())
                                    .isEqualTo(RegionErrorCode.INVALID_PARENT_REGION_TYPE));
            verify(travelRecordRepository, never()).findProvinceMapSummaries(10L, 15L);
        }
    }

    private ProvinceMapSummaryService service() {
        return new ProvinceMapSummaryService(
                memberRepository,
                regionRepository,
                travelRecordRepository,
                levelPolicy
        );
    }

    private static RegionMapSummaryQueryResult result(
            Long regionId,
            String regionCode,
            String name,
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
            public long getRecordCount() {
                return recordCount;
            }
        };
    }
}
