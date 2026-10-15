package com.mapmory.backend;

import com.mapmory.backend.auth.infrastructure.apple.AppleIdentityAdapter;
import com.mapmory.backend.auth.infrastructure.google.GoogleIdentityAdapter;
import com.mapmory.backend.auth.infrastructure.kakao.KakaoIdentityAdapter;
import com.mapmory.backend.support.MySqlTestContainerSupport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

/**
 * DB가 필요한 통합 테스트의 공통 설정.
 *
 * MySQL 컨테이너는 테스트 JVM에서 하나를 공유한다.
 * 각 테스트는 운영과 같은 트랜잭션 경계로 실행하고, 종료 후 변경 데이터를 별도로 정리한다.
 *
 * 이미지 태그는 운영 RDS와 동일한 8.4로 고정한다. latest 금지.
 *
 * S3 권한 자가진단(ADR 0018)은 끈다. 실제 AWS를 호출하는 진단이라 테스트에서 돌 이유가 없고,
 * 인스턴스 역할이 붙은 러너에서는 운영 버킷을 조회하게 된다.
 * 진단 자체는 S3PermissionSelfCheckTest가 대역으로 검증한다.
 *
 * MockMvc는 쓰지 않는 테스트에도 켠다. 테스트마다 켜고 끄면 컨텍스트 캐시 키가 달라져
 * 같은 설정의 컨텍스트를 새로 띄우게 된다.
 *
 * 소셜 로그인 adapter도 같은 이유로 여기서 spy로 둔다. stub하지 않으면 실제 메서드를 그대로 부르고,
 * stub은 테스트가 끝나면 초기화되므로 이 adapter를 쓰지 않는 테스트의 동작은 바뀌지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "upload.storage.s3.permission-self-check=false")
@Sql(
        scripts = "/db/cleanup-test-data.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public abstract class IntegrationTest extends MySqlTestContainerSupport {

    @MockitoSpyBean
    protected KakaoIdentityAdapter kakaoIdentityAdapter;

    @MockitoSpyBean
    protected AppleIdentityAdapter appleIdentityAdapter;

    @MockitoSpyBean
    protected GoogleIdentityAdapter googleIdentityAdapter;
}
