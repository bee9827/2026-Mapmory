package com.mapmory.backend.mapsummary.application;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.country.CountryRepository;
import com.mapmory.backend.location.Location;
import com.mapmory.backend.location.LocationRepository;
import com.mapmory.backend.mapsummary.MapSummaryErrorCode;
import com.mapmory.backend.mapsummary.api.CountryMapSummaryResponse;
import com.mapmory.backend.mapsummary.api.RegionMapSummaryResponse;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.travelrecord.RegionSummaryProjection;
import com.mapmory.backend.travelrecord.TravelRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MapSummaryService {

    private final MemberRepository memberRepository;
    private final CountryRepository countryRepository;
    private final LocationRepository locationRepository;
    private final TravelRecordRepository travelRecordRepository;
    private final MapSummaryLevelPolicy levelPolicy;

    public MapSummaryService(
            MemberRepository memberRepository,
            CountryRepository countryRepository,
            LocationRepository locationRepository,
            TravelRecordRepository travelRecordRepository,
            MapSummaryLevelPolicy levelPolicy
    ) {
        this.memberRepository = memberRepository;
        this.countryRepository = countryRepository;
        this.locationRepository = locationRepository;
        this.travelRecordRepository = travelRecordRepository;
        this.levelPolicy = levelPolicy;
    }

    public List<CountryMapSummaryResponse> summarizeCountries(Long memberId) {
        validateMember(memberId);
        return travelRecordRepository.summarizeCountries(memberId).stream()
                .map(summary -> new CountryMapSummaryResponse(
                        summary.getCountryCode(),
                        summary.getName(),
                        summary.getCount(),
                        levelPolicy.levelOf(summary.getCount())
                ))
                .toList();
    }

    public List<RegionMapSummaryResponse> summarizeTopLevelRegions(
            Long memberId,
            String countryCode
    ) {
        validateMember(memberId);
        validateCountry(countryCode);
        return toRegionResponses(
                travelRecordRepository.summarizeTopLevelRegions(memberId, countryCode)
        );
    }

    public List<RegionMapSummaryResponse> summarizeChildren(
            Long memberId,
            String countryCode,
            String parentLocationCode
    ) {
        validateMember(memberId);
        validateCountry(countryCode);
        Location parent = locationRepository
                .findByCountryCodeAndRegionCode(countryCode, parentLocationCode)
                .orElseThrow(() -> new BusinessException(MapSummaryErrorCode.LOCATION_NOT_FOUND));

        return toRegionResponses(travelRecordRepository.summarizeChildren(
                memberId,
                countryCode,
                parent.getId()
        ));
    }

    private List<RegionMapSummaryResponse> toRegionResponses(
            List<RegionSummaryProjection> summaries
    ) {
        return summaries.stream()
                .map(summary -> new RegionMapSummaryResponse(
                        summary.getLocationCode(),
                        summary.getName(),
                        summary.getCount(),
                        levelPolicy.levelOf(summary.getCount())
                ))
                .toList();
    }

    private void validateMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(MapSummaryErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void validateCountry(String countryCode) {
        if (!countryRepository.existsByCode(countryCode)) {
            throw new BusinessException(MapSummaryErrorCode.COUNTRY_NOT_FOUND);
        }
    }
}
