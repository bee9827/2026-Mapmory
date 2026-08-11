package com.mapmory.backend.travelrecord.mapsummary;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.MemberErrorCode;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CountryMapSummaryService {

    private final MemberRepository memberRepository;
    private final TravelRecordRepository travelRecordRepository;
    private final LevelPolicy levelPolicy;

    public CountryMapSummaryService(
            MemberRepository memberRepository,
            TravelRecordRepository travelRecordRepository,
            LevelPolicy levelPolicy
    ) {
        this.memberRepository = memberRepository;
        this.travelRecordRepository = travelRecordRepository;
        this.levelPolicy = levelPolicy;
    }

    @Transactional(readOnly = true)
    public List<RegionMapSummaryResponse> getCountrySummaries(Long memberId) {
        validateMember(memberId);
        return travelRecordRepository.findCountryMapSummaries(memberId).stream()
                .map(result -> RegionMapSummaryResponse.from(result, levelPolicy))
                .toList();
    }

    private void validateMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
