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
| `GET` | `/travel-records/statistics` * | 선택 지역과 하위 지역의 기록 수 조회 |

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

`objectKeys`는 배열 순서대로 `record_media.sort_order`를 0부터 부여해 저장한다. 값이 없거나 `null`이면 미디어를 생성하지 않는다.

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

현재 회원의 기록을 생성일시 내림차순으로 조회한다.

| 파라미터 | 필수 | 설명 |
| --- | --- | --- |
| `countryCode` | 아니요 | 선택 국가 자체와 `root_id`가 해당 국가인 모든 하위 Region 기록 |
| `provinceCode` | 아니요 | 선택 시도 자체와 `parent_id`가 해당 시도인 시·군·구 기록. `countryCode` 필수 |
| `districtCode` | 아니요 | 선택 시·군·구에 직접 저장된 기록. `countryCode`, `provinceCode` 필수 |
| `page` | 아니요 | 기본값 `0` |
| `size` | 아니요 | 기본값 `20` |

#### 조회 예시

```http
# 내 전체 기록
GET /api/v1/travel-records

# 대한민국과 모든 하위 지역 기록
GET /api/v1/travel-records?countryCode=KR

# 제주특별자치도와 하위 시·군·구 기록
GET /api/v1/travel-records?countryCode=KR&provinceCode=49

# 제주시 기록
GET /api/v1/travel-records?countryCode=KR&provinceCode=49&districtCode=50110
```

#### Response `200 OK`

```json
{
  "data": {
    "items": [
      {
        "id": 101,
        "title": "비 오는 날의 제주시",
        "regionName": "제주시",
        "startDate": "2026-08-11",
        "endDate": null,
        "thumbnailUrl": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

목록에는 본문과 전체 미디어 목록을 포함하지 않는다. `thumbnailUrl` 필드는 후속 구현을 위해 포함했으며 현재는 `null`을 반환한다. `keyword` 검색과 페이지 크기 최대값 검증은 아직 구현하지 않았다.

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

## 5. 지역별 여행 기록 통계 API

### 선택 지역 기록 수 조회

`GET /api/v1/travel-records/statistics`

| 파라미터 | 필수 | 설명 |
| --- | --- | --- |
| `countryCode` | 예 | 통계를 낼 국가 |
| `provinceCode` | 아니요 | 특정 시도 통계 |
| `districtCode` | 아니요 | 특정 시군구 통계 |

#### 요청 예시

```http
GET /api/v1/travel-records/statistics?countryCode=KR&provinceCode=49
X-Member-Id: 10
```

#### Response `200 OK`

```json
{
  "data": {
    "countryCode": "KR",
    "provinceCode": "49",
    "districtCode": null,
    "recordCount": 5
  }
}
```

서버는 요청한 Region 자신과 모든 하위 Region에 저장된 현재 회원의 기록을 집계한다.

| 요청 | 집계 범위 |
| --- | --- |
| `countryCode=KR` | 대한민국 및 모든 시도·시군구 기록 |
| `countryCode=KR&provinceCode=49` | 제주특별자치도 및 하위 시군구 기록 |
| `countryCode=KR&provinceCode=49&districtCode=50110` | 제주시 기록 |
