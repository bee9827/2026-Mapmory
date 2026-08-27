-- 기록 미디어 (S3 객체 키만 저장, 파일 실물은 S3)
--
-- [주의] ON DELETE CASCADE 는 DB 레코드만 삭제한다.
--        S3의 실제 파일은 남으므로 애플리케이션에서 별도로 삭제해야 한다.
--        누락 시 고아 객체가 누적된다.
CREATE TABLE record_media (
                              id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
                              travel_record_id BIGINT       NOT NULL,
                              object_key       VARCHAR(500) NOT NULL COMMENT 'mapmory/ 접두사 필수',
                              thumb_key        VARCHAR(500) NULL,
                              sort_order       INT          NOT NULL DEFAULT 0,
                              created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_media_record FOREIGN KEY (travel_record_id)
                                  REFERENCES travel_record (id) ON DELETE CASCADE,
                              CONSTRAINT uk_media_object_key UNIQUE (object_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
