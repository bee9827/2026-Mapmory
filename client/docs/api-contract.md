# Mapmory Client API Contract

## Base URL

/api/v1

## 임시 사용자 식별

모든 사용자 전용 API는 다음 헤더를 사용한다.

X-Member-Id: 10

실제 로그인 도입 뒤에는 `Authorization: Bearer <token>`으로 교체한다.

## 지도 방문 지역

GET /travel-records/map

지도 SDK의 역할은 지도 타일·스타일·지도 피처를 표시하는 것이다. Mapbox가
Mapmory의 `Location.regionCode`를 제공한다고 가정하지 않는다.

- 방문 지역 데이터는 Mapmory 서버가 제공한다.
- 지도 색칠은 서버의 `regionCode`와 클라이언트가 사용하는 행정구역 GeoJSON의
  속성을 매칭해서 처리한다.
- Mapbox SDK에서 제공하는 지도 피처 속성이나 내부 ID를 Mapmory 지역 코드로
  직접 저장하지 않는다.
- 위치 권한을 사용하지 않는 한 사용자 GPS를 방문 지역 판정에 사용하지 않는다.

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

### 사진 업로드

- Presigned URL 발급 API의 HTTP method·경로와 요청 필드(파일명, MIME type, 크기)
- 발급 응답의 `objectKey`, 업로드 URL, 필수 헤더, 만료 시각 형식
- 여행 기록 생성·수정 전에 업로드한 파일의 취소·실패 시 정리 정책
- 첨부 가능한 사진 수, 파일 크기, MIME type 제한

### 장소 선택

- 국가와 행정구역을 조회하는 API 경로와 응답 구조
- `travelRecord.locationId`가 항상 최종 행정구역인 `DISTRICT`를 가리키는지
- 지도 GeoJSON 속성과 `Location.regionCode`의 정확한 매핑 규칙

### 지도와 통계

- `GET /travel-records/map` 응답이 방문한 `locationId` 목록인지, `regionCode` 목록인지
- `GET /travel-statistics`가 제공할 국가·지역별 방문 수와 기록 수의 응답 구조

## 클라이언트 구현 순서

1. API 응답 필드와 오류 형식 확정
2. DTO와 도메인 모델 변환 규칙 작성
3. HTTP Repository 구현 및 테스트
4. 지도 방문 지역 응답과 GeoJSON 속성 매핑 확정
5. Mapbox 지도에 방문 지역 색칠 연결

## 확정된 데이터 정책

- 사진은 만료되는 URL이 아닌 `objectKey`를 영구 저장한다. 조회 응답에서만 서버가 `url`을 생성한다.
- 사진 첨부 흐름은 `Presigned URL 발급 → 클라이언트 직접 업로드 → travel record에 objectKey 전달`이다.
- 날짜를 입력하지 않으면 `startDate`, `endDate` 모두 `null`이다.
- 시작일만 입력하면 서버가 `endDate`를 시작일과 같은 날짜로 저장한다.
- 종료일만 입력하는 기록은 허용하지 않는다.
- 종료일이 시작일보다 빠른 기록은 허용하지 않는다.
