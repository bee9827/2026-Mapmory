-- Postman 로컬 확인용 여행 일지
-- 파일 내용이 변경되어 다시 실행되더라도 같은 제목의 샘플 일지는 중복 생성하지 않는다.

-- 도우너: 국가만 선택한 해외 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       country.id,
       '일본 첫 여행',
       '국가만 선택한 해외 여행 일지입니다.',
       '2026-03-12',
       '2026-03-15',
       '2026-08-10 09:00:00',
       '2026-08-10 09:00:00'
FROM member
JOIN region country
  ON country.parent_id IS NULL
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'JP'
WHERE member.uuid = '2c853e88-97fb-4a42-852b-4dfc159c85ac'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '일본 첫 여행'
  );

-- 도우너: 서울특별시 종로구 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       district.id,
       '서촌 궁궐 나들이',
       '경복궁과 북촌을 걸었다.',
       '2026-04-05',
       '2026-04-05',
       '2026-08-11 10:00:00',
       '2026-08-11 10:00:00'
FROM member
JOIN region district
  ON district.region_type = 'DISTRICT'
 AND district.region_code = '11110'
JOIN region province
  ON province.id = district.parent_id
 AND province.region_type = 'PROVINCE'
 AND province.region_code = '11'
JOIN region country
  ON country.id = district.root_id
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'KR'
WHERE member.uuid = '2c853e88-97fb-4a42-852b-4dfc159c85ac'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '서촌 궁궐 나들이'
  );

-- 도우너: 제주특별자치도 제주시 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       district.id,
       '제주 바다 여행',
       '협재 해수욕장에서 노을을 봤다.',
       '2026-05-02',
       '2026-05-04',
       '2026-08-12 11:00:00',
       '2026-08-12 11:00:00'
FROM member
JOIN region district
  ON district.region_type = 'DISTRICT'
 AND district.region_code = '50110'
JOIN region province
  ON province.id = district.parent_id
 AND province.region_type = 'PROVINCE'
 AND province.region_code = '49'
JOIN region country
  ON country.id = district.root_id
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'KR'
WHERE member.uuid = '2c853e88-97fb-4a42-852b-4dfc159c85ac'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '제주 바다 여행'
  );

-- 도우너: 제주특별자치도 서귀포시 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       district.id,
       '서귀포 올레길',
       '올레길을 걸으며 사진을 찍었다.',
       '2026-05-05',
       '2026-05-05',
       '2026-08-13 12:00:00',
       '2026-08-13 12:00:00'
FROM member
JOIN region district
  ON district.region_type = 'DISTRICT'
 AND district.region_code = '50130'
JOIN region province
  ON province.id = district.parent_id
 AND province.region_type = 'PROVINCE'
 AND province.region_code = '49'
JOIN region country
  ON country.id = district.root_id
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'KR'
WHERE member.uuid = '2c853e88-97fb-4a42-852b-4dfc159c85ac'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '서귀포 올레길'
  );

-- 티온: 국가만 선택한 해외 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       country.id,
       '미국 횡단 여행',
       '국가 단위로 남긴 미국 여행 기록입니다.',
       '2026-06-01',
       '2026-06-10',
       '2026-08-10 13:00:00',
       '2026-08-10 13:00:00'
FROM member
JOIN region country
  ON country.parent_id IS NULL
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'US'
WHERE member.uuid = '774c091b-d920-4642-a3d8-b1548aa1f36c'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '미국 횡단 여행'
  );

-- 티온: 부산광역시 해운대구 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       district.id,
       '해운대 밤바다',
       '해운대에서 밤바다를 보고 왔다.',
       '2026-07-18',
       '2026-07-19',
       '2026-08-11 14:00:00',
       '2026-08-11 14:00:00'
FROM member
JOIN region district
  ON district.region_type = 'DISTRICT'
 AND district.region_code = '26350'
JOIN region province
  ON province.id = district.parent_id
 AND province.region_type = 'PROVINCE'
 AND province.region_code = '26'
JOIN region country
  ON country.id = district.root_id
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'KR'
WHERE member.uuid = '774c091b-d920-4642-a3d8-b1548aa1f36c'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '해운대 밤바다'
  );

-- 달수: 국가만 선택한 해외 일지
INSERT INTO travel_record (
    member_id, region_id, title, content, start_date, end_date, created_at, updated_at
)
SELECT member.id,
       country.id,
       '프랑스 미술관 여행',
       '파리의 미술관을 둘러봤다.',
       '2026-07-25',
       '2026-07-29',
       '2026-08-12 15:00:00',
       '2026-08-12 15:00:00'
FROM member
JOIN region country
  ON country.parent_id IS NULL
 AND country.region_type = 'COUNTRY'
 AND country.region_code = 'FR'
WHERE member.uuid = 'bd88b2eb-8e69-42e6-b755-f62044569bd1'
  AND NOT EXISTS (
      SELECT 1
      FROM travel_record existing_record
      WHERE existing_record.member_id = member.id
        AND existing_record.title = '프랑스 미술관 여행'
  );
