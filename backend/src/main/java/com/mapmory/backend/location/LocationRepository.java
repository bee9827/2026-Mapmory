package com.mapmory.backend.location;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByCountryCodeAndRegionCode(String countryCode, String regionCode);
}
