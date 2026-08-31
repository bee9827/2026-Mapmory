CREATE TABLE launch_waitlist (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(254) COLLATE utf8mb4_0900_bin NOT NULL,
    consented_at  DATETIME     NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_launch_waitlist_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
