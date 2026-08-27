package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.recordmedia.ExpiringUrl;
import com.mapmory.backend.tag.Tag;
import com.mapmory.backend.travelrecord.TravelRecord;
import java.util.List;
import java.util.Map;
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
            Page<TravelRecord> travelRecords,
            Map<Long, List<Tag>> tagsByTravelRecordId,
            Map<Long, ExpiringUrl> thumbnailUrlsByTravelRecordId
    ) {
        return new TravelRecordListResponse(
                travelRecords.getContent().stream()
                        .map(travelRecord -> TravelRecordListItemResponse.from(
                                travelRecord,
                                tagsByTravelRecordId.getOrDefault(travelRecord.getId(), List.of()),
                                thumbnailUrlsByTravelRecordId.get(travelRecord.getId())
                        ))
                        .toList(),
                travelRecords.getNumber(),
                travelRecords.getSize(),
                travelRecords.getTotalElements(),
                travelRecords.getTotalPages(),
                travelRecords.hasNext()
        );
    }
}
