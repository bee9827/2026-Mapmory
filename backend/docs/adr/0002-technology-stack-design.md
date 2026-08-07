# ADR-0002: 기술 스택 설계

## 날짜

2026-08-07

## 컨텍스트와 문제 정의

Mapmory는 신규 프로젝트로, 지도 기반 여행 기록 API와 이미지 저장을 빠르게 구현해야 한다. 팀이 운영 가능한 기술을 쓰되, 앞으로의 유지보수와 확장 가능성을 해치지 않는 선택이 필요하다.

## 고려한 옵션들

| 영역 | 고려한 옵션 | 선택 |
| --- | --- | --- |
| 런타임·웹 | Java 17 또는 21, 경량 프레임워크 또는 Spring Boot | Java 21 + Spring Boot |
| DB·데이터 접근 | MySQL, PostgreSQL/PostGIS, SQL 직접 작성 또는 JPA | MySQL 8.4 + Spring Data JPA |
| 인증 | 세션, JWT, 임시 사용자 식별 | MVP는 `X-Member-Id`, 이후 JWT |
| 이미지 저장 | 서버 중계·로컬 디스크·S3 직접 전송 | S3 Presigned URL |
| 성능 기술 | 캐시·CDN·검색 엔진 선도입 또는 측정 후 도입 | 측정 후 도입 |

## 결정 근거

- Java 21은 신규 프로젝트에 적합한 LTS이며, Spring Boot는 REST API·검증·예외 처리·DI의 기본 구성을 제공한다.
- 현재 조회는 지역 코드, 사용자, 기록 중심이다. 팀이 익숙한 MySQL과 JPA가 MVP CRUD에 적합하며 PostGIS 수준의 공간 연산은 필요하지 않다.
- S3 직접 전송은 제한된 EC2 자원을 사진 전송에서 분리한다.
- 인증 모델은 미확정이므로, MVP에서는 최소한의 기록 소유권만 검증하고 JWT 정책은 후속으로 확정한다.
- 캐시, CDN, 검색 엔진, 메시지 큐는 문제를 측정한 뒤 도입해야 운영 비용과 복잡도를 피할 수 있다.

## 결정 사항

| 영역 | 기술 | 역할 |
| --- | --- | --- |
| Backend | Java 21, Spring Boot | API, 업무 규칙, 검증, 예외 처리 |
| ORM | Spring Data JPA | 일반 CRUD. 복잡한 조회는 JPQL·네이티브 쿼리 |
| Database | MySQL 8.4 on RDS | 여행 기록·지역·이미지 메타데이터 저장 |
| 임시 인증 | `X-Member-Id` | 사용자별 기록 소유권 식별 |
| 정식 인증 | JWT | 로그인 도입 후 무상태 인증 |
| Web | Nginx | HTTPS 종료, 리버스 프록시 |
| Storage | Amazon S3 | 사진 원본·썸네일 저장, Presigned URL 전송 |
| Deploy·Monitoring | GitHub Actions, CodeDeploy, CloudWatch, SNS | 배포 자동화와 최소 운영 알림 |

로컬·CI·운영 환경의 Java와 MySQL 버전은 각각 21과 8.4로 통일한다.

## 장단점

| 장점 | 단점 |
| --- | --- |
| 팀의 익숙한 기술로 MVP 구현과 장애 대응이 빠르다. | Spring·JPA의 추상화와 동작을 이해하고 관리해야 한다. |
| Java 21 LTS와 JWT 전환 경로로 장기 유지보수 기반을 갖춘다. | 임시 `X-Member-Id`는 인증 수단이 아니므로 공개 운영 전 교체가 필수다. |
| 파일·DB를 분리하고, 과도한 기술 도입을 피한다. | 캐시·CDN이 없어 성능 문제가 생기면 별도 개선이 필요하다. |
