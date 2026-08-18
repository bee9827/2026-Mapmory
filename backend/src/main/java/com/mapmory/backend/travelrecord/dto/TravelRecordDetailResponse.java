package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.recordmedia.RecordMedia;
import com.mapmory.backend.travelrecord.TravelRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TravelRecordDetailResponse(
        Long id,
        String title,
        String content,
        RegionDetailResponse region,
        LocalDate startDate,
        LocalDate endDate,
        List<String> objectKeys,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TravelRecordDetailResponse from(
            TravelRecord travelRecord,
            List<RecordMedia> recordMedia
    ) {
        return new TravelRecordDetailResponse(
                travelRecord.getId(),
                travelRecord.getTitle(),
                travelRecord.getContent(),
                RegionDetailResponse.from(travelRecord.getRegion()),
                travelRecord.getStartDate(),
                travelRecord.getEndDate(),
                recordMedia.stream()
                        .map(RecordMedia::getObjectKey)
                        .toList(),
                travelRecord.getCreatedAt(),
                travelRecord.getUpdatedAt()
        );
    }
}
