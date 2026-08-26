# Mapmory 클라이언트 작업 목록

## 먼저 처리할 일

- [x] 외부 지도 SDK 의존성 제거
- [x] `:androidApp:assembleDebug` 재검증

## 지도

- [ ] `GET /travel-records/map` 응답 형식 확정
- [x] 행정구역 GeoJSON과 `Location.regionCode` 매핑 규칙 확정
- [ ] 방문 지역 색칠 연결
- [ ] 지도 데이터·렌더링 방식 결정

## 데이터 계층

- [ ] 여행 기록 CRUD API 응답·요청 DTO 확정
- [ ] DTO와 도메인 모델 변환 구현
- [ ] HTTP Repository 구현 및 테스트

## 품질·모니터링

- [x] Android 1차 모니터링 기준과 실행 방법 문서화
- [x] 사진 추천 성능 로그와 측정 문서 연결
- [ ] 앱 시작·지도 전환 Macrobenchmark 추가
- [ ] 원격 크래시 수집 도입 여부 결정
