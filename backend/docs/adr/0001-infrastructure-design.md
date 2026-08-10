# ADR-0001: 인프라 설계

## 날짜

2026-08-07

## 컨텍스트와 문제 정의

MVP는 핵심 지도·기록 기능을 빠르게 검증해야 하며, 월 인프라 예산은 약 $50 ~ $80이다. 애플리케이션, 데이터베이스, 이미지 파일을 안전하게 분리하면서도 아직 확인되지 않은 트래픽을 위해 과도한 운영 복잡도와 비용을 만들지 않아야 한다.

## 고려한 옵션들

| 옵션 | 설명 |
| --- | --- |
| 단일 EC2에 애플리케이션·DB·파일 저장 | 가장 단순하지만 데이터와 서버 자원이 한 인스턴스에 결합된다. |
| **EC2 + RDS + S3** | 애플리케이션, DB, 이미지 파일을 각각 분리한다. |
| ALB·다중 EC2·Multi-AZ RDS·CloudFront | 가용성과 확장성을 우선하는 운영 구성이다. |

## 결정 근거

- DB를 RDS로 분리하면 EC2의 메모리 경합과 인스턴스 장애에 따른 데이터 유실 위험을 낮춘다.
- 이미지는 S3 Presigned URL로 앱이 직접 전송하면 EC2와 JVM이 파일을 중계하지 않아도 된다.
- 단일 서버 MVP에서는 ALB가 불필요하고, 개인 사진 위주의 조회에서는 초기 CloudFront 캐시 효과가 작다.
- `t4g.small`과 `db.t4g.micro` 구성은 현재 예산 범위에서 운영 가능하다.

## 결정 사항

```mermaid
flowchart LR
  App["Android 앱"] -->|"HTTPS"| Nginx["Nginx · EC2"]
  Nginx -->|"Reverse proxy"| Server["Spring Boot · EC2"]
  Server --> RDS["MySQL 8.4 · RDS"]
  App -->|"Presigned PUT/GET"| S3["Private S3"]
```

- EC2 `t4g.small` 1대(Ubuntu 22.04 `arm64`)에서 Nginx와 Spring Boot를 실행한다.
- Nginx가 Let's Encrypt TLS 종료와 리버스 프록시를 담당한다.
- RDS MySQL 8.4 `db.t4g.micro`는 퍼블릭 접근을 막고 EC2 보안 그룹에서만 접근하도록 한다.
- S3는 퍼블릭 접근을 차단한다. 모든 객체 키에는 `mapmory/` 접두사를 붙이고 Presigned URL로만 업로드·조회한다.
- EC2는 SSM Session Manager로 접속하며 SSH 포트는 열지 않는다.
- CloudWatch 로그는 7일 보관하고, AWS Budgets $40 알람을 둔다. (아직 추가 x)

## 장단점

| 장점 | 단점 |
| --- | --- |
| 애플리케이션·DB·이미지 파일을 분리해 장애와 자원 경계를 명확히 한다. | EC2와 RDS가 단일 장애점이다. |
| 비용과 운영 복잡도를 MVP 수준으로 유지한다. | 트래픽 증가 시 즉시 수평 확장할 수 없다. |
| 파일 전송이 서버 자원을 사용하지 않는다. | S3 업로드 상태와 고아 객체를 관리해야 한다. |

CPU 크레딧 소진·OOM, 디스크 80% 초과, 조회·사진 로딩 지연, 단일 장애점 문제, 월 비용 $60 초과가 관찰되면 EC2 확장, CloudFront, ALB·다중 인스턴스 또는 Multi-AZ를 재검토한다.
