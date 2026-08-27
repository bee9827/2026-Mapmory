# ADR 0007. 여행 기록의 국가와 세부 지역 저장 방식

- 상태: 대체됨 (ADR 0008)
- 날짜: 2026-08-11
- 관련: `V4__create_travel_record.sql`, `V9__add_country_to_travel_record.sql`, ADR 0004

---

## 문제

여행 기록은 해외에서는 국가만 선택해 저장할 수 있고, 대한민국에서는 시·군·구를 선택해야 한다. 기존 `travel_record`는 `location_id`만 보관하므로 국가만 선택한 기록을 표현할 수 없다.

## 결정

`travel_record.country_id`를 필수로 저장하고, `location_id`를 선택값으로 둔다.

| 컬럼 | null 허용 | 의미 |
| --- | --- | --- |
| `country_id` | 불가 | 여행 기록의 목적 국가 |
| `location_id` | 가능 | 국가 안의 세부 지역 |

`location_id`가 있으면 `(location_id, country_id)` 복합 외래 키로 해당 지역이 기록의 국가에 속함을 데이터베이스에서 보장한다. 이를 위해 `location(id, country_id)`에 복합 유니크 제약을 추가한다.

## 도메인 규칙

- 해외 기록은 국가만 저장할 수 있다.
- 대한민국(`KR`) 기록은 `location_id`가 필수이며, 연결된 지역은 `DISTRICT`여야 한다.
- 시·도는 ADR 0004에 따라 `location.parent_id`로만 판단한다. `region_code` 접두사로 계층이나 국가를 추론하지 않는다.

`KR`의 세부 규칙과 `DISTRICT` 검증은 행의 다른 값과 지역 유형을 함께 확인해야 하므로 애플리케이션 서비스에서 검증한다. 국가와 지역의 소속 일치는 복합 외래 키가 보장한다.

## 결과

- 국가 단위 여행 기록과 세부 지역 여행 기록을 하나의 테이블로 관리한다.
- 국가 필터는 `travel_record.country_id`만으로 처리할 수 있다.
- `location` 조인 없이도 해외 기록을 표현할 수 있다.
- 기존 V4는 변경하지 않고, V9에서 국가 값을 백필한 뒤 제약을 추가한다.
