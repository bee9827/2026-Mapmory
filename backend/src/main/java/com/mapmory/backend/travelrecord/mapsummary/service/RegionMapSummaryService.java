package com.mapmory.backend.travelrecord.mapsummary.service;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.region.exception.RegionErrorCode;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionMapSummaryService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final TravelRecordRepository travelRecordRepository;
    private final LevelPolicy levelPolicy;

    public RegionMapSummaryService(
            MemberRepository memberRepository,
            RegionRepository regionRepository,
            TravelRecordRepository travelRecordRepository,
            LevelPolicy levelPolicy
    ) {
        this.memberRepository = memberRepository;
        this.regionRepository = regionRepository;
        this.travelRecordRepository = travelRecordRepository;
        this.levelPolicy = levelPolicy;
    }

    @Transactional(readOnly = true)
    public List<RegionMapSummaryResponse> getSummaries(Long memberId, Long parentRegionId) {
        validateMember(memberId);
        validateParentRegion(parentRegionId);
        return travelRecordRepository.findRegionMapSummaries(memberId, parentRegionId).stream()
                .map(result -> RegionMapSummaryResponse.from(result, levelPolicy))
                .toList();
    }

    private void validateMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void validateParentRegion(Long parentRegionId) {
        if (parentRegionId != null && !regionRepository.existsById(parentRegionId)) {
            throw new BusinessException(RegionErrorCode.REGION_NOT_FOUND);
        }
    }
}
