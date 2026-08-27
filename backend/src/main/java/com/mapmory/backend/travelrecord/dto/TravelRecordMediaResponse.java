package com.mapmory.backend.travelrecord.dto;

import com.mapmory.backend.recordmedia.RecordMedia;
import java.net.URI;
import java.time.Duration;

public record TravelRecordMediaResponse(
        Long id,
        String objectKey,
        String viewUrl,
        long viewUrlExpiresIn,
        int sortOrder
) {
    public static TravelRecordMediaResponse from(
            RecordMedia recordMedia,
            URI viewUrl,
            Duration expiration
    ) {
        return new TravelRecordMediaResponse(
                recordMedia.getId(),
                recordMedia.getObjectKey(),
                viewUrl.toString(),
                expiration.toSeconds(),
                recordMedia.getSortOrder()
        );
    }
}
