package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.dto.TravelRecordRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TravelRecordServiceTest {

    @Mock
    private TravelRecordRepository travelRecordRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private TravelRecordService travelRecordService;

    @Test
    void createsCountryTravelRecord() {
        Member member = mock(Member.class);
        Region japan = mock(Region.class);
        TravelRecordRequest request = new TravelRecordRequest(
                "JP",
                null,
                null,
                "일본 여행",
                "",
                LocalDate.of(2026, 8, 11),
                null,
                List.of()
        );

        when(memberRepository.getReferenceById(10L)).thenReturn(member);
        when(regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                "JP"
        )).thenReturn(Optional.of(japan));
        when(travelRecordRepository.save(any(TravelRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TravelRecord result = travelRecordService.create(10L, request);

        assertThat(result).isNotNull();
        verify(memberRepository).getReferenceById(10L);
        verify(regionRepository).findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                "JP"
        );
        verify(travelRecordRepository).save(any(TravelRecord.class));
    }
}
