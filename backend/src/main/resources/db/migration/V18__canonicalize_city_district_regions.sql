-- 일반 도의 여행 기록 지역 단위를 일반구가 아닌 시·군으로 통일한다.
--
-- 처리 순서:
--   1. 13개 canonical 시 Region 추가
--   2. 기존 일반구 여행 기록을 canonical 시 Region으로 이관
--   3. 더 이상 사용하지 않는 일반구 Region 삭제
--
-- Region 계층에는 CITY 타입이 없으므로 시 코드도 DISTRICT로 저장한다.

INSERT INTO region (parent_id, root_id, region_code, name, region_type)
SELECT province.id,
       country.id,
       canonical_city.region_code,
       canonical_city.name,
       'DISTRICT'
FROM (
         SELECT '41' AS province_code, '41110' AS region_code, '수원시' AS name
         UNION ALL SELECT '41', '41130', '성남시'
         UNION ALL SELECT '41', '41170', '안양시'
         UNION ALL SELECT '41', '41190', '부천시'
         UNION ALL SELECT '41', '41270', '안산시'
         UNION ALL SELECT '41', '41280', '고양시'
         UNION ALL SELECT '41', '41460', '용인시'
         UNION ALL SELECT '41', '41590', '화성시'
         UNION ALL SELECT '43', '43110', '청주시'
         UNION ALL SELECT '44', '44130', '천안시'
         UNION ALL SELECT '45', '52110', '전주시'
         UNION ALL SELECT '47', '47110', '포항시'
         UNION ALL SELECT '48', '48120', '창원시'
     ) canonical_city
         JOIN region country
              ON country.parent_id IS NULL
                  AND country.region_type = 'COUNTRY'
                  AND country.region_code = 'KR'
         JOIN region province
              ON province.parent_id = country.id
                  AND province.region_type = 'PROVINCE'
                  AND province.region_code = canonical_city.province_code;

UPDATE travel_record travel_record
    JOIN region deprecated_district
         ON deprecated_district.id = travel_record.region_id
    JOIN (
             SELECT '41111' AS deprecated_code, '41110' AS canonical_code
             UNION ALL SELECT '41113', '41110'
             UNION ALL SELECT '41115', '41110'
             UNION ALL SELECT '41117', '41110'
             UNION ALL SELECT '41131', '41130'
             UNION ALL SELECT '41133', '41130'
             UNION ALL SELECT '41135', '41130'
             UNION ALL SELECT '41171', '41170'
             UNION ALL SELECT '41173', '41170'
             UNION ALL SELECT '41192', '41190'
             UNION ALL SELECT '41194', '41190'
             UNION ALL SELECT '41196', '41190'
             UNION ALL SELECT '41271', '41270'
             UNION ALL SELECT '41273', '41270'
             UNION ALL SELECT '41281', '41280'
             UNION ALL SELECT '41285', '41280'
             UNION ALL SELECT '41287', '41280'
             UNION ALL SELECT '41461', '41460'
             UNION ALL SELECT '41463', '41460'
             UNION ALL SELECT '41465', '41460'
             UNION ALL SELECT '41591', '41590'
             UNION ALL SELECT '41593', '41590'
             UNION ALL SELECT '41595', '41590'
             UNION ALL SELECT '41597', '41590'
             UNION ALL SELECT '43111', '43110'
             UNION ALL SELECT '43112', '43110'
             UNION ALL SELECT '43113', '43110'
             UNION ALL SELECT '43114', '43110'
             UNION ALL SELECT '44131', '44130'
             UNION ALL SELECT '44133', '44130'
             UNION ALL SELECT '52111', '52110'
             UNION ALL SELECT '52113', '52110'
             UNION ALL SELECT '47111', '47110'
             UNION ALL SELECT '47113', '47110'
             UNION ALL SELECT '48121', '48120'
             UNION ALL SELECT '48123', '48120'
             UNION ALL SELECT '48125', '48120'
             UNION ALL SELECT '48127', '48120'
             UNION ALL SELECT '48129', '48120'
         ) city_mapping
         ON city_mapping.deprecated_code = deprecated_district.region_code
    JOIN region canonical_city
         ON canonical_city.parent_id = deprecated_district.parent_id
             AND canonical_city.region_type = 'DISTRICT'
             AND canonical_city.region_code = city_mapping.canonical_code
SET travel_record.region_id = canonical_city.id;

DELETE deprecated_district
FROM region deprecated_district
         JOIN region country
              ON country.id = deprecated_district.root_id
                  AND country.region_type = 'COUNTRY'
                  AND country.region_code = 'KR'
WHERE deprecated_district.region_type = 'DISTRICT'
  AND deprecated_district.region_code IN (
      '41111', '41113', '41115', '41117',
      '41131', '41133', '41135',
      '41171', '41173',
      '41192', '41194', '41196',
      '41271', '41273',
      '41281', '41285', '41287',
      '41461', '41463', '41465',
      '41591', '41593', '41595', '41597',
      '43111', '43112', '43113', '43114',
      '44131', '44133',
      '52111', '52113',
      '47111', '47113',
      '48121', '48123', '48125', '48127', '48129'
  );
