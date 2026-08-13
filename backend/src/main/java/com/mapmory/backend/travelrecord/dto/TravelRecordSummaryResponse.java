package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.travelrecord.TravelRecord;
import java.time.LocalDate;

public record TravelRecordSummaryResponse(
        Long id,
        String title,
        String regionName,
        LocalDate startDate,
        LocalDate endDate,
        String thumbnailUrl
) {
    public static TravelRecordSummaryResponse from(
            TravelRecord travelRecord
    ) {
        return new TravelRecordSummaryResponse(
                travelRecord.getId(),
                travelRecord.getTitle(),
                travelRecord.getRegion().getName(),
                travelRecord.getStartDate(),
                travelRecord.getEndDate(),
                null // 다음 단계에서 첫 번째 미디어의 URL을 넣는다.
        );
    }
}
