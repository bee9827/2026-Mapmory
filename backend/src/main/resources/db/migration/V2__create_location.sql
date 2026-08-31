-- 지역 (자기참조 계층: 시·도 -> 시·군·구)
--
-- [중요] 코드 체계가 레벨마다 다르다.
--   PROVINCE : ISO 3166-2  (서울 11, 강원 42, 제주 49, 세종 50 ...)
--   DISTRICT : 행정표준코드 (세종 36110, 제주 50110, 강원 51110 ...)
--
-- 따라서 region_code 접두사로 상위 지역을 유추하면 안 된다.
-- 예) '50110 제주시'의 앞 두 자리 50은 ISO에서 세종특별자치시다.
-- 계층 관계는 반드시 parent_id 로만 판단한다.
CREATE TABLE location (
                          id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
                          country_id    BIGINT       NOT NULL,
                          parent_id     BIGINT       NULL,
                          region_code   VARCHAR(10)  NOT NULL,
                          name          VARCHAR(100) NOT NULL,
                          location_type VARCHAR(20)  NOT NULL COMMENT 'PROVINCE | DISTRICT',
                          CONSTRAINT fk_location_country FOREIGN KEY (country_id) REFERENCES country (id),
                          CONSTRAINT fk_location_parent  FOREIGN KEY (parent_id)  REFERENCES location (id),
                          CONSTRAINT uk_location_region  UNIQUE (country_id, region_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
