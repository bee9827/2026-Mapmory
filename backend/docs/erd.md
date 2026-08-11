# Mapmory ERD

> 기준일: 2026-08-11 · 범위: 지역 선택, 지도 마킹, 여행 기록, 이미지 첨부


<img width="588" height="364" alt="스크린샷 2026-08-11 오후 3 04 34" src="https://github.com/user-attachments/assets/1d24fb8f-2ca7-400a-8525-cea938246569" />


## 테이블 역할

| 테이블 | 역할 |
| --- | --- |
| `member` | 여행 기록 소유자. `uuid`는 외부 노출용 식별자다. |
| `region` | 국가와 행정구역을 하나의 계층으로 관리한다. |
| `travel_record` | 회원이 선택한 국가 또는 최종 행정구역에 남기는 기록이다. |
| `record_media` | S3 객체 키, 썸네일 키, 노출 순서를 보관한다. |

## Region 계층과 코드

| `region_type` | `parent_id` | `root_id` | `region_code` 체계 |
| --- | --- | --- | --- |
| `COUNTRY` | `NULL` | `NULL` | ISO 3166-1 alpha-2 |
| `PROVINCE` | 국가 Region | 국가 Region ID | ISO 3166-2 지역 코드 |
| `DISTRICT` | 시·도 Region | 국가 Region ID | 행정표준코드 |

`region_code`에는 해당 노드의 원본 표준 코드만 저장한다. 계층 관계는 어떤 경우에도 코드 접두사가 아닌 `parent_id`로만 판단한다.

`root_id`는 국가별 통계와 필터를 위한 보조 컬럼이다. 직접 부모 관계를 표현하는 `parent_id`를 대체하지 않는다.

## 예시 데이터

| id | parent_id | root_id | region_code | region_type | name |
| ---: | ---: | ---: | --- | --- | --- |
| 1 | NULL | NULL | `KR` | COUNTRY | 대한민국 |
| 2 | 1 | 1 | `49` | PROVINCE | 제주특별자치도 |
| 3 | 2 | 1 | `50110` | DISTRICT | 제주시 |
| 4 | NULL | NULL | `JP` | COUNTRY | 일본 |

| travel_record.id | member_id | region_id | 의미 |
| ---: | ---: | ---: | --- |
| 101 | 10 | 3 | 대한민국 → 제주특별자치도 → 제주시 기록 |
| 102 | 10 | 4 | 일본 국가 단위 기록 |

## 무결성 및 조회 규칙

- `travel_record.region_id`는 필수다.
- 해외 기록은 `COUNTRY` Region을 참조한다.
- 대한민국 기록은 `DISTRICT` Region만 참조한다. 이 규칙은 서비스에서 검증한다.
- 국가별 통계는 `region.id = 국가 ID OR region.root_id = 국가 ID` 조건으로 조회한다.
- 시·도·시군구 통계는 요청 Region과 그 하위 Region에 연결된 기록을 집계한다.
- 권장 인덱스는 `region(parent_id)`, `region(root_id)`, `travel_record(member_id, region_id)`다.
