제목: 서비스 반환값에서 Region 엔티티를 빼고 지역 값으로 넘기기

## 문제

`open-in-view: false`라서 트랜잭션이 끝난 뒤 지연 로딩 연관을 읽으면 `LazyInitializationException`이 납니다. 지금은 조회마다 `@EntityGraph`를 빠뜨리지 않는 것으로만 막고 있습니다. #319가 그 사례입니다. 주소 매칭용 목록 조회에 `@EntityGraph`가 없어서 장소 선택이 500이 났습니다.

## 범위

엔티티가 트랜잭션 밖에서 응답으로 바뀌는 곳 두 군데입니다.

- 장소 선택: `PlaceSelectionService.select`가 `SelectedPlace(PlaceDetails, Region)`을 반환하고, 컨트롤러에서 `RegionDetailResponse.from(region)`을 호출합니다. 서비스에는 트랜잭션이 없습니다(Google 호출 때문).
- 여행 기록 상세: `TravelRecordDetail`이 `TravelRecord`를 담고, 컨트롤러에서 `TravelRecordDetailResponse.from`이 `travelRecord.getRegion()`으로 `RegionDetailResponse`를 만듭니다.

## 방향

- `region` 모듈이 읽기 트랜잭션(`@Transactional(readOnly = true)`) 안에서 국가·시·도·시·군·구의 코드와 이름을 담은 값 하나(예: `RegionPath`)를 만들어 돌려줍니다.
- `RegionDetailResponse`도 그 값에서 만듭니다. `Region`에서 응답을 만드는 변환은 한 곳에만 둡니다.
- `TravelRecordService`가 기록에 지역을 연결할 때 쓰는 엔티티 조회(`PlaceSelectionService.suggestedRegion`)는 자기 트랜잭션 안이라 그대로 둡니다.
- Geoapify를 지울 때 함께 하면 좌표 경로(`DistrictLocator`, `KoreanDistrictLocator`, `PlaceLookupRouter`)가 사라져서 장소 선택의 지역 판정이 주소 매칭 하나로 줄어듭니다.

## 참고

a1c06cf에서 장소 선택만 `SuggestedRegion` 값으로 바꿔 봤다가 되돌렸습니다. 변환이 여전히 트랜잭션 밖이라 `@EntityGraph`가 없으면 똑같이 깨졌고, `RegionDetailResponse`와 같은 모양의 타입과 변환만 늘었습니다.

## 완료 기준

- 지역 조회의 `@EntityGraph`를 빼도 장소 선택과 여행 기록 상세 통합 테스트가 통과합니다.
- `Region` → 응답 변환이 한 곳에만 있습니다.
