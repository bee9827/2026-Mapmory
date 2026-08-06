# Mapmory Client API Contract

## Base URL

/api/v1

## 임시 사용자 식별

모든 사용자 전용 API는 다음 헤더를 사용한다.

X-Member-Id: 10

## 지도 방문 지역

GET /travel-records/map

## 여행 기록 목록

GET /travel-records?locationId={id}&keyword={keyword}&page={page}&size={size}

## 통계

GET /travel-statistics