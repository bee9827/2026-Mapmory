package com.mapmory.backend;

import org.junit.jupiter.api.Test;

/**
 * 애플리케이션 컨텍스트 로딩 검증.
 *
 * 이 테스트 하나가 다음을 모두 커버한다.
 *   - 모든 Flyway 마이그레이션 적용
 *   - ddl-auto: validate 로 엔티티와 테이블 일치 검증
 *   - 빈 생성 및 의존성 주입
 */
class MapmoryBackendApplicationTest extends IntegrationTest {

    @Test
    void loadsApplicationContext() {
    }
}
