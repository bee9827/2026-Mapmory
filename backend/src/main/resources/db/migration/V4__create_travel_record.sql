-- 여행 기록
--
-- start_date / end_date 는 DATE 타입이다.
-- "8월 5일에 여수에 갔다"는 시간대와 무관한 사실이므로 DATETIME을 쓰지 않는다.
-- 반면 created_at 은 기록이 생성된 '시점'이므로 DATETIME(UTC)으로 저장한다.
CREATE TABLE travel_record (
                               id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
                               member_id   BIGINT       NOT NULL,
                               location_id BIGINT       NOT NULL,
                               title       VARCHAR(200) NOT NULL,
                               content     TEXT,
                               start_date  DATE         NOT NULL,
                               end_date    DATE,
                               created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_record_member   FOREIGN KEY (member_id)   REFERENCES member (id),
                               CONSTRAINT fk_record_location FOREIGN KEY (location_id) REFERENCES location (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;