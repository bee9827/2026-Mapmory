package com.mapmory.backend.travelrecord.mapsummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.MemberErrorCode;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("국가별 지도 요약 서비스")
class CountryMapSummaryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TravelRecordRepository travelRecordRepository;

    private final LevelPolicy levelPolicy = LevelPolicy.standard();

    @Nested
    @DisplayName("국가별 요약을 조회할 때")
    class GetCountrySummaries {

        @Test
        @DisplayName("국가별 기록 수와 색상 단계를 반환한다")
        void returnsCountrySummaries() {
            CountryMapSummaryService service = service();
            when(memberRepository.existsById(10L)).thenReturn(true);
            when(travelRecordRepository.findCountryMapSummaries(10L))
                    .thenReturn(List.of(result(1L, "KR", "대한민국", 3L)));

            List<RegionMapSummaryResponse> responses = service.getCountrySummaries(10L);

            assertThat(responses).containsExactly(new RegionMapSummaryResponse(
                    1L,
                    "KR",
                    RegionType.COUNTRY,
                    "대한민국",
                    3L,
                    2
            ));
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 404 업무 예외를 던진다")
        void rejectsUnknownMember() {
            CountryMapSummaryService service = service();
            when(memberRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.getCountrySummaries(999L))
                    .isInstanceOfSatisfying(BusinessException.class, exception ->
                            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
            verify(travelRecordRepository, never()).findCountryMapSummaries(999L);
        }
    }

    private CountryMapSummaryService service() {
        return new CountryMapSummaryService(memberRepository, travelRecordRepository, levelPolicy);
    }

    private static CountryMapSummaryQueryResult result(
            Long regionId,
            String regionCode,
            String name,
            long recordCount
    ) {
        return new CountryMapSummaryQueryResult() {
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
