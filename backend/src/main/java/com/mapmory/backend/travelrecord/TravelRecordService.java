package com.mapmory.backend.travelrecord;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.recordmedia.RecordMedia;
import com.mapmory.backend.recordmedia.RecordMediaRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionResolver;
import com.mapmory.backend.travelrecord.dto.TravelRecordRequest;
import com.mapmory.backend.travelrecord.dto.TravelRecordDetailResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TravelRecordService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TravelRecordRepository travelRecordRepository;
    private final RegionResolver regionResolver;
    private final MemberRepository memberRepository;
    private final RecordMediaRepository recordMediaRepository;

    public TravelRecordService(
            TravelRecordRepository travelRecordRepository,
            MemberRepository memberRepository,
            RegionResolver regionResolver,
            RecordMediaRepository recordMediaRepository
    ) {
        this.travelRecordRepository = travelRecordRepository;
        this.memberRepository = memberRepository;
        this.regionResolver = regionResolver;
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
    public TravelRecordDetailResponse findById(Long memberId, Long travelRecordId) {
        TravelRecord travelRecord = travelRecordRepository.findByIdAndMemberId(travelRecordId, memberId)
                .orElseThrow(() -> new BusinessException(TravelRecordErrorCode.TRAVEL_RECORD_NOT_FOUND));
        List<RecordMedia> recordMedia = recordMediaRepository
                .findByTravelRecordIdOrderBySortOrderAsc(travelRecordId);

        return TravelRecordDetailResponse.from(travelRecord, recordMedia);
    }

    @Transactional
    public TravelRecordDetailResponse update(
            Long memberId,
            Long travelRecordId,
            TravelRecordRequest request
    ) {
        TravelRecord travelRecord = travelRecordRepository.findByIdAndMemberId(travelRecordId, memberId)
                .orElseThrow(() -> new BusinessException(TravelRecordErrorCode.TRAVEL_RECORD_NOT_FOUND));
        List<String> objectKeys = request.objectKeys() == null ? List.of() : request.objectKeys();
        validateUniqueObjectKeys(objectKeys);

        Region region = resolveRegion(request);
        List<RecordMedia> existingMedia = recordMediaRepository
                .findByTravelRecordIdOrderBySortOrderAsc(travelRecordId);
        validateObjectKeysAreAvailable(objectKeys, existingMedia);

        travelRecord.update(
                region,
                request.title(),
                request.content(),
                request.startDate(),
                request.endDate()
        );
        List<RecordMedia> updatedMedia = synchronizeMedia(travelRecord, existingMedia, objectKeys);

        travelRecordRepository.flush();

        return TravelRecordDetailResponse.from(travelRecord, updatedMedia);
    }

    @Transactional
    public void delete(Long memberId, Long travelRecordId) {
        TravelRecord travelRecord = travelRecordRepository.findByIdAndMemberId(travelRecordId, memberId)
                .orElseThrow(() -> new BusinessException(TravelRecordErrorCode.TRAVEL_RECORD_NOT_FOUND));

        travelRecordRepository.delete(travelRecord);
    }

    @Transactional(readOnly = true)
    public Page<TravelRecord> findAll(Long memberId, String countryCode, String provinceCode, String districtCode, int page, int size) {
        validateRegionCodeFormat(countryCode, provinceCode, districtCode);
        validateRegionFilterHierarchy(countryCode, provinceCode, districtCode);
        validatePagination(page, size);
        validateMemberExists(memberId);
        Pageable pageable = createPageable(page, size);

        if (countryCode == null) {
            return travelRecordRepository.findByMemberId(memberId, pageable);
        }

        Region country = regionResolver.findCountry(countryCode);

        if (provinceCode == null) {
            return travelRecordRepository.findByMemberIdAndCountryId(
                    memberId,
                    country.getId(),
                    pageable
            );
        }

        Region province = regionResolver.findProvince(country, provinceCode);

        if (districtCode == null) {
            return travelRecordRepository.findByMemberIdAndProvinceId(
                    memberId,
                    province.getId(),
                    pageable
            );
        }

        Region district = regionResolver.findDistrict(province, districtCode);

        return travelRecordRepository.findByMemberIdAndRegionId(
                memberId,
                district.getId(),
                pageable
        );
    }

    private void validateRegionFilterHierarchy(
            String countryCode,
            String provinceCode,
            String districtCode
    ) {
        if (countryCode == null && (provinceCode != null || districtCode != null)) {
            throw new BusinessException(TravelRecordErrorCode.REGION_REQUIRED);
        }

        if (provinceCode == null && districtCode != null) {
            throw new BusinessException(TravelRecordErrorCode.REGION_REQUIRED);
        }
    }

    private void validateRegionCodeFormat(
            String countryCode,
            String provinceCode,
            String districtCode
    ) {
        if ((countryCode != null && !countryCode.matches("[A-Z]{2}"))
                || isBlank(provinceCode)
                || isBlank(districtCode)) {
            throw new BusinessException(TravelRecordErrorCode.INVALID_REGION_CODE);
        }
    }

    private boolean isBlank(String value) {
        return value != null && value.isBlank();
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    TravelRecordErrorCode.INVALID_PAGINATION,
                    "page는 0 이상이고 size는 1 이상 %d 이하여야 합니다.".formatted(MAX_PAGE_SIZE)
            );
        }
    }

    private void validateMemberExists(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(TravelRecordErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private Region resolveRegion(TravelRecordRequest request) {
        Region country = regionResolver.findCountry(request.countryCode());

        if (request.provinceCode() == null && request.districtCode() == null) {
            return country;
        }

        Region province = regionResolver.findProvince(country, request.provinceCode());
        return regionResolver.findDistrict(province, request.districtCode());
    }

    private void validateUniqueObjectKeys(List<String> objectKeys) {
        if (new HashSet<>(objectKeys).size() != objectKeys.size()) {
            throw new BusinessException(TravelRecordErrorCode.INVALID_OBJECT_KEY);
        }
    }

    private void validateObjectKeysAreAvailable(
            List<String> objectKeys,
            List<RecordMedia> existingMedia
    ) {
        Set<String> existingObjectKeys = existingMedia.stream()
                .map(RecordMedia::getObjectKey)
                .collect(Collectors.toSet());
        List<String> newObjectKeys = objectKeys.stream()
                .filter(objectKey -> !existingObjectKeys.contains(objectKey))
                .toList();

        if (!newObjectKeys.isEmpty()
                && !recordMediaRepository.findByObjectKeyIn(newObjectKeys).isEmpty()) {
            throw new BusinessException(TravelRecordErrorCode.INVALID_OBJECT_KEY);
        }
    }

    private List<RecordMedia> synchronizeMedia(
            TravelRecord travelRecord,
            List<RecordMedia> existingMedia,
            List<String> objectKeys
    ) {
        Map<String, RecordMedia> existingMediaByObjectKey = new HashMap<>();
        for (RecordMedia recordMedia : existingMedia) {
            existingMediaByObjectKey.put(recordMedia.getObjectKey(), recordMedia);
        }

        List<RecordMedia> updatedMedia = new ArrayList<>();
        for (int index = 0; index < objectKeys.size(); index++) {
            String objectKey = objectKeys.get(index);
            RecordMedia recordMedia = existingMediaByObjectKey.remove(objectKey);
            if (recordMedia == null) {
                recordMedia = RecordMedia.of(travelRecord, objectKey, null, index);
            } else {
                recordMedia.updateSortOrder(index);
            }
            updatedMedia.add(recordMedia);
        }

        recordMediaRepository.deleteAll(existingMediaByObjectKey.values());
        return recordMediaRepository.saveAll(updatedMedia);
    }
}
