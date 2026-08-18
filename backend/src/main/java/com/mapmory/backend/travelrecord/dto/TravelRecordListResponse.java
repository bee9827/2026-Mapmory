package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.travelrecord.TravelRecord;
import java.util.List;
import org.springframework.data.domain.Page;

public record TravelRecordListResponse(
        List<TravelRecordListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static TravelRecordListResponse from(
            Page<TravelRecord> travelRecords
    ) {
        return new TravelRecordListResponse(
                travelRecords.getContent().stream()
                        .map(TravelRecordListItemResponse::from)
                        .toList(),
                travelRecords.getNumber(),
                travelRecords.getSize(),
                travelRecords.getTotalElements(),
                travelRecords.getTotalPages(),
                travelRecords.hasNext()
        );
    }
}
