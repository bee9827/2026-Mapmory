package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.recordmedia.RecordMedia;
import com.mapmory.backend.recordmedia.RecordMediaRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionResolver;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.dto.TravelRecordDetailResponse;
import com.mapmory.backend.travelrecord.dto.TravelRecordRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TravelRecordServiceTest {

    @Mock
    private TravelRecordRepository travelRecordRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionResolver regionResolver;

    @Mock
    private RecordMediaRepository recordMediaRepository;

    @InjectMocks
    private TravelRecordService travelRecordService;

    @BeforeEach
    void setUp() {
        lenient().when(memberRepository.existsById(10L)).thenReturn(true);
    }

    @Test
    void createsCountryTravelRecord() {
        Member member = mock(Member.class);
        Region japan = mock(Region.class);
        TravelRecordRequest request = new TravelRecordRequest(
                "JP", null, null, "일본 여행", "", LocalDate.of(2026, 8, 11), null, List.of()
        );

        when(memberRepository.getReferenceById(10L)).thenReturn(member);
        when(regionResolver.findCountry("JP")).thenReturn(japan);
        when(travelRecordRepository.save(any(TravelRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TravelRecord result = travelRecordService.create(10L, request);

        assertThat(result).isNotNull();
        verify(regionResolver).findCountry("JP");
        verify(travelRecordRepository).save(any(TravelRecord.class));
    }

    @Test
    void findsTravelRecordDetailWithRegionHierarchyAndOrderedObjectKeys() {
        Region country = Region.of(null, null, "KR", "대한민국", RegionType.COUNTRY);
        Region province = Region.of(country, country, "49", "제주특별자치도", RegionType.PROVINCE);
        Region district = Region.of(province, country, "50110", "제주시", RegionType.DISTRICT);
        TravelRecord travelRecord = TravelRecord.of(
                mock(Member.class),
                district,
                "제주 여행",
                "제주시를 걸었다.",
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 13)
        );
        ReflectionTestUtils.setField(travelRecord, "id", 101L);
        List<RecordMedia> recordMedia = List.of(
                RecordMedia.of(travelRecord, "mapmory/travel-records/a.jpg", null, 0),
                RecordMedia.of(travelRecord, "mapmory/travel-records/b.jpg", null, 1)
        );
        when(travelRecordRepository.findByIdAndMemberId(101L, 10L))
                .thenReturn(Optional.of(travelRecord));
        when(recordMediaRepository.findByTravelRecordIdOrderBySortOrderAsc(101L))
                .thenReturn(recordMedia);

        TravelRecordDetailResponse result = travelRecordService.findById(10L, 101L);

        assertThat(result.id()).isEqualTo(101L);
        assertThat(result.content()).isEqualTo("제주시를 걸었다.");
        assertThat(result.region().country().code()).isEqualTo("KR");
        assertThat(result.region().province().code()).isEqualTo("49");
        assertThat(result.region().district().code()).isEqualTo("50110");
        assertThat(result.objectKeys()).containsExactly(
                "mapmory/travel-records/a.jpg",
                "mapmory/travel-records/b.jpg"
        );
    }

    @Test
    void returnsEmptyObjectKeysForTravelRecordWithoutMedia() {
        Region japan = Region.of(null, null, "JP", "일본", RegionType.COUNTRY);
        TravelRecord travelRecord = TravelRecord.of(
                mock(Member.class),
                japan,
                "일본 여행",
                "도쿄 여행",
                LocalDate.of(2026, 8, 11),
                null
        );
        ReflectionTestUtils.setField(travelRecord, "id", 102L);
        when(travelRecordRepository.findByIdAndMemberId(102L, 10L))
                .thenReturn(Optional.of(travelRecord));
        when(recordMediaRepository.findByTravelRecordIdOrderBySortOrderAsc(102L))
                .thenReturn(List.of());

        TravelRecordDetailResponse result = travelRecordService.findById(10L, 102L);

        assertThat(result.region().country().code()).isEqualTo("JP");
        assertThat(result.region().province()).isNull();
        assertThat(result.region().district()).isNull();
        assertThat(result.objectKeys()).isEmpty();
    }

    @Test
    void rejectsMissingOrOtherMembersTravelRecord() {
        when(travelRecordRepository.findByIdAndMemberId(101L, 10L))
                .thenReturn(Optional.empty());

        assertError(() -> travelRecordService.findById(10L, 101L), "TRAVEL_RECORD_NOT_FOUND");
        verify(recordMediaRepository, never()).findByTravelRecordIdOrderBySortOrderAsc(101L);
    }

    @Test
    void findsRecordsWithoutRegionFilter() {
        TravelRecord travelRecord = mock(TravelRecord.class);
        Page<TravelRecord> expected = new PageImpl<>(List.of(travelRecord), PageRequest.of(0, 20), 1);
        when(travelRecordRepository.findByMemberId(eq(10L), any(Pageable.class))).thenReturn(expected);

        Page<TravelRecord> result = travelRecordService.findAll(10L, null, null, null, 0, 20);

        assertThat(result).isEqualTo(expected);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(travelRecordRepository).findByMemberId(eq(10L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void findsRecordsByCountry() {
        Region korea = region(1L);
        Page<TravelRecord> expected = Page.empty();
        when(regionResolver.findCountry("KR")).thenReturn(korea);
        when(travelRecordRepository.findByMemberIdAndCountryId(eq(10L), eq(1L), any(Pageable.class)))
                .thenReturn(expected);

        assertThat(travelRecordService.findAll(10L, "KR", null, null, 0, 20)).isEqualTo(expected);
    }

    @Test
    void findsRecordsByProvince() {
        Region korea = mock(Region.class);
        Region jeju = region(2L);
        Page<TravelRecord> expected = Page.empty();
        when(regionResolver.findCountry("KR")).thenReturn(korea);
        when(regionResolver.findProvince(korea, "49")).thenReturn(jeju);
        when(travelRecordRepository.findByMemberIdAndProvinceId(eq(10L), eq(2L), any(Pageable.class)))
                .thenReturn(expected);

        assertThat(travelRecordService.findAll(10L, "KR", "49", null, 0, 20)).isEqualTo(expected);
    }

    @Test
    void findsRecordsByDistrict() {
        Region korea = mock(Region.class);
        Region jeju = mock(Region.class);
        Region jejuCity = region(3L);
        Page<TravelRecord> expected = Page.empty();
        when(regionResolver.findCountry("KR")).thenReturn(korea);
        when(regionResolver.findProvince(korea, "49")).thenReturn(jeju);
        when(regionResolver.findDistrict(jeju, "50110")).thenReturn(jejuCity);
        when(travelRecordRepository.findByMemberIdAndRegionId(eq(10L), eq(3L), any(Pageable.class)))
                .thenReturn(expected);

        assertThat(travelRecordService.findAll(10L, "KR", "49", "50110", 0, 20)).isEqualTo(expected);
    }

    @Test
    void rejectsInvalidRegionFilterCombination() {
        assertError(() -> travelRecordService.findAll(10L, null, "49", null, 0, 20), "REGION_REQUIRED");
        assertError(() -> travelRecordService.findAll(10L, "KR", null, "50110", 0, 20), "REGION_REQUIRED");
    }

    @Test
    void rejectsInvalidRegionCodeFormat() {
        assertError(() -> travelRecordService.findAll(10L, "kr", null, null, 0, 20), "VALIDATION_ERROR");
        assertError(() -> travelRecordService.findAll(10L, "KR", " ", null, 0, 20), "VALIDATION_ERROR");
    }

    @Test
    void rejectsInvalidPagination() {
        assertError(() -> travelRecordService.findAll(10L, null, null, null, -1, 20), "VALIDATION_ERROR");
        assertError(() -> travelRecordService.findAll(10L, null, null, null, 0, 0), "VALIDATION_ERROR");
        assertError(() -> travelRecordService.findAll(10L, null, null, null, 0, 101), "VALIDATION_ERROR");
    }

    @Test
    void rejectsNonexistentMember() {
        when(memberRepository.existsById(10L)).thenReturn(false);

        assertError(() -> travelRecordService.findAll(10L, null, null, null, 0, 20), "MEMBER_NOT_FOUND");
    }

    private Region region(Long id) {
        Region region = mock(Region.class);
        when(region.getId()).thenReturn(id);
        return region;
    }

    private void assertError(Runnable action, String errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode().code())
                .isEqualTo(errorCode);
    }
}
