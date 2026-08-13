# Mapmory MVP API 명세

> 기준일: 2026-08-11 · 범위: 지역 선택, 지도 마킹, 여행 기록, 이미지 첨부

## 1. 기본 정보

| 항목 | 값 |
| --- | --- |
| Base URL | `/api/v1` |
| 요청·응답 형식 | `application/json` |
| 오류 응답 형식 | `application/problem+json` |
| 날짜 형식 | `YYYY-MM-DD` |
| 임시 사용자 식별 | `X-Member-Id` 요청 헤더 |
| 페이지네이션 | `page` 기본 0, `size` 기본 20·최대 100 |

여행 기록은 비공개이며 작성자 본인만 조회·생성·수정·삭제할 수 있다. 다른 회원의 기록 ID를 요청해도 존재 여부를 숨기기 위해 `404`를 반환한다.

### API 목록

`*`는 `X-Member-Id` 헤더가 필요한 API다.

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `GET` | `/countries` | 국가 목록 조회 |
| `POST` | `/uploads/presigned-urls` * | 이미지 업로드용 Presigned URL 발급 |
| `POST` | `/travel-records` * | 여행 기록 생성 |
| `GET` | `/travel-records` * | 내 여행 기록 목록 조회 |
| `GET` | `/travel-records/{travelRecordId}` * | 내 여행 기록 상세 조회 |
| `PUT` | `/travel-records/{travelRecordId}` * | 내 여행 기록 전체 수정 |
| `DELETE` | `/travel-records/{travelRecordId}` * | 내 여행 기록 삭제 |
| `GET` | `/travel-records/map-summary/regions/roots` * | 루트 Region별 지도 색칠 정보 조회 |
| `GET` | `/travel-records/map-summary/regions/{regionId}/children` * | 직속 하위 Region별 지도 색칠 정보 조회 |

### 공통 응답

성공 응답은 `data`로 감싼다.

```json
{
  "data": {}
}
```

오류 응답은 RFC 9457 Problem Details를 사용한다.

```json
{
  "title": "요청 값이 올바르지 않습니다.",
  "status": 400,
  "detail": "1개의 필드가 유효하지 않습니다.",
  "instance": "/api/v1/travel-records",
  "code": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "countryCode",
      "detail": "대문자 2자의 ISO-2 형식이어야 합니다."
    }
  ]
}
```

| 상태 | 의미 |
| --- | --- |
| `200 OK` | 조회·수정 성공 |
| `201 Created` | 생성 성공 |
| `204 No Content` | 삭제 성공 |
| `400 Bad Request` | 형식·유효성·업무 규칙 오류 |
| `404 Not Found` | 리소스 없음 또는 타인 기록 |
| `409 Conflict` | 업로드 상태 충돌 |

## 2. Country API

### 국가 목록 조회

`GET /api/v1/countries`

`region_type = COUNTRY`인 Region을 반환한다. 지역 및 지도 경계 데이터는 안드로이드 앱의 로컬 데이터로 관리하므로, 서버는 별도 지역 목록 API를 제공하지 않는다.

#### Response `200 OK`

```json
{
  "data": [
    {
      "countryCode": "KR",
      "name": "대한민국"
    },
    {
      "countryCode": "JP",
      "name": "일본"
    }
  ]
}
```

## 3. Upload API

이미지는 API 서버를 거치지 않고 S3에 직접 업로드한다. 서버는 UUID 기반 `objectKey`만 발급하며, DB에는 만료되는 Presigned URL이 아닌 `objectKey`를 저장한다.

### 이미지 업로드 URL 발급

`POST /api/v1/uploads/presigned-urls`

#### Request Body

```json
{
  "files": [
    {
      "fileName": "jeju-trip.jpg",
      "contentType": "image/jpeg",
      "fileSize": 3145728
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `files` | Object[] | 예 | 1개 이상 |
| `files[].fileName` | String | 예 | 로그·확장자 확인용 원본 파일명 |
| `files[].contentType` | String | 예 | 허용 이미지 MIME 타입 |
| `files[].fileSize` | Long | 예 | 바이트 단위 파일 크기 |

#### Response `200 OK`

```json
{
  "data": {
    "uploads": [
      {
        "objectKey": "travel-records/10/550e8400-e29b-41d4-a716-446655440000.jpg",
        "presignedUrl": "https://...",
        "method": "PUT",
        "contentType": "image/jpeg",
        "expiresIn": 300
      }
    ]
  }
}
```

업로드 후 24시간 안에 여행 기록과 연결되지 않은 객체는 고아 객체로 정리한다.

## 4. Travel Record API

### 지역 선택 규칙

클라이언트는 Region의 DB ID가 아닌 표준 코드 경로를 전달한다. 서버는 `parent_id` 관계로 경로를 검증한 뒤 최종 Region의 ID만 `travel_record.region_id`에 저장한다.

| 기록 대상 | 요청 값 | 저장되는 Region |
| --- | --- | --- |
| 해외 국가 | `countryCode` | `COUNTRY` |
| 대한민국 시·군·구 | `countryCode`, `provinceCode`, `districtCode` | `DISTRICT` |

- `countryCode = KR`이면 `provinceCode`, `districtCode`가 모두 필수다.
- `provinceCode`는 선택 국가의 직접 자식 `PROVINCE`여야 한다.
- `districtCode`는 선택 시도의 직접 자식 `DISTRICT`여야 한다.
- 해외에서는 MVP 기준 국가 단위만 허용한다.
- 코드 접두사로 부모 지역을 추론하지 않는다.

### 여행 기록 생성

`POST /api/v1/travel-records`

#### Request Body — 대한민국 기록

```json
{
  "countryCode": "KR",
  "provinceCode": "49",
  "districtCode": "50110",
  "title": "비 오는 날의 제주시",
  "content": "골목을 걸으며 오래된 가게들을 기록했다.",
  "startDate": "2026-08-11",
  "endDate": null,
  "objectKeys": [
    "travel-records/10/550e8400-e29b-41d4-a716-446655440000.jpg"
  ]
}
```

#### Request Body — 해외 국가 기록

```json
{
  "countryCode": "JP",
  "provinceCode": null,
  "districtCode": null,
  "title": "일본 여행",
  "content": "",
  "startDate": "2026-08-11",
  "endDate": null,
  "objectKeys": []
}
```

| 필드 | 타입 | 필수 | 제약조건 |
| --- | --- | --- | --- |
| `countryCode` | String | 예 | 존재하는 ISO 3166-1 alpha-2 코드 |
| `provinceCode` | String | 조건부 | `KR`이면 필수 |
| `districtCode` | String | 조건부 | `KR`이면 필수 |
| `title` | String | 예 | 최대 200자, 빈 문자열·공백 허용 |
| `content` | String | 예 | 빈 문자열·공백 허용 |
| `startDate` | LocalDate | 예 | `YYYY-MM-DD` |
| `endDate` | LocalDate | 아니요 | 시작일보다 빠를 수 없음 |
| `objectKeys` | String[] | 아니요 | 업로드 완료된 Object Key 목록 |

#### Response `201 Created`

```json
{
  "data": {
    "id": 101
  }
}
```

| 상태 | `code` | 조건 |
| --- | --- | --- |
| `404` | `COUNTRY_NOT_FOUND` | 국가 코드가 존재하지 않음 |
| `400` | `REGION_REQUIRED` | 한국 기록에 시도 또는 시군구가 없음 |
| `404` | `REGION_NOT_FOUND` | 요청 지역이 존재하지 않음 |
| `400` | `INVALID_REGION_HIERARCHY` | 요청 지역의 부모 관계가 맞지 않음 |
| `400` | `INVALID_REGION_TYPE` | 한국 기록의 최종 지역이 `DISTRICT`가 아님 |
| `400` | `INVALID_TRAVEL_DATE_RANGE` | 종료일이 시작일보다 빠름 |
| `400` | `INVALID_OBJECT_KEY` | Object Key 형식 또는 소유자가 올바르지 않음 |
| `409` | `OBJECT_NOT_UPLOADED` | S3 업로드가 확인되지 않음 |

### 여행 기록 목록 조회

`GET /api/v1/travel-records`

| 파라미터 | 필수 | 설명 |
| --- | --- | --- |
| `countryCode` | 아니요 | 선택 국가와 하위 Region 기록 필터 |
| `provinceCode` | 아니요 | 선택 시도와 하위 Region 기록 필터. `countryCode` 필수 |
| `districtCode` | 아니요 | 선택 시군구 기록 필터. `provinceCode` 필수 |
| `keyword` | 아니요 | 제목·본문 검색 |
| `page` | 아니요 | 기본값 `0` |
| `size` | 아니요 | 기본값 `20`, 최대 `100` |

### 여행 기록 상세 조회

`GET /api/v1/travel-records/{travelRecordId}`

#### Response `200 OK`

```json
{
  "data": {
    "id": 101,
    "region": {
      "countryCode": "KR",
      "provinceCode": "49",
      "districtCode": "50110",
      "name": "제주시"
    },
    "title": "비 오는 날의 제주시",
    "content": "골목을 걸으며 오래된 가게들을 기록했다.",
    "startDate": "2026-08-11",
    "endDate": null,
    "media": [
      {
        "id": 1,
        "objectKey": "travel-records/10/550e8400-e29b-41d4-a716-446655440000.jpg",
        "sortOrder": 0
      }
    ]
  }
}
```

### 여행 기록 수정 및 삭제

- `PUT /api/v1/travel-records/{travelRecordId}`: 생성과 같은 요청 본문으로 전체 수정한다.
- `DELETE /api/v1/travel-records/{travelRecordId}`: 기록을 삭제한다. 연결된 `record_media` 행은 CASCADE 삭제한다. S3 객체 삭제는 별도 처리한다.

## 5. 지도 요약 API

지도 요약 응답에는 현재 회원의 기록이 있는 Region만 포함한다. 응답에 없는 Region은 앱에서 `count = 0`, `level = 0`으로 처리한다.

### 루트 Region별 지도 색칠 정보 조회

`GET /api/v1/travel-records/map-summary/regions/roots`

#### Response `200 OK`

```json
{
  "data": [
    {
      "regionId": 1,
      "code": "KR",
      "regionType": "COUNTRY",
      "name": "대한민국",
      "count": 12,
      "level": 3
    }
  ]
}
```

- 기록이 있는 `region_type = COUNTRY` Region을 `code` 오름차순으로 반환한다.
- 현재 회원의 국가 Region 기록과 해당 국가의 모든 하위 Region 기록을 합산한다.
- `regionId`는 후속 하위 Region 지도 요약 요청에 사용한다.
- `code`는 안드로이드 로컬 지도 데이터와 매칭하는 표준 코드다.

| 기록 수 | `level` |
| ---: | --- |
| `0` | `0` |
| `1~2` | `1` |
| `3~5` | `2` |
| `6 이상` | `3` |

### 직속 하위 Region별 지도 색칠 정보 조회

`GET /api/v1/travel-records/map-summary/regions/{regionId}/children`

이전 지도 요약 응답에서 받은 `regionId`를 경로에 전달한다.

#### 요청 예시

```http
GET /api/v1/travel-records/map-summary/regions/1/children
X-Member-Id: 10
```

#### Response `200 OK`

```json
{
  "data": [
    {
      "regionId": 15,
      "code": "49",
      "regionType": "PROVINCE",
      "name": "제주특별자치도",
      "count": 5,
      "level": 2
    }
  ]
}
```

- `COUNTRY` ID를 전달하면 기록이 있는 직속 `PROVINCE`를 반환한다.
- `PROVINCE` ID를 전달하면 기록이 있는 직속 `DISTRICT`를 반환한다.
- 각 결과 Region 자신과 모든 하위 Region에 저장된 현재 회원의 기록을 합산한다.
- 정렬 및 색상 단계 규칙은 국가별 조회와 같다.

| 상태 | `code` | 조건 |
| --- | --- | --- |
| `400` | `VALIDATION_ERROR` | `regionId`가 양수가 아님 |
| `404` | `REGION_NOT_FOUND` | 상위 Region이 존재하지 않음 |
