package com.mapmory.backend.travelrecord;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.recordmedia.RecordMedia;
import com.mapmory.backend.recordmedia.RecordMediaRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.dto.TravelRecordRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TravelRecordService {

    private final TravelRecordRepository travelRecordRepository;
    private final RegionRepository regionRepository;
    private final MemberRepository memberRepository;
    private final RecordMediaRepository recordMediaRepository;

    public TravelRecordService(
            TravelRecordRepository travelRecordRepository,
            MemberRepository memberRepository,
            RegionRepository regionRepository,
            RecordMediaRepository recordMediaRepository
    ) {
        this.travelRecordRepository = travelRecordRepository;
        this.memberRepository = memberRepository;
        this.regionRepository = regionRepository;
        this.recordMediaRepository = recordMediaRepository;
    }

    @Transactional
    public TravelRecord create(Long memberId, TravelRecordRequest request) {
        Member member = memberRepository.getReferenceById(memberId);
        Region region = resolveRegion(request);

        TravelRecord travelRecord = TravelRecord.of(
                member,
                region,
                request.title(),
                request.content(),
                request.startDate(),
                request.endDate()
        );

        TravelRecord savedTravelRecord = travelRecordRepository.save(travelRecord);

        List<String> objectKeys = request.objectKeys() == null ? List.of() : request.objectKeys();

        // TODO : save or saveAll 결정하고 적용하기
        for (int index = 0; index < objectKeys.size(); index++) {
            RecordMedia recordMedia = RecordMedia.of(
                    savedTravelRecord,
                    objectKeys.get(index),
                    null,
                    index
            );

            recordMediaRepository.save(recordMedia);
        }

        return savedTravelRecord;
    }

    @Transactional(readOnly = true)
    public Page<TravelRecord> findAll(Long memberId, String countryCode, String provinceCode, String districtCode, int page, int size) {
        Pageable pageable = createPageable(page, size);

        if (countryCode == null) {
            return travelRecordRepository.findByMemberId(memberId, pageable);
        }

        Region country = findCountry(countryCode);

        if (provinceCode == null) {
            return travelRecordRepository.findByMemberIdAndCountryId(
                    memberId,
                    country.getId(),
                    pageable
            );
        }

        Region province = findProvince(country, provinceCode);

        if (districtCode == null) {
            return travelRecordRepository.findByMemberIdAndProvinceId(
                    memberId,
                    province.getId(),
                    pageable
            );
        }

        Region district = findDistrict(province, districtCode);

        return travelRecordRepository.findByMemberIdAndRegionId(
                memberId,
                district.getId(),
                pageable
        );
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private Region resolveRegion(TravelRecordRequest request) {
        Region country = findCountry(request.countryCode());

        if (request.provinceCode() == null && request.districtCode() == null) {
            return country;
        }

        Region province = findProvince(country, request.provinceCode());
        return findDistrict(province, request.districtCode());
    }

    private Region findCountry(String countryCode) {
        return regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                countryCode
        ).orElseThrow();
    }

    private Region findProvince(Region country, String provinceCode) {
        return regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                country.getId(),
                RegionType.PROVINCE,
                provinceCode
        ).orElseThrow();
    }

    private Region findDistrict(Region province, String districtCode) {
        return regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                province.getId(),
                RegionType.DISTRICT,
                districtCode
        ).orElseThrow();
    }
}
