# Mapmory Play Console 제출 체크리스트

기준: `main` 출시 AAB에서 실제로 실행되는 기능.

## 제출 정보

| 항목 | 입력값 |
| --- | --- |
| 개인정보처리방침 | GitHub Pages로 공개한 `docs/privacy-policy.html`의 HTTPS URL |
| 데이터 수집·공유 | 아니오 |
| 광고 포함 | 아니오 |
| 로그인 필요 | 아니오 |
| 리뷰어 계정 | 불필요 |
| 타겟 연령 | 전체 이용자, 아동을 주된 대상으로 하지 않음 |

상세 근거는 [`play-console-data-safety.md`](./play-console-data-safety.md)를 사용합니다.

## 출시 직전 확인

- [ ] Play Console에 올릴 AAB가 실제 `main` 기준인가
- [ ] 개인정보처리방침 URL이 HTTPS로 공개되고 로그인 없이 열리는가
- [ ] 공개 페이지에 개인정보처리자 한수진, 팀명 Mapmory, 문의 이메일이 표시되는가
- [ ] Ktor 원격 저장소와 `X-Member-Id` 전송이 앱 실행 경로에 연결되지 않았는가
- [ ] 카메라·사진·위치 권한을 실제로 요청하지 않는가
- [ ] Firebase, Mapbox, 광고·분석·추적 SDK가 없는가
- [ ] 새 데이터 전송 기능이 생겼다면 데이터 보안 응답과 개인정보처리방침을 갱신했는가

## 기능 추가 시 갱신 대상

서버 연동, 로그인, 사진 첨부, GPS 위치, 오류 로그 수집 또는 외부 SDK를 실제 출시 빌드에 연결하면 제출 전에 수집 항목, 처리 목적, 보관·삭제 정책과 제3자 처리 여부를 다시 검토합니다.

## 근거

- [Google Play 데이터 보안 섹션 공식 안내](https://support.google.com/googleplay/android-developer/answer/10787469?hl=ko)
- [Mapmory 개인정보처리방침](../../docs/privacy-policy.html)
