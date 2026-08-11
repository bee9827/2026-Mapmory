package com.mapmory.backend.region.repository;

import com.mapmory.backend.region.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
