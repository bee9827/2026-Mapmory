package com.mapmory.backend.region;

import com.mapmory.backend.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class RegionResolver {

    private final RegionRepository regionRepository;

    public RegionResolver(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    public Region findCountry(String countryCode) {
        return regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(
                RegionType.COUNTRY,
                countryCode
        ).orElseThrow(() -> new BusinessException(RegionErrorCode.COUNTRY_NOT_FOUND));
    }

    public Region findProvince(Region country, String provinceCode) {
        return findChild(country, RegionType.PROVINCE, provinceCode);
    }

    public Region findDistrict(Region province, String districtCode) {
        return findChild(province, RegionType.DISTRICT, districtCode);
    }

    private Region findChild(Region parent, RegionType regionType, String regionCode) {
        return regionRepository.findByParentIdAndRegionTypeAndRegionCode(
                parent.getId(),
                regionType,
                regionCode
        ).orElseGet(() -> {
            if (regionRepository.existsByRegionTypeAndRegionCode(regionType, regionCode)) {
                throw new BusinessException(RegionErrorCode.INVALID_REGION_HIERARCHY);
            }
            throw new BusinessException(RegionErrorCode.REGION_NOT_FOUND);
        });
    }
}
