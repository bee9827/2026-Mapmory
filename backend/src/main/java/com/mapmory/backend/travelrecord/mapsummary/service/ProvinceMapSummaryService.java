package com.mapmory.backend.travelrecord.mapsummary.service;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import com.mapmory.backend.member.repository.MemberRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.region.exception.RegionErrorCode;
import com.mapmory.backend.region.repository.RegionRepository;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.LevelPolicy;
import com.mapmory.backend.travelrecord.repository.TravelRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProvinceMapSummaryService {

    private final MemberRepository memberRepository;
    private final RegionRepository regionRepository;
    private final TravelRecordRepository travelRecordRepository;
    private final LevelPolicy levelPolicy;

    public ProvinceMapSummaryService(
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
    public List<RegionMapSummaryResponse> getProvinceSummaries(Long memberId, Long countryId) {
        validateMember(memberId);
        validateCountry(countryId);
        return travelRecordRepository.findProvinceMapSummaries(memberId, countryId).stream()
                .map(result -> RegionMapSummaryResponse.from(result, RegionType.PROVINCE, levelPolicy))
                .toList();
    }

    private void validateMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void validateCountry(Long countryId) {
        Region region = regionRepository.findById(countryId)
                .orElseThrow(() -> new BusinessException(RegionErrorCode.REGION_NOT_FOUND));
        if (region.getRegionType() != RegionType.COUNTRY) {
            throw new BusinessException(RegionErrorCode.INVALID_PARENT_REGION_TYPE);
        }
    }
}
