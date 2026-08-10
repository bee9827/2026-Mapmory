package com.mapmory.backend.travelrecord;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TravelRecordRepository extends JpaRepository<TravelRecord, Long> {

    @Query("""
            select tr.country.code as countryCode,
                   tr.country.name as name,
                   count(tr.id) as count
            from TravelRecord tr
            where tr.member.id = :memberId
            group by tr.country.code, tr.country.name
            order by tr.country.code
            """)
    List<CountrySummaryProjection> summarizeCountries(@Param("memberId") Long memberId);

    @Query("""
            select parent.regionCode as locationCode,
                   parent.name as name,
                   count(tr.id) as count
            from TravelRecord tr
            join tr.location location
            join location.parent parent
            where tr.member.id = :memberId
              and tr.country.code = :countryCode
              and parent.country.code = :countryCode
            group by parent.regionCode, parent.name
            order by parent.regionCode
            """)
    List<RegionSummaryProjection> summarizeTopLevelRegions(
            @Param("memberId") Long memberId,
            @Param("countryCode") String countryCode
    );

    @Query("""
            select location.regionCode as locationCode,
                   location.name as name,
                   count(tr.id) as count
            from TravelRecord tr
            join tr.location location
            where tr.member.id = :memberId
              and tr.country.code = :countryCode
              and location.parent.id = :parentLocationId
            group by location.regionCode, location.name
            order by location.regionCode
            """)
    List<RegionSummaryProjection> summarizeChildren(
            @Param("memberId") Long memberId,
            @Param("countryCode") String countryCode,
            @Param("parentLocationId") Long parentLocationId
    );
}
