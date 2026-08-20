# Testcontainers 테스트 성능 개선

## 문제

`MySQLContainer`를 Spring Bean으로 생성하면 서로 다른 `ApplicationContext`마다 컨테이너가 만들어진다.
전체 테스트에서는 `@SpringBootTest`, `@DataJpaTest`, `@MockitoBean` 등의 구성 차이로 컨텍스트가 나뉘어
MySQL 시작 비용이 반복됐다.

## 공유 방식

- `MySqlTestContainerSupport`의 정적 필드가 테스트 worker JVM마다 MySQL 컨테이너 하나를 시작한다.
- 컨테이너를 Spring Bean으로 등록하지 않아 개별 `ApplicationContext`의 종료와 생명주기를 분리한다.
- `@DynamicPropertySource`로 같은 컨테이너의 JDBC 접속 정보를 각 테스트 컨텍스트에 공급한다.
- 통합 테스트는 운영과 같은 트랜잭션 경계로 실행하고, `@Sql`로 각 테스트 종료 후 변경 데이터를 삭제한다.
- `@DataJpaTest`는 Spring의 기본 테스트 트랜잭션 롤백을 그대로 사용한다.
- Testcontainers의 영구 재사용 옵션은 사용하지 않는다. 테스트 프로세스가 끝나면 컨테이너도 정리된다.

## 측정 결과

측정 환경은 Windows, Docker Desktop, JDK 21, MySQL 8.4이다. 2026-08-20에 PR #95가 병합된
`upstream/main`과 성능 개선 브랜치를 같은 Docker daemon에서 순서대로 실행했다. 두 실행 모두
`./gradlew.bat cleanTest test --console=plain` 명령을 사용했다. 각 버전을 한 번씩 실행한 값이므로
개선 방향을 확인하는 기준이며, 절대 시간과 정확한 감소율은 실행 환경에 따라 달라질 수 있다.

| 구분 | 테스트 수 | MySQL 시작 횟수 | 테스트 메서드 합계 | 테스트 스위트 합계 | 전체 경과 시간 |
| --- | ---: | ---: | ---: | ---: | ---: |
| 개선 전 (`upstream/main`) | 119 | 5 | 7.033초 | 139.446초 | 149.677초 |
| 개선 후 | 119 | 1 | 7.287초 | 52.020초 | 62.316초 |

동일한 119개 테스트가 모두 통과했다. MySQL 시작 횟수는 5회에서 1회로 줄었고, 테스트 스위트
합계는 약 62.7%, 전체 경과 시간은 약 58.4% 감소했다. 테스트 메서드 합계는 거의 같으므로
개선 효과는 테스트 로직이 아니라 컨테이너와 테스트 환경 준비 비용이 줄어든 결과로 판단한다.

### MVC 슬라이스 테스트 전환 후

2026-08-19에 `RegionMapSummaryControllerTest`를 `@SpringBootTest` 기반 통합 테스트에서
`@WebMvcTest` 기반 MVC 슬라이스 테스트로 전환한 뒤 측정했다.

| 실행 범위 | 실행 명령 | 결과 | Gradle 경과 시간 |
| --- | --- | ---: | ---: |
| `RegionMapSummaryControllerTest` | `./gradlew.bat test --tests "com.mapmory.backend.travelrecord.mapsummary.controller.RegionMapSummaryControllerTest" --console=plain` | 6개 통과 | 11초 |
| 전체 테스트 | `./gradlew.bat test --console=plain` | 전체 통과 | 58초 |

컨트롤러 단독 실행은 MVC 계층과 필요한 보안 구성만 로드하며 MySQL 컨테이너를 시작하지 않는다.
전체 테스트의 58초는 `cleanTest`와 `--info` 없이 실행한 별도 측정값이므로 위의 53.73초와 직접적인
성능 비교 수치로 사용하지 않고, 슬라이스 전환 후 전체 회귀 테스트가 통과했음을 확인하는 기록으로 남긴다.

## 확인 방법

`--info` 로그에서 다음 메시지의 개수를 확인한다.

```text
Container mysql:8.4 is starting
```

2026-08-20 측정에서는 해당 메시지가 1회 출력됐고, 119개 테스트가 실패 없이 통과했다.
