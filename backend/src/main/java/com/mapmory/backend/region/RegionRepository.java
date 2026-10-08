package com.mapmory.backend.region;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findByParentIsNullAndRegionTypeAndRegionCode(
            RegionType regionType,
            String regionCode
    );

    @EntityGraph(attributePaths = {"parent", "root"})
    Optional<Region> findByParentIdAndRegionTypeAndRegionCode(
            Long parentId,
            RegionType regionType,
            String regionCode
    );

    boolean existsByRegionTypeAndRegionCode(RegionType regionType, String regionCode);

    List<Region> findByParentIdAndRegionType(Long parentId, RegionType regionType);
}
