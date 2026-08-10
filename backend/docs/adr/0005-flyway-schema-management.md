# ADR 0005. 스키마 관리를 Flyway로 한다

- 상태: 채택
- 날짜: 2026-08-10
- 관련: `build.gradle`, `application.yaml`, `src/main/resources/db/migration/`

---

## 문제

팀원 5명이 각자 로컬에서 개발하고, 운영 DB는 RDS 한 대다.
**스키마를 어떻게 일치시킬 것인가.**

---

## 배경

JPA의 `ddl-auto: update`를 쓰면 엔티티 코드에 따라 테이블이 자동 생성된다. 편하지만 문제가 있다.

1. **팀원마다 스키마가 갈린다.** A가 `VARCHAR(200)`, B가 `VARCHAR(100)`으로 엔티티를 작성하면 각자의 로컬 DB가 달라진다.
2**JPA가 무엇을 할지 예측할 수 없다.** 운영에서 컬럼이 변경되면 데이터가 손실될 수 있다.
3**이력이 남지 않는다.** "이 컬럼이 언제 왜 추가됐나"를 알 수 없다.

---

## 결정

### 1. 스키마는 Flyway 마이그레이션 SQL로 관리한다

```
src/main/resources/db/migration/V{n}__{설명}.sql
```

SQL 파일이 jar에 포함되므로 **로컬과 운영에 동일한 SQL이 동일한 순서로 적용**된다.
접속 정보만 프로필로 갈린다.

### 2. `ddl-auto: validate`를 로컬·운영 모두에 적용한다

JPA는 검증만 한다. 엔티티와 실제 테이블이 다르면 애플리케이션이 기동하지 않으므로 불일치를 즉시 발견한다.

| 값 | 동작 | 채택 |
|---|---|---|
| `create` / `create-drop` | 매 기동 시 테이블 재생성 | ✗ |
| `update` | 엔티티 기준 자동 변경 | ✗ |
| **`validate`** | 검증만 | **✓** |
| `none` | 아무것도 안 함 | ✗ (불일치를 놓침) |

### 3. 이미 커밋된 마이그레이션 파일은 수정하지 않는다

Flyway가 체크섬을 저장하므로 수정 시 기동이 실패한다.
로컬은 초기화로 회복되지만 **운영에서는 장애다.** 변경이 필요하면 새 버전 파일을 만든다.

---

## 구현 중 발생한 문제

### Spring Boot 4.x의 자동 설정 모듈 분리

`flyway-core` 의존성만으로는 Flyway가 **아무 로그 없이 동작하지 않았다.**
조건 평가 보고서에 `FlywayAutoConfiguration`이 Positive·Negative 어디에도 없었다 — 자동 설정 클래스 자체가 등록되지 않은 상태였다.

Spring Boot 4.x부터 자동 설정이 기능별 스타터로 분리되었기 때문이다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-flyway'  // 필요
implementation 'org.flywaydb:flyway-mysql'                            // DB별 모듈
```

**학습**: Spring Boot 4.x에서 특정 기능이 이유 없이 동작하지 않으면 전용 스타터 누락을 먼저 의심한다. 원인 파악에 상당한 시간이 소요되었으며, 자료가 거의 없는 영역이다.

### `spring.flyway.url` 명시의 위험

디버깅 과정에서 Flyway에 접속 정보를 직접 지정했더니, Flyway가 datasource가 아닌 **해당 URL로 접속**했다.

```
FlywayUrlCondition @ConditionalOnProperty (spring.flyway.url) matched
```

운영 배포 시 이 값이 로컬로 남아 있으면 마이그레이션이 로컬 DB를 향한다.
→ **`spring.flyway`에는 접속 정보를 두지 않는다.** datasource를 공유하게 한다.

---

## 결과

- 8개 마이그레이션이 로컬과 RDS에 동일하게 적용된다.
- 팀원이 코드를 pull 하고 실행하면 스키마가 자동으로 최신화된다.
- 마스터 데이터(국가 249건, 지역 273건)도 마이그레이션에 포함되어 모든 환경에 동일하게 존재한다.

## 감수한 비용

- 마이그레이션 파일 작성이라는 추가 작업이 생긴다.
- 두 사람이 동시에 같은 버전 번호를 만들면 머지 충돌이 발생한다. → PR 전 최신 버전 확인을 팀 규칙으로 둔다.