package com.mapmory.backend;

import com.mapmory.backend.support.MySqlTestContainerConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * DB가 필요한 통합 테스트의 공통 설정.
 *
 * MySQL 컨테이너를 테스트 설정 빈으로 등록해 Spring 컨텍스트 생명주기에 맞춰 관리한다.
 *
 * 이미지 태그는 운영 RDS와 동일한 8.4로 고정한다. latest 금지.
 */
@SpringBootTest
@Import(MySqlTestContainerConfig.class)
public abstract class IntegrationTest {
}
