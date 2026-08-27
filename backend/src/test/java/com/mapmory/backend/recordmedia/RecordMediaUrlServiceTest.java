package com.mapmory.backend.recordmedia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.travelrecord.TravelRecord;
import com.mapmory.backend.upload.policy.UploadPolicyProperties;
import com.mapmory.backend.upload.storage.PresignedUrlProvider;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordMediaUrlServiceTest {

    private final RecordMediaRepository recordMediaRepository = mock(RecordMediaRepository.class);
    private final PresignedUrlProvider presignedUrlProvider = mock(PresignedUrlProvider.class);
    private final UploadPolicyProperties uploadPolicyProperties = mock(UploadPolicyProperties.class);
    private RecordMediaUrlService recordMediaUrlService;

    @BeforeEach
    void setUp() {
        when(uploadPolicyProperties.presignedUrlExpiration()).thenReturn(Duration.ofMinutes(5));
        recordMediaUrlService = new RecordMediaUrlService(
                recordMediaRepository,
                presignedUrlProvider,
                uploadPolicyProperties
        );
    }

    @Test
    void 조회_URL과_만료_시간을_값_객체로_반환한다() {
        when(presignedUrlProvider.createPresignedGetUrl("mapmory/original.jpg", Duration.ofMinutes(5)))
                .thenReturn(URI.create("https://download.example/mapmory/original.jpg"));

        ExpiringUrl result = recordMediaUrlService.createViewUrl("mapmory/original.jpg");

        assertThat(result).isEqualTo(new ExpiringUrl(
                "https://download.example/mapmory/original.jpg",
                300L
        ));
    }

    @Test
    void 만료_URL은_빈_URL이나_유효하지_않은_만료_시간을_허용하지_않는다() {
        assertThatThrownBy(() -> new ExpiringUrl(" ", 300L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExpiringUrl("https://download.example/file.jpg", 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 기록별_첫_미디어만_썸네일_URL로_변환한다() {
        TravelRecord firstRecord = travelRecord(101L);
        TravelRecord secondRecord = travelRecord(102L);
        RecordMedia firstMedia = RecordMedia.of(
                firstRecord,
                "mapmory/first-original.jpg",
                null,
                0
        );
        RecordMedia laterMedia = RecordMedia.of(
                firstRecord,
                "mapmory/later.jpg",
                null,
                0
        );
        RecordMedia thumbnailMedia = RecordMedia.of(
                secondRecord,
                "mapmory/second-original.jpg",
                "mapmory/second-thumbnail.jpg",
                0
        );
        List<Long> travelRecordIds = List.of(101L, 102L);
        when(recordMediaRepository.findByTravelRecordIdInOrderByTravelRecordIdAscSortOrderAscIdAsc(
                travelRecordIds
        )).thenReturn(List.of(firstMedia, laterMedia, thumbnailMedia));
        when(presignedUrlProvider.createPresignedGetUrl(any(), any()))
                .thenAnswer(invocation -> URI.create(
                        "https://download.example/" + invocation.getArgument(0)
                ));

        Map<Long, ExpiringUrl> result = recordMediaUrlService.createThumbnailUrls(travelRecordIds);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                101L,
                new ExpiringUrl("https://download.example/mapmory/first-original.jpg", 300L),
                102L,
                new ExpiringUrl("https://download.example/mapmory/second-thumbnail.jpg", 300L)
        ));
        verify(presignedUrlProvider).createPresignedGetUrl(
                "mapmory/first-original.jpg",
                Duration.ofMinutes(5)
        );
        verify(presignedUrlProvider, never()).createPresignedGetUrl(
                "mapmory/later.jpg",
                Duration.ofMinutes(5)
        );
        verify(presignedUrlProvider).createPresignedGetUrl(
                "mapmory/second-thumbnail.jpg",
                Duration.ofMinutes(5)
        );
    }

    @Test
    void 기록_ID가_없으면_저장소를_조회하지_않는다() {
        assertThat(recordMediaUrlService.createThumbnailUrls(List.of())).isEmpty();

        verify(recordMediaRepository, never())
                .findByTravelRecordIdInOrderByTravelRecordIdAscSortOrderAscIdAsc(any());
        verify(presignedUrlProvider, never()).createPresignedGetUrl(any(), any());
    }

    private TravelRecord travelRecord(Long id) {
        TravelRecord travelRecord = mock(TravelRecord.class);
        when(travelRecord.getId()).thenReturn(id);
        return travelRecord;
    }
}
