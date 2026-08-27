-- 여행 기록의 목적 국가는 필수이며, 세부 지역은 선택 사항이다.
-- 기존 기록은 기존 location의 국가로 백필한다.
ALTER TABLE travel_record
    ADD COLUMN country_id BIGINT NULL AFTER member_id;

UPDATE travel_record tr
    JOIN location l ON l.id = tr.location_id
SET tr.country_id = l.country_id;

-- (location_id, country_id) 복합 FK로 선택한 지역이 기록 국가에 속함을 보장한다.
ALTER TABLE location
    ADD CONSTRAINT uk_location_id_country UNIQUE (id, country_id);

ALTER TABLE travel_record
    MODIFY COLUMN country_id BIGINT NOT NULL,
    MODIFY COLUMN location_id BIGINT NULL,
    ADD CONSTRAINT fk_record_country FOREIGN KEY (country_id) REFERENCES country (id),
    ADD CONSTRAINT fk_record_location_country
        FOREIGN KEY (location_id, country_id) REFERENCES location (id, country_id);
