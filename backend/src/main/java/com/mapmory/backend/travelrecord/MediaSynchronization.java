package com.mapmory.backend.travelrecord;

import com.mapmory.backend.recordmedia.RecordMedia;
import java.util.List;

/**
 * 미디어 동기화 결과. {@code media}는 요청 순서대로 정렬된 최종 상태이고,
 * {@code removed}는 더 이상 요청에 없어 제거해야 하는 미디어다.
 */
public record MediaSynchronization(
        List<RecordMedia> media,
        List<RecordMedia> removed
) {
}
