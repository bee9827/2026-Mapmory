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
`upstream/main`과 성능 개선 브랜치를 같은 Docker daemon에서 측정했다. 컴파일 차이를 제외하기 위해
양쪽 모두 `testClasses`를 먼저 실행한 뒤 `./gradlew.bat cleanTest test --no-daemon --console=plain`
명령으로 세 번씩 측정했다. 실행 순서는 순서 편향을 줄이기 위해 개선 전(A)과 개선 후(B)를
`A-B-B-A-A-B`로 배치했다.

| 지표 | 구분 | 1회 | 2회 | 3회 | 중앙값 |
| --- | --- | ---: | ---: | ---: | ---: |
| 테스트 스위트 합계 | 개선 전 | 119.580초 | 113.608초 | 126.997초 | 119.580초 |
| 테스트 스위트 합계 | 개선 후 | 39.470초 | 38.040초 | 46.222초 | 39.470초 |
| 전체 경과 시간 | 개선 전 | 141.182초 | 130.057초 | 144.862초 | 141.182초 |
| 전체 경과 시간 | 개선 후 | 54.102초 | 52.426초 | 62.143초 | 54.102초 |

매 실행에서 동일한 119개 테스트가 모두 통과했다. MySQL 시작 횟수는 개선 전에는 매번 5회,
개선 후에는 매번 1회였다. 테스트 메서드 합계의 중앙값은 각각 5.045초와 5.317초로 비슷했다.
중앙값 기준으로 테스트 스위트 합계는 약 67.0%, 전체 경과 시간은 약 61.7% 감소했다.
따라서 개선 효과는 테스트 로직이 아니라 컨테이너와 테스트 환경 준비 비용이 줄어든 결과로 판단한다.

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

2026-08-20 세 번의 개선 후 측정에서 해당 메시지는 매번 1회 출력됐고, 매번 119개 테스트가
실패 없이 통과했다.
