package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.travelrecord.TravelRecord;

public record CreateTravelRecordResponse(
        Long id
) {
    public static CreateTravelRecordResponse from(TravelRecord travelRecord) {
        return new CreateTravelRecordResponse(travelRecord.getId());
    }
}
