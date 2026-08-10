package com.mapmory.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * DB가 필요한 통합 테스트의 공통 설정.
 *
 * MySQL 컨테이너를 static으로 선언해 JVM 전체에서 한 번만 기동한다.
 * 테스트 클래스마다 새로 띄우면 CI 시간이 급증한다.
 *
 * 이미지 태그는 운영 RDS와 동일한 8.4로 고정한다. latest 금지.
 */
@SpringBootTest
@Testcontainers
public abstract class IntegrationTest {
    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
}
