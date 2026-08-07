# Mapmory 클라이언트 작업 목록

## 먼저 처리할 일

- [ ] Mapbox 결제수단 등록 및 계정 활성화
- [ ] Mapbox 공개 토큰을 `client/local.properties`에 설정
- [ ] Mapbox SDK 다운로드 토큰을 사용자 Gradle 설정에 설정
- [ ] `:androidApp:assembleDebug` 재검증 및 실제 지도 표시 확인

## 지도

- [ ] `GET /travel-records/map` 응답 형식 확정
- [ ] 행정구역 GeoJSON과 `Location.regionCode` 매핑 규칙 확정
- [ ] 방문 지역 색칠 연결

## 데이터 계층

- [ ] 여행 기록 CRUD API 응답·요청 DTO 확정
- [ ] DTO와 도메인 모델 변환 구현
- [ ] HTTP Repository 구현 및 테스트

