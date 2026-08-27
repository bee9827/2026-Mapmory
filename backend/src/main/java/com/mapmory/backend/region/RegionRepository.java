package com.mapmory.backend.region;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByParentIsNullAndRegionTypeAndRegionCode(
            RegionType regionType,
            String regionCode
    );

    Optional<Region> findByParentIdAndRegionTypeAndRegionCode(
            Long parentId,
            RegionType regionType,
            String regionCode
    );

    boolean existsByRegionTypeAndRegionCode(RegionType regionType, String regionCode);
}
