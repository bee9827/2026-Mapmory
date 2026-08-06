# Mapmory Client API Contract

## Base URL

/api/v1

## 임시 사용자 식별

모든 사용자 전용 API는 다음 헤더를 사용한다.

X-Member-Id: 10

실제 로그인 도입 뒤에는 `Authorization: Bearer <token>`으로 교체한다.

## 지도 방문 지역

GET /travel-records/map

## 여행 기록 목록

GET /travel-records?locationId={id}&keyword={keyword}&page={page}&size={size}

## 통계

GET /travel-statistics

## 클라이언트 구현 전 확정할 항목

아래 항목은 아직 DTO나 HTTP Repository에 임의로 반영하지 않는다.

### 여행 기록 CRUD

- 상세 조회, 생성, 수정, 삭제의 HTTP method와 경로
- 생성·수정 요청 본문의 필드명과 `mediaObjectKeys` 전달 형식
- 단건·목록 응답의 필드명, 날짜·시각 형식, 페이지 응답 구조
- 공통 오류 응답 형식과 오류 코드

### 장소 선택

- 국가와 행정구역을 조회하는 API 경로와 응답 구조
- `travelRecord.locationId`가 항상 최종 행정구역인 `DISTRICT`를 가리키는지
- 지도 GeoJSON 속성과 `Location.regionCode`의 정확한 매핑 규칙

### 지도와 통계

- `GET /travel-records/map` 응답이 방문한 `locationId` 목록인지, `regionCode` 목록인지
- `GET /travel-statistics`가 제공할 국가·지역별 방문 수와 기록 수의 응답 구조

## 확정된 데이터 정책

- 사진은 만료되는 URL이 아닌 `objectKey`를 영구 저장한다. 조회 응답에서만 서버가 `url`을 생성한다.
- 날짜를 입력하지 않으면 `startDate`, `endDate` 모두 `null`이다.
- 시작일만 입력하면 서버가 `endDate`를 시작일과 같은 날짜로 저장한다.
- 종료일이 시작일보다 빠른 기록은 허용하지 않는다.
