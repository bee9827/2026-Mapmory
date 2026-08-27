-- 회원
-- MySQL에는 UUID 타입이 없으므로 CHAR(36)으로 저장한다.
-- (BINARY(16)이 공간 효율은 좋으나 디버깅이 어려워 MVP에서는 CHAR 사용)
CREATE TABLE member (
                        id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
                        uuid       CHAR(36)    NOT NULL,
                        name       VARCHAR(50) NOT NULL,
                        created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT uk_member_uuid UNIQUE (uuid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- TODO 소셜 로그인 도입 시 provider / provider_id 컬럼을 별도 버전으로 추가한다.
--      (이미 적용된 마이그레이션 파일은 수정하지 말 것)