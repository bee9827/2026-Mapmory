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
import org.springframework.stereotype.Service;

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
