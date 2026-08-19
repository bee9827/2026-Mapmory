package com.mapmory.backend.travelrecord.mapsummary.service;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.region.exception.RegionErrorCode;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import com.mapmory.backend.travelrecord.mapsummary.repository.RegionMapSummaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionMapSummaryService {

    private final RegionRepository regionRepository;
    private final RegionMapSummaryRepository regionMapSummaryRepository;
    private final LevelPolicy levelPolicy;

    public RegionMapSummaryService(
            RegionRepository regionRepository,
            RegionMapSummaryRepository regionMapSummaryRepository,
            LevelPolicy levelPolicy
    ) {
        this.regionRepository = regionRepository;
        this.regionMapSummaryRepository = regionMapSummaryRepository;
        this.levelPolicy = levelPolicy;
    }

    @Transactional(readOnly = true)
    public List<RegionMapSummaryResponse> getSummaries(Member member, Long parentRegionId) {
        validateParentRegion(parentRegionId);
        return regionMapSummaryRepository.findRegionMapSummaries(member.getId(), parentRegionId).stream()
                .map(result -> RegionMapSummaryResponse.from(result, levelPolicy))
                .toList();
    }

    private void validateParentRegion(Long parentRegionId) {
        if (parentRegionId != null && !regionRepository.existsById(parentRegionId)) {
            throw new BusinessException(RegionErrorCode.REGION_NOT_FOUND);
        }
    }
}
