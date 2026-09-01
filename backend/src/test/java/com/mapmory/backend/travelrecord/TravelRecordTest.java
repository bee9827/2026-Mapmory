package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.recordmedia.RecordMedia;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionType;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class TravelRecordTest {

    private static final String KEY_A = "travel-records/10/a.jpg";
    private static final String KEY_B = "travel-records/10/b.jpg";
    private static final String KEY_C = "travel-records/10/c.jpg";

    @Test
    void 요청_순서를_미디어_정렬_순서로_삼는다() {
        TravelRecord travelRecord = travelRecord();
        RecordMedia mediaA = RecordMedia.of(travelRecord, KEY_A, null, 0);
        RecordMedia mediaB = RecordMedia.of(travelRecord, KEY_B, null, 1);

        MediaSynchronization result = travelRecord.synchronizeMedia(
                List.of(mediaA, mediaB),
                List.of(KEY_B, KEY_A)
        );

        assertThat(result.media()).containsExactly(mediaB, mediaA);
        assertThat(mediaB.getSortOrder()).isZero();
        assertThat(mediaA.getSortOrder()).isEqualTo(1);
        assertThat(result.removed()).isEmpty();
    }

    @Test
    void 요청에_없는_미디어는_제거_대상이_된다() {
        TravelRecord travelRecord = travelRecord();
        RecordMedia mediaA = RecordMedia.of(travelRecord, KEY_A, null, 0);
        RecordMedia mediaB = RecordMedia.of(travelRecord, KEY_B, null, 1);

        MediaSynchronization result = travelRecord.synchronizeMedia(
                List.of(mediaA, mediaB),
                List.of(KEY_B)
        );

        assertThat(result.media()).containsExactly(mediaB);
        assertThat(result.removed()).containsExactly(mediaA);
    }

    @Test
    void 새_Object_Key는_미디어를_만들어_붙인다() {
        TravelRecord travelRecord = travelRecord();
        RecordMedia mediaA = RecordMedia.of(travelRecord, KEY_A, null, 0);

        MediaSynchronization result = travelRecord.synchronizeMedia(
                List.of(mediaA),
                List.of(KEY_A, KEY_C)
        );

        assertThat(result.media()).hasSize(2);
        assertThat(result.media())
                .extracting(RecordMedia::getObjectKey)
                .containsExactly(KEY_A, KEY_C);
        assertThat(result.media().getLast().getSortOrder()).isEqualTo(1);
        assertThat(result.removed()).isEmpty();
    }

    @Test
    void 빈_Object_Key_목록은_모든_미디어를_제거_대상으로_삼는다() {
        TravelRecord travelRecord = travelRecord();
        RecordMedia mediaA = RecordMedia.of(travelRecord, KEY_A, null, 0);

        MediaSynchronization result = travelRecord.synchronizeMedia(List.of(mediaA), List.of());

        assertThat(result.media()).isEmpty();
        assertThat(result.removed()).containsExactly(mediaA);
    }

    @Test
    void 한_일지_안에서_Object_Key가_중복되면_거부한다() {
        TravelRecord travelRecord = travelRecord();

        assertThatThrownBy(() -> travelRecord.validateObjectKeys(List.of(KEY_A, KEY_A)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode().code())
                .isEqualTo("INVALID_OBJECT_KEY");
    }

    @Test
    void 이미_가진_Object_Key는_새_키가_아니다() {
        TravelRecord travelRecord = travelRecord();
        RecordMedia mediaA = RecordMedia.of(travelRecord, KEY_A, null, 0);

        List<String> newObjectKeys = travelRecord.newObjectKeys(
                List.of(mediaA),
                List.of(KEY_A, KEY_C)
        );

        assertThat(newObjectKeys).containsExactly(KEY_C);
    }

    @Test
    void 태그를_다섯_개까지_붙일_수_있다() {
        TravelRecord travelRecord = travelRecord();

        assertThatCode(() -> travelRecord.validateTagIds(tagIds(5))).doesNotThrowAnyException();
    }

    @Test
    void 태그가_다섯_개를_넘으면_거부한다() {
        TravelRecord travelRecord = travelRecord();

        assertTagError(() -> travelRecord.validateTagIds(tagIds(6)), "TOO_MANY_TAGS");
    }

    @Test
    void 같은_태그를_두_번_붙이면_거부한다() {
        TravelRecord travelRecord = travelRecord();

        assertTagError(() -> travelRecord.validateTagIds(List.of(1L, 1L)), "VALIDATION_ERROR");
    }

    private TravelRecord travelRecord() {
        Region region = Region.of(null, null, "JP", "일본", RegionType.COUNTRY);

        return TravelRecord.of(
                mock(Member.class),
                region,
                "일본 여행",
                "본문",
                LocalDate.of(2026, 8, 11),
                null
        );
    }

    private List<Long> tagIds(int count) {
        return LongStream.rangeClosed(1, count).boxed().toList();
    }

    private void assertTagError(Runnable action, String errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode().code())
                .isEqualTo(errorCode);
    }
}
