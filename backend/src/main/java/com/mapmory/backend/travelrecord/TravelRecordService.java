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

        List<String> objectKeys = request.objectKeys() == null
                ? List.of()
                : request.objectKeys();

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
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 필터로 들어온 조건이 없으면 = 내 기록 전체 조회
        if (countryCode == null) {
            return travelRecordRepository.findByMemberId(memberId, pageable);
        }

        Region country = regionRepository
                .findByParentIsNullAndRegionTypeAndRegionCode(
                        RegionType.COUNTRY,
                        countryCode
                ).orElseThrow();

        // COUNTRY 타입이면서 부모가 없는 최상위 국가 Region을 찾기
        if (provinceCode == null) {
            return travelRecordRepository.findByMemberIdAndCountryId(
                    memberId,
                    country.getId(),
                    pageable
            );
        }

        Region province = regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                country.getId(),
                RegionType.PROVINCE,
                provinceCode
        ).orElseThrow();

        if (districtCode == null) {
            return travelRecordRepository.findByMemberIdAndProvinceId(
                    memberId,
                    province.getId(),
                    pageable
            );
        }

        Region district = regionRepository
                .findByParentIdAndRegionTypeAndRegionCode(
                        province.getId(),
                        RegionType.DISTRICT,
                        districtCode
                ).orElseThrow();

        return travelRecordRepository.findByMemberIdAndRegionId(
                memberId,
                district.getId(),
                pageable
        );
    }

    private Region resolveRegion(TravelRecordRequest request) {
        // countryCode → provinceCode → districtCode로 Region 탐색

        // 국가 조건 : parentId가 없어야 함 + RegionType = COUNTRY + countryCode
        Region country = regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                request.countryCode()
        ).orElseThrow();

        if (request.provinceCode() == null && request.districtCode() == null) {
            return country;
        }

        // 중간 지역 조건 : parentId = country_id + RegionType = PROVINCE + provinceCode
        Region province = regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                country.getId(),
                RegionType.PROVINCE,
                request.provinceCode()
        ).orElseThrow();

        // 세부 지역 조건 : parentId = province_id + RegionType = DISTRICT + districtCode
        return regionRepository
                .findByParentIdAndRegionTypeAndRegionCode(
                province.getId(),
                RegionType.DISTRICT,
                request.districtCode()
        ).orElseThrow();
    }
}
