# ADR 0008. 국가와 행정구역을 Region 계층으로 통합

- 상태: 채택
- 날짜: 2026-08-11
- 관련: ADR 0003, ADR 0004, ADR 0007

---

## 문제

해외 기록은 국가 단위로, 대한민국 기록은 시·군·구 단위로 저장해야 한다. `country`와 `location`을 분리하면 `travel_record`가 국가와 세부 지역을 함께 참조해야 하며, 기록 대상의 단계가 달라질 때 저장 규칙과 통계 조회가 복잡해진다.

## 결정

국가와 행정구역을 `region` 테이블 하나의 계층으로 통합한다.

- `COUNTRY → PROVINCE → DISTRICT`를 `parent_id`로 연결한다.
- `travel_record`는 선택한 `region_id` 하나만 참조한다.
- 해외 국가 단위 기록은 `COUNTRY` Region을 참조한다.
- 대한민국 기록은 `DISTRICT` Region을 참조한다.
- `root_id`에는 하위 Region이 속한 국가 Region ID를 저장한다. 국가 Region의 `root_id`는 `NULL`이다.

## 코드 원칙

`region_code`에는 해당 노드의 표준 원본 코드만 저장한다.

| Region 타입 | 코드 체계 |
| --- | --- |
| `COUNTRY` | ISO 3166-1 alpha-2 |
| `PROVINCE` | ISO 3166-2 지역 코드 |
| `DISTRICT` | 행정표준코드 |

코드 체계를 나타내는 별도 컬럼은 두지 않는다. `region_type`으로 코드의 의미를 알 수 있기 때문이다. 또한 `region_code` 접두사로 계층을 유추하지 않고, 계층 판단은 ADR 0004와 같이 `parent_id`로만 수행한다.

## 결과

- 여행 기록은 `region_id` 하나만 가지므로 국가 단위와 세부 지역 기록을 같은 방식으로 저장한다.
- 국가 통계는 `root_id`를 활용해 재귀 조회 없이 빠르게 필터링할 수 있다.
- 시·도·시군구 통계는 선택 Region과 하위 Region을 기준으로 집계한다.
- ADR 0003의 `country`·`location` 분리 결정과 ADR 0007의 `travel_record.country_id` 결정은 이 ADR로 대체한다.
- ADR 0004의 `parent_id` 기반 계층 판단 원칙은 유지한다.
