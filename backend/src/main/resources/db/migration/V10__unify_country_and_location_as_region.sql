-- 국가와 행정구역을 하나의 Region 계층으로 전환한다.
CREATE TABLE region (
                        id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
                        parent_id   BIGINT       NULL,
                        root_id     BIGINT       NULL,
                        region_code VARCHAR(20)  NOT NULL,
                        name        VARCHAR(100) NOT NULL,
                        region_type VARCHAR(20)  NOT NULL COMMENT 'COUNTRY | PROVINCE | DISTRICT',
                        created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_region_parent FOREIGN KEY (parent_id) REFERENCES region (id),
                        CONSTRAINT fk_region_root FOREIGN KEY (root_id) REFERENCES region (id),
                        INDEX idx_region_parent (parent_id),
                        INDEX idx_region_root (root_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- 국가를 Region 루트로 먼저 이관한다.
INSERT INTO region (region_code, name, region_type)
SELECT code, name, 'COUNTRY'
FROM country;

-- 기존 location의 부모 참조를 변환하기 위해 마이그레이션 중에만 원본 ID를 보관한다.
ALTER TABLE region
    ADD COLUMN legacy_location_id BIGINT NULL,
    ADD COLUMN legacy_parent_id BIGINT NULL;

INSERT INTO region (root_id, region_code, name, region_type, legacy_location_id, legacy_parent_id)
SELECT country_region.id,
       location.region_code,
       location.name,
       location.location_type,
       location.id,
       location.parent_id
FROM location
         JOIN country ON country.id = location.country_id
         JOIN region country_region
              ON country_region.region_type = 'COUNTRY'
                  AND country_region.region_code = country.code;

UPDATE region child
    JOIN region parent ON parent.legacy_location_id = child.legacy_parent_id
SET child.parent_id = parent.id
WHERE child.legacy_parent_id IS NOT NULL;

-- 여행 기록은 기존 세부 지역을 우선 사용하고, 세부 지역이 없는 기록은 국가 Region을 사용한다.
ALTER TABLE travel_record
    ADD COLUMN region_id BIGINT NULL AFTER member_id;

UPDATE travel_record tr
    LEFT JOIN region location_region ON location_region.legacy_location_id = tr.location_id
    LEFT JOIN country ON country.id = tr.country_id
    LEFT JOIN region country_region
              ON country_region.region_type = 'COUNTRY'
                  AND country_region.region_code = country.code
SET tr.region_id = COALESCE(location_region.id, country_region.id);

UPDATE travel_record
SET content = ''
WHERE content IS NULL;

ALTER TABLE travel_record
    DROP FOREIGN KEY fk_record_location_country,
    DROP FOREIGN KEY fk_record_country,
    DROP FOREIGN KEY fk_record_location,
    DROP COLUMN country_id,
    DROP COLUMN location_id,
    MODIFY COLUMN region_id BIGINT NOT NULL,
    MODIFY COLUMN content TEXT NOT NULL,
    ADD CONSTRAINT fk_record_region FOREIGN KEY (region_id) REFERENCES region (id),
    ADD INDEX idx_record_member_region (member_id, region_id);

ALTER TABLE region
    DROP COLUMN legacy_location_id,
    DROP COLUMN legacy_parent_id;

DROP TABLE location;
DROP TABLE country;
