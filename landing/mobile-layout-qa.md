# Mapmory 반응형 중심 컬럼 개선 QA

## 결론

**Passed — 로컬 개선안 기준. 운영에는 아직 반영하지 않음.**

모바일에서는 상단바, 메인 문구, 사진, 일반 섹션, 출시 알림 폼을 좌우 24px의 같은 중심 컬럼에 맞췄다. 데스크톱에서는 상단바와 일반 콘텐츠를 1080px 중심 컬럼으로 통일했다. 지구본과 대한민국 상세지도는 조작 공간을 보존하기 위해 모바일 8px 여백, 데스크톱 1180px 폭으로 확장했다.

스크린샷 증거는 `design-qa/mobile-centered/`와 `design-qa/desktop-centered/`에
로컬 보관한다. 저장소에는 바이너리 이미지를 올리지 않고 이 문서에 캡처 경로와
검수 결과만 유지한다.

## 데스크톱 후속 개선

사용자 확인에서 모바일만 중앙화되고 데스크톱은 기존 상태라는 문제가 발견됐다. 같은 구조 원칙을 데스크톱에도 적용했다.

| 항목 | 이전 | 개선 |
| --- | ---: | ---: |
| 상단바·footer 폭 | 1376.8px | 1080px |
| hero·일반 섹션·폼 폭 | 1180px | 1080px |
| 지구본·상세지도 폭 | 1180px | 1180px 유지 |
| 일반 콘텐츠 좌우 위치 | 122.4px | 172.4px |
| 상단바 좌우 위치 | 24px | 172.4px |

- 상단 로고, 내비게이션, 테마 전환, CTA가 hero와 같은 중심축에 들어왔다.
- hero, 앱 흐름, 출시 알림, footer의 좌우 기준선이 반복된다.
- 지구본과 상세지도는 기존 폭을 유지해 체험 밀도를 줄이지 않았다.

데스크톱 캡처:

- `design-qa/desktop-centered/01-before-desktop.png`
- `design-qa/desktop-centered/02-after-desktop.png`
- `design-qa/desktop-centered/03-after-desktop-experience.png`
- `design-qa/desktop-centered/04-after-desktop-detail.png`

## 사용자 피드백과 반영 결과

| 피드백 | 이전 상태 | 반영 결과 |
| --- | --- | --- |
| 첫 화면이 AI로 만든 랜딩처럼 보임 | 텍스트와 CTA만 크게 보이고 실제 제품 경험이나 팀 사진이 첫 화면에 없음 | 실제 제주 사진을 사용한 에디토리얼 첫 화면과 다음 체험 섹션 노출 |
| 지구본이 움직이는지 알기 어려움 | 첫 화면의 제품 체험 연결이 약함 | `지구본 돌려보기` CTA와 명시적인 조작 안내 유지 |
| 제목이 크고 개성이 약함 | 모바일 45.3px Noto Sans KR | 39px LINE Seed Sans KR, 제목 높이 약 19% 감소 |
| 모바일 상단바와 콘텐츠가 옆으로 퍼져 보임 | 상단바와 일반 콘텐츠 좌우 15px | 동일한 좌우 24px 중심 컬럼으로 정렬 |
| 화면을 좁히면 제품 체험이 답답해질 수 있음 | 모든 섹션이 같은 15px 여백 | 일반 콘텐츠는 24px, 지구본·상세지도만 8px 여백으로 분리 |

## 버전별 근거

1. **초기 랜딩 — Needs improvement**
   - 기준 커밋: `8a9f952` (`13ea1ee` 리팩터링 직전)
   - 실제 사진과 지구본 경험이 첫 화면에 드러나지 않았다.
   - 캡처: `design-qa/mobile-centered/01-pre-refactor-mobile.png`

2. **현재 운영 버전 — Healthy, but visually dense**
   - 실제 팀 사진, 명확한 체험 CTA, 다음 섹션 노출로 자연스러움과 제품 이해가 개선됐다.
   - 모바일 제목 크기와 15px 여백 때문에 상단 요소가 화면 전체로 퍼져 보였다.
   - 캡처: `design-qa/mobile-centered/02-current-production-mobile.png`

3. **타이포그래피 개선안 — Healthy**
   - 제목 크기·폰트·자간·한국어 줄바꿈이 개선됐다.
   - 상단바와 일반 콘텐츠 폭은 여전히 345px였다.
   - 캡처: `design-qa/mobile-centered/03-typography-only-mobile.png`

4. **모바일 중심 컬럼 개선안 — Healthy**
   - 상단바와 일반 콘텐츠 폭을 327px로 통일했다.
   - 지구본과 상세지도는 359px로 넓혀 체험 공간을 유지했다.
   - 캡처: `design-qa/mobile-centered/04-centered-mobile-hero.png`

## 레퍼런스에서 가져온 원칙

- Polarsteps: 여행 영상·사진은 화면을 크게 쓰고, 메시지와 CTA는 시각적으로 중앙에 모은다.
- Toss: 상단바와 텍스트, 주요 콘텐츠의 좌우 기준선을 반복해 모바일 읽기 흐름을 안정시킨다.
- Mapmory 적용: 레퍼런스의 외형을 복제하지 않고 `일반 정보는 중심 컬럼`, `제품 체험은 넓은 표면`이라는 구조 원칙만 반영했다.

검수 캡처:

- `design-qa/mobile-centered/reference-polarsteps-mobile.png`
- `design-qa/mobile-centered/reference-toss-mobile.png`

## 검수한 화면

- 모바일 메인: 390 × 844
- 최소 폭 재배치: 320 × 700
- 데스크톱 메인과 체험 흐름: 1440 × 1024
- 3D 지구본과 별도 기억 패널
- 대한민국 상세지도
- 앱 흐름
- 출시 알림 폼
- 라이트·다크 테마

추가 캡처:

- `design-qa/mobile-centered/06-centered-mobile-320.png`
- `design-qa/mobile-centered/07-centered-experience.png`
- `design-qa/mobile-centered/08-centered-detail.png`
- `design-qa/mobile-centered/09-centered-journey.png`
- `design-qa/mobile-centered/10-centered-download.png`

## 주요 측정값

| 항목 | 이전 | 개선 |
| --- | ---: | ---: |
| 일반 콘텐츠 좌우 여백 | 15px | 24px |
| 일반 콘텐츠 폭 | 345.2px | 327.2px |
| 지구본·상세지도 좌우 여백 | 15px | 8px |
| 지구본·상세지도 폭 | 345.2px | 359.2px |
| 모바일 메인 제목 | 45.3px | 39.0px |

## 검증 결과

- 390px와 320px에서 가로 오버플로 없음
- 1440px에서 상단바와 일반 콘텐츠의 1080px 중심축 일치
- 1440px에서 지구본과 상세지도 1180px 폭 유지
- 상단바, 메인, 일반 섹션, 출시 알림 폼의 좌우 기준선 일치
- 지구본과 상세지도 조작 영역은 이전보다 넓어짐
- 다크모드 전환 후 가로 오버플로 없음
- 브라우저 콘솔 오류와 React 오류 오버레이 없음

## 남은 확인

- 실제 모바일 기기에서 24px 여백에 대한 선호도를 최종 확인한다.
- 새 제목 폰트 약 463KB의 첫 방문 성능은 배포 후 Lighthouse와 실제 사용자 지표로 확인한다.
