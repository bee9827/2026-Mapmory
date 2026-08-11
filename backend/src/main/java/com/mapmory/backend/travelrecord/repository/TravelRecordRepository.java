package com.mapmory.backend.travelrecord.repository;

import com.mapmory.backend.travelrecord.TravelRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TravelRecordRepository extends JpaRepository<TravelRecord, Long> {

    @Query(value = """
            SELECT country.id AS regionId,
                   country.region_code AS regionCode,
                   country.name AS name,
                   COALESCE(member_record.record_count, 0) AS recordCount
            FROM region country
            LEFT JOIN (
                SELECT COALESCE(selected_region.root_id, selected_region.id) AS country_id,
                       COUNT(tr.id) AS record_count
                FROM travel_record tr
                JOIN region selected_region ON selected_region.id = tr.region_id
                WHERE tr.member_id = :memberId
                GROUP BY COALESCE(selected_region.root_id, selected_region.id)
            ) member_record ON member_record.country_id = country.id
            WHERE country.region_type = 'COUNTRY'
            ORDER BY country.region_code
            """, nativeQuery = true)
    List<RegionMapSummaryQueryResult> findCountryMapSummaries(@Param("memberId") Long memberId);

    @Query(value = """
            SELECT province.id AS regionId,
                   province.region_code AS regionCode,
                   province.name AS name,
                   COALESCE(member_record.record_count, 0) AS recordCount
            FROM region province
            LEFT JOIN (
                SELECT CASE selected_region.region_type
                           WHEN 'PROVINCE' THEN selected_region.id
                           WHEN 'DISTRICT' THEN selected_region.parent_id
                       END AS province_id,
                       COUNT(tr.id) AS record_count
                FROM travel_record tr
                JOIN region selected_region ON selected_region.id = tr.region_id
                WHERE tr.member_id = :memberId
                  AND selected_region.root_id = :countryId
                GROUP BY CASE selected_region.region_type
                             WHEN 'PROVINCE' THEN selected_region.id
                             WHEN 'DISTRICT' THEN selected_region.parent_id
                         END
            ) member_record ON member_record.province_id = province.id
            WHERE province.parent_id = :countryId
              AND province.region_type = 'PROVINCE'
            ORDER BY province.region_code
            """, nativeQuery = true)
    List<RegionMapSummaryQueryResult> findProvinceMapSummaries(
            @Param("memberId") Long memberId,
            @Param("countryId") Long countryId
    );
}
