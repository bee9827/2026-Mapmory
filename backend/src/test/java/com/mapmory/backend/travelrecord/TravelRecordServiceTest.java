package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    void findsTravelRecordsWithPagination() {
        TravelRecord travelRecord = mock(TravelRecord.class);

        Page<TravelRecord> expected = new PageImpl<>(
                List.of(travelRecord),
                PageRequest.of(0, 20),
                1
        );

        when(travelRecordRepository.findByMemberId(
                eq(10L),
                any(Pageable.class)
        )).thenReturn(expected);

        Page<TravelRecord> result = travelRecordService.findAll(10L, null, null, null, 0, 20);

        assertThat(result.getContent()).containsExactly(travelRecord);
        assertThat(result.getTotalElements()).isEqualTo(1);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(travelRecordRepository).findByMemberId(
                eq(10L),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    void findsTravelRecordsByCountry() {
        Region korea = mock(Region.class);

        when(korea.getId()).thenReturn(1L);

        when(regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                "KR"
        )).thenReturn(Optional.of(korea));

        Page<TravelRecord> expected = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(travelRecordRepository.findByMemberIdAndCountryId(
                eq(10L),
                eq(1L),
                any(Pageable.class)
        )).thenReturn(expected);

        Page<TravelRecord> result = travelRecordService.findAll(
                10L,
                "KR",
                null,
                null,
                0,
                20
        );

        assertThat(result).isEqualTo(expected);

        verify(travelRecordRepository).findByMemberIdAndCountryId(
                eq(10L),
                eq(1L),
                any(Pageable.class)
        );
    }

    @Test
    void findsTravelRecordsByProvince() {
        Region korea = mock(Region.class);
        Region jeju = mock(Region.class);

        when(korea.getId()).thenReturn(1L);
        when(jeju.getId()).thenReturn(2L);

        when(regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                "KR"
        )).thenReturn(Optional.of(korea));

        when(regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                1L,
                RegionType.PROVINCE,
                "49"
        )).thenReturn(Optional.of(jeju));

        Page<TravelRecord> expected = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(travelRecordRepository.findByMemberIdAndProvinceId(
                eq(10L),
                eq(2L),
                any(Pageable.class)
        )).thenReturn(expected);

        Page<TravelRecord> result = travelRecordService.findAll(
                10L,
                "KR",
                "49",
                null,
                0,
                20
        );

        assertThat(result).isEqualTo(expected);

        verify(travelRecordRepository).findByMemberIdAndProvinceId(
                eq(10L),
                eq(2L),
                any(Pageable.class)
        );
    }

    @Test
    void findsTravelRecordsByDistrict() {
        Region korea = mock(Region.class);
        Region jeju = mock(Region.class);
        Region jejuCity = mock(Region.class);

        when(korea.getId()).thenReturn(1L);
        when(jeju.getId()).thenReturn(2L);
        when(jejuCity.getId()).thenReturn(3L);

        when(regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                "KR"
        )).thenReturn(Optional.of(korea));

        when(regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                1L,
                RegionType.PROVINCE,
                "49"
        )).thenReturn(Optional.of(jeju));

        when(regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                2L,
                RegionType.DISTRICT,
                "50110"
        )).thenReturn(Optional.of(jejuCity));

        Page<TravelRecord> expected = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(travelRecordRepository.findByMemberIdAndRegionId(
                eq(10L),
                eq(3L),
                any(Pageable.class)
        )).thenReturn(expected);

        Page<TravelRecord> result = travelRecordService.findAll(
                10L,
                "KR",
                "49",
                "50110",
                0,
                20
        );

        assertThat(result).isEqualTo(expected);

        verify(travelRecordRepository).findByMemberIdAndRegionId(
                eq(10L),
                eq(3L),
                any(Pageable.class)
        );
    }
}
