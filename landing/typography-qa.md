# Mapmory 타이포그래피 개선 QA

## 결론

**Passed — 로컬 개선안 기준. 운영에는 아직 반영하지 않음.**

모바일에서 메인 제목의 시각적 압박을 낮추고, 한국어 단어가 중간에서 끊기던 앱 흐름·출시 알림 제목을 자연스럽게 정리했다. 메시지 위계는 유지하면서 사진, CTA, 다음 콘텐츠가 더 빨리 보이도록 했다.

스크린샷 증거는 `design-qa/typography/`에 로컬 보관한다. 저장소에는 바이너리
이미지를 올리지 않고 이 문서에 캡처 경로와 검수 결과만 유지한다.

## 검수 범위

- 운영 이전 버전: `https://map-mory.com/`
- 로컬 개선안: `http://127.0.0.1:4173/`
- 모바일 뷰포트: 390 × 844
- 데스크톱 뷰포트: 1440 × 1024
- 화면 상태: 메인, 앱 흐름, 출시 알림
- 테마: 기본 라이트 테마

## 변경값

| 항목 | 이전 | 개선안 | 판단 |
| --- | --- | --- | --- |
| 모바일 메인 제목 | Noto Sans KR, 45.3px, 173.9px 높이 | LINE Seed Sans KR, 39.0px, 140.5px 높이 | 제목 높이 약 19% 감소 |
| 데스크톱 메인 제목 | 72px, 267.8px 높이 | 64px, 226.5px 높이 | 제목 높이 약 15% 감소 |
| 제목 자간 | -0.06em | -0.035em | 과도한 밀착 완화 |
| 앱 흐름 제목 | `자연/스럽게` 단어 분리 | `자연스럽게` 단위 유지 | 읽기 흐름 개선 |
| 출시 알림 제목 | 40px, 3줄 | 35px, 2줄 | 폼보다 제목이 과도하게 커 보이던 문제 완화 |

## 시각적 판단

1. **메인 메시지 위계 — Healthy**
   - 핵심 문구는 여전히 3줄이며 `기억` 강조색도 유지된다.
   - 모바일 첫 화면에서 사진과 CTA가 더 일찍 보여 랜딩의 체험 흐름이 명료해졌다.

2. **한국어 줄바꿈 — Healthy**
   - 제목에 `word-break: keep-all`을 적용해 의미 단위가 단어 중간에서 끊기지 않는다.
   - 앱 흐름과 출시 알림 제목이 각각 안정적인 2줄로 정리됐다.

3. **폰트 개성 — Healthy**
   - 본문은 가독성이 익숙한 Noto Sans KR을 유지했다.
   - 큰 제목에만 LINE Seed Sans KR Bold를 적용해 과한 장식 없이 조금 더 둥글고 친근한 인상을 만들었다.

4. **반응형 안전성 — Healthy**
   - 모바일 390px에서 가로 오버플로가 없었다.
   - 데스크톱 1440px에서 사진, CTA, 다음 섹션의 균형이 유지됐다.

5. **브라우저 오류 — Healthy**
   - 로컬 데스크톱 캡처 상태에서 콘솔 오류가 없었다.
   - React 오류 오버레이가 나타나지 않았다.

## 구현 파일

- `src/styles.css`
- `public/assets/fonts/line-seed-kr-bold.woff2`
- `public/assets/fonts/README.md`

폰트는 LINE Seed 공식 배포본을 사용하며 SIL Open Font License 1.1을 따른다. 출처: <https://seed.line.me/>

## 캡처 목록

- 모바일 메인 이전: `design-qa/typography/01-before-mobile-hero.png`
- 모바일 메인 개선: `design-qa/typography/02-after-mobile-hero.png`
- 모바일 앱 흐름 이전/개선: `05-before-mobile-journey.png`, `03-after-mobile-journey.png`
- 모바일 출시 알림 이전/개선: `06-before-mobile-download.png`, `04-after-mobile-download.png`
- 데스크톱 메인 이전/개선: `07-before-desktop-hero.png`, `08-after-desktop-hero.png`

## 남은 판단

- 새 제목 폰트 파일은 약 463KB다. 첫 방문 성능은 실제 배포 후 GA4 및 Lighthouse로 확인해야 한다.
- 이번 결과는 시각적 선호를 검증하는 로컬 개선안이다. 사용자 확인 후 커밋·배포 여부를 결정한다.

후속 모바일 여백·정렬 개선은 `mobile-layout-qa.md`에서 확인할 수 있다.
