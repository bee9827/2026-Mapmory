# Mapmory landing — interactive globe and district-detail QA

## Evidence

- Source visual truth: `qa/reference-jeonnam-district-map.png` (456 × 363 px), supplied by the user as the expected province-detail map state.
- Desktop implementation: `qa/desktop-jeonnam-detail-final.png` (998 × 1000 px browser capture), rendered with a 1440 × 1000 CSS viewport override in the Codex in-app browser.
- Mobile implementation: `qa/mobile-jeonnam-detail-final.png` and `qa/mobile-globe-country-dock.png` (375 × 811 px browser captures), rendered with a 390 × 844 CSS viewport override.
- Focused comparison: the source and the rendered Jeonnam map were opened together at original density. The comparison was limited to the map surface because the source does not include the surrounding landing-page card or photo.
- State: light landing theme with the intentionally dark product-map surface; Jeonnam selected at level 3 and Yeosu highlighted.
- Density normalization: captures were compared by content region and relative map proportions, not raw pixel equality, because the in-app browser surface crops its screenshot output below the explicit CSS viewport width.

## Interaction and technical checks

- Country shortcuts remain visible inside the 3D globe panel at desktop and mobile widths.
- Korea, China, Japan, and Nepal shortcuts update the adjacent card to the correct developer-owned photo with non-zero natural dimensions.
- The Korea flow changes within one card: 17-province map → province city/county/district map → back to 17-province map.
- Seoul, Jeonnam, and Jeju load the exact client district JSON; Mapo-gu, Yeosu, and Jeju City are highlighted respectively.
- The back button returns to the province map without route navigation.
- The mobile checks showed no horizontal overflow (`scrollWidth` did not exceed the layout viewport).
- Browser console errors and warnings: none.
- Frontend tests: 10 passed.
- Production build: passed.

## Required fidelity surfaces

- Fonts and typography: existing Noto Sans KR / Be Vietnam Pro hierarchy is preserved. District labels use compact optical sizes at mobile width so the dense Jeonnam map remains readable.
- Spacing and layout rhythm: the globe selector is contained by the 3D surface. The level-3 map no longer stretches to the photo-card height, eliminating the large empty lower area found in the first comparison.
- Colors and visual tokens: the district surface now uses the source's dark slate fills, muted blue-gray boundaries and labels, and a single mint selected district.
- Image quality and asset fidelity: the existing developer-owned memories and the five new USA memories use the original photographs; no generated or placeholder imagery remains in the active flows.
- Copy and content: Huiok copy preserves the user's notes about umami, acidity, and the long wait in a shorter landing-page voice. Level labels clearly distinguish province selection from district detail.

## Comparison history

1. First focused comparison — blocked.
   - [P2] The level-3 map stretched to the height of the adjacent photo card, leaving a large empty dark area below the map.
   - [P2] Unselected Jeonnam districts were light gray, while the source uses a dark slate map with quiet boundaries.
2. Fixes applied.
   - Changed the detail grid to top alignment so the map keeps its natural canvas height.
   - Changed unselected district fill, stroke, and label colors to the source's dark visual system; kept Yeosu mint.
   - Reduced district label size and outer padding at the mobile breakpoint.
3. Final focused comparison — no actionable P0/P1/P2 differences remain. The rendered map preserves the source hierarchy, region silhouette, label treatment, and active-district emphasis while adding the landing-specific step caption and adjacent memory card.

## Residual test gaps

- The globe bundle still produces Vite's existing large-chunk warning; loading and interaction completed successfully, so this is a performance follow-up rather than a visual blocker.
- The source screenshot covers only the Jeonnam detail state, so other landing sections were checked for consistency and responsiveness rather than pixel fidelity to that source.

## USA west gallery extension

### Evidence

- Source visual truth: `C:/Users/YongSung/Downloads/20251.jpg`, `20709.jpg`, `19812.jpg`, `19937.jpg`, and `19939.jpg`, supplied by the user as five photographs from the same USA trip.
- Desktop implementation: `C:/Users/YongSung/.codex/visualizations/2026/08/23/01a02dce-4d75-7521-a0b5-3c8f63753f9d/desktop-usa-gallery-final.png`, browser capture at the default desktop viewport with the USA selected and the first gallery image active.
- Mobile implementation: `C:/Users/YongSung/.codex/visualizations/2026/08/23/01a02dce-4d75-7521-a0b5-3c8f63753f9d/mobile-usa-gallery-card-final.png`, browser capture with a 390 x 844 CSS viewport override and the USA photo card aligned to the viewport.
- Combined comparison evidence: `C:/Users/YongSung/.codex/visualizations/2026/08/23/01a02dce-4d75-7521-a0b5-3c8f63753f9d/usa-gallery-comparison.png` places all five source photos beside the rendered desktop and mobile states in one image.
- Density normalization: source photographs retain their original 4000 x 3000, 2252 x 4000, 4000 x 3000, 3000 x 4000, and 4000 x 2252 pixels. The browser captures are evaluated by the rendered content box at device scale 1 rather than raw pixel equality because the source is photography, not a UI mock.
- Focused comparison: the photo area, gallery controls, caption treatment, and memory-card copy were readable in the combined evidence, so no additional crop was required.

### Interaction and technical checks

- Selecting `미국` highlights the USA on the globe and opens the `미국 · 서부 여행` memory card.
- All five dot controls and both arrows change the active photograph; each image loaded with the source's non-zero natural dimensions and the count changed from `1 / 5` through `5 / 5`.
- Landscape and portrait photographs use `object-fit: contain`, preserving the full source frame without stretching the memory card.
- Mobile layout showed no horizontal overflow (`scrollWidth` equaled the layout viewport width), and the country selector, arrows, counter, caption, dots, title, description, and photo credit remained usable.
- Browser console errors and warnings: none.

### Required fidelity surfaces

- Fonts and typography: the existing Mapmory hierarchy is preserved; gallery captions and the count remain legible over the photographs at desktop and mobile widths.
- Spacing and layout rhythm: the gallery keeps the existing memory-card proportions. Portrait images no longer expand the card height, while controls remain inside the image surface.
- Colors and visual tokens: dark translucent gallery controls reuse the product-map surface and mint active token without competing with the photographs.
- Image quality and asset fidelity: all five supplied files are present at their original dimensions and shown uncropped with `contain`; no AI-generated or replacement imagery is used.
- Copy and content: the five captions distinguish Bryce Canyon, Antelope Canyon, Las Vegas daytime, the fountain, and the Venetian night, while the card groups them as one USA west trip.

### Comparison history

1. First browser comparison - blocked.
   - [P2] Portrait photographs expanded the gallery image element and made the memory card substantially taller than the adjacent globe panel.
2. Fix applied.
   - Positioned gallery images inside the fixed image surface and set explicit width, height, and `object-fit: contain` so portrait and landscape sources share one stable card geometry.
3. Final combined comparison - no actionable P0/P1/P2 differences remain. Source composition is preserved, the five-photo sequence is obvious, and desktop/mobile hierarchy remains intact.

## Final result

final result: passed

---

# Natural editorial hero refactor QA — 2026-08-26

## 기준 및 산출물

- 시각적 기준 이미지: `C:\Users\YongSung\.codex\generated_images\01a03c3d-4d65-7953-a731-1efc7fc41422\exec-d7d4ad24-2ba2-45b8-8218-ca002f947ac4.png`
- 기준 목업·구현 비교 이미지: `design-qa/comparison-reference-vs-implementation.png`
- 데스크톱 구현: `design-qa/desktop-1440x1024.png`
- 데스크톱 지구본 체험: `design-qa/desktop-experience-1440x1024.png`
- 데스크톱 다크모드: `design-qa/desktop-dark-1440x1024.png`
- 대한민국 2단계 지도: `design-qa/desktop-korea-level2.png`
- 대한민국 3단계 제주 지도: `design-qa/desktop-korea-level3-jeju.png`
- 모바일 구현: `design-qa/mobile-390x844.png`
- 모바일 지구본 체험: `design-qa/mobile-experience-390x844.png`

## 검수 환경 및 화면 상태

- 로컬 Vite 개발 서버: `http://127.0.0.1:4173/`
- 뷰포트: 데스크톱 1440 × 1024, 모바일 390 × 844
- 기본 라이트 테마와 다크 테마
- 에디토리얼 첫 화면, 스크롤 유도, `01 · 세계` 섹션, 3D 지구본과 국가별 기억 패널
- 대한민국 17개 시·도 지도, 제주특별자치도 시·군·구 지도와 장소 기억 패널

## 주요 인터랙션 결과

- `지구본 돌려보기` CTA가 `#experience`로 이동하고 고정 헤더 아래에 제목과 조작 안내를 노출함: Passed
- 실제 지구본 캔버스를 드래그했을 때 시점이 회전함: Passed
- 중국 선택 시 `상하이 · 와이탄`, 일본 선택 시 `도쿄` 기록이 별도 기억 패널에서 열림: Passed
- 대한민국 상세지도 링크가 `#korea-detail`로 이동함: Passed
- 17개 시·도 → 제주 시·군·구 → 제주 기억 패널 → 대한민국 지도로 돌아가기 흐름: Passed
- 라이트/다크 테마 전환 후 텍스트, 사진, CTA, 다음 섹션이 깨지지 않음: Passed
- 모바일에서 텍스트, 사진, CTA, 지구본과 조작 안내가 잘리거나 겹치지 않으며 가로 오버플로가 없음: Passed

## 시각 비교

- 선택 목업의 왼쪽 정렬 메시지, 절제된 여행 사진, 민트색 단일 강조색, 첫 화면 아래로 다음 경험이 이어지는 편집 구조를 유지함.
- 기준 목업의 서울 골목 사진 대신 팀 소유 제주 사진을 사용해 실제 자산 원칙을 지킴.
- 목업의 장식용 대형 지구본을 첫 화면에 복제하지 않고 바로 다음 섹션에서 실제 조작 가능한 지구본과 별도 기억 패널을 노출해 제품 흐름을 정확히 유지함.
- 데스크톱에서는 첫 화면 아래에 `01 · 세계` 제목과 지구본 패널 상단이 보이며, 모바일에서는 스크롤 안내가 첫 화면 하단에 노출됨.

## 콘솔 및 오류

- 브라우저 콘솔 오류/경고: 없음
- Vite/React 오류 오버레이: 없음
- 빈 화면, 이미지 로드 실패, 대한민국 상세지도 JSON 로드 실패: 없음

## 발견 및 수정한 문제

- 이번 브라우저 검수에서 추가 P0/P1/P2 문제는 발견되지 않음.
- 기존 diff의 에디토리얼 2열 구조, 명시적인 지구본 CTA·조작 안내, 다음 섹션 유도, 태블릿·모바일 반응형이 실제 브라우저에서 정상 동작함을 확인함.
- 기준 목업과의 차이는 실제 팀 사진과 실제 제품 흐름을 우선하기 위한 의도된 차이이며, 존재하지 않는 기능이나 편집 흐름을 추가하지 않음.

## 최종 결과

**Passed**
