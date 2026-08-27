package com.mapmory.backend.recordmedia;

import com.mapmory.backend.upload.policy.UploadPolicyProperties;
import com.mapmory.backend.upload.storage.PresignedUrlProvider;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RecordMediaUrlService {

    private final RecordMediaRepository recordMediaRepository;
    private final PresignedUrlProvider presignedUrlProvider;
    private final Duration expiration;

    public RecordMediaUrlService(
            RecordMediaRepository recordMediaRepository,
            PresignedUrlProvider presignedUrlProvider,
            UploadPolicyProperties uploadPolicyProperties
    ) {
        this.recordMediaRepository = recordMediaRepository;
        this.presignedUrlProvider = presignedUrlProvider;
        this.expiration = uploadPolicyProperties.presignedUrlExpiration();
    }

    public ExpiringUrl createViewUrl(String objectKey) {
        return ExpiringUrl.from(
                presignedUrlProvider.createPresignedGetUrl(objectKey, expiration),
                expiration
        );
    }

    public Map<Long, ExpiringUrl> createThumbnailUrls(List<Long> travelRecordIds) {
        if (travelRecordIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, ExpiringUrl> thumbnailUrls = new HashMap<>();
        List<RecordMedia> recordMedia = recordMediaRepository
                .findByTravelRecordIdInOrderByTravelRecordIdAscSortOrderAscIdAsc(travelRecordIds);
        for (RecordMedia media : recordMedia) {
            thumbnailUrls.computeIfAbsent(
                    media.travelRecordId(),
                    ignored -> createViewUrl(media.getThumbnailObjectKey())
            );
        }
        return Map.copyOf(thumbnailUrls);
    }
}
