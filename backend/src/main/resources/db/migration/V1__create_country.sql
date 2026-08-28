-- 국가 (ISO 3166-1 alpha-2)
CREATE TABLE country (
                         id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
                         code CHAR(2)      NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         CONSTRAINT uk_country_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;