# Mapmory landing — interactive globe and district-detail QA

> Screenshot evidence is retained locally under `design-qa/` and is intentionally
> excluded from Git. This document records the capture paths and verified results
> without adding binary QA artifacts to the repository history.

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

# Centered hero and scroll-built memory collage QA — 2026-08-27

## Evidence

- Source visual truth: `design-qa/hero-centered-scroll/reference-selected-option-3.png` (1487 × 1058 px), the user's selected third hero direction.
- Fresh desktop state: `design-qa/hero-centered-scroll/desktop-initial-1440x1024.png` (1425 × 1013 px browser capture) with only the centered 제주 record visible.
- Desktop reveal states: `desktop-step-1-1440x1024.png`, `desktop-step-2-1440x1024.png`, and `desktop-all-photos-top-1440x1024.png` (1425 × 1013 px each).
- Dark theme: `desktop-dark-1440x1024.png` (1425 × 1013 px).
- Mobile states: `mobile-initial-390x844.png` and `mobile-all-390x844.png` (375 × 811 px captures).
- Combined comparison: `design-qa/hero-centered-scroll/comparison-reference-vs-implementation.png` (2880 × 1084 px) places the selected reference and the final all-photo implementation together.
- CSS viewport and density: desktop 1440 × 1024, mobile 390 × 844, device scale 1. The in-app browser reserves 15 px horizontally and 11/33 px vertically for its scroll surface, so the resulting content captures are 1425 × 1013 and 375 × 811 px. The combined image fits each artifact proportionally into an equal 1440 × 1024 comparison panel without cropping.
- State alignment: the reference shows the completed three-photo collage, so it is compared with the implementation after both scroll reveals have completed and the user has returned to the hero top.

## States and interactions checked

- A fresh visit begins with one centered 제주 photo. Natural scrolling reveals 합정 first and 여수 second; scrolling is never locked or hijacked.
- Once a photo has been revealed, it remains in the collage when the user scrolls back up, matching the metaphor of accumulated records.
- `기억` retains the readable mint display word and receives a restrained handwritten overlay above it.
- Each photo caption writes on with an experience-led factual title: `파도 소리가 남은 저녁`, `기다림 끝의 따뜻한 한 그릇`, and `함께 나눈 달콤한 한 상자`.
- `지구본 돌려보기` navigates to `#experience`; the section settles 100 px below the floating header.
- 중국 selection marks the country selected and opens the separate `상하이 · 와이탄` memory panel.
- The existing globe drag interaction was preserved unchanged from the preceding passed QA run.
- In the Korea demo, adding the 서울 photo changes progress from 0/17 to 1/17 and 6%, adds the 서울 hotspot, and exposes `기억 보기`. Selecting it opens the 서울 시·군·구 map and 합정·희옥 memory card.
- No horizontal overflow was found at either target viewport (`scrollWidth === clientWidth`).
- Browser console: no errors, warnings, or React overlay; only Vite connection/HMR messages and the React development-tools notice.

## Required fidelity surfaces

- Fonts and typography: the centered single-axis headline follows the selected reference. LINE Seed Sans KR remains the readable base; Nanum Pen Script is limited to the `기억` annotation and photo records so the page does not become decorative or noisy.
- Spacing and layout rhythm: header, promise, actions, and photo cluster share one centered axis with generous whitespace. The completed collage keeps the source's dominant center photo and quieter angled side photos while the initial state remains intentionally lean.
- Colors and visual tokens: the restrained light canvas, dark ink, mint accent, subtle border, paper white, and tape treatment align with the reference and remain legible in dark mode.
- Image quality and asset fidelity: all three visible records use Mapmory team-owned photography at stable crops. No generated image, placeholder, CSS-drawn asset, upload, or database interaction was introduced.
- Copy and content: the core Mapmory promise and real launch-notification state remain unchanged. The handwritten titles describe only experiences visible in the supplied photographs and do not imply unsupported functionality.
- Focused comparison: a separate crop was unnecessary because the combined 2880 × 1084 image keeps the headline annotation, both CTA treatments, all three photo crops, handwritten captions, release note, and fold cue readable at original density.

## Comparison history

1. First browser pass — blocked.
   - [P2] The handwritten `기억` duplicate sat directly on top of the base word and weakened headline legibility.
   - [P2] At 390 × 844, side-photo titles were truncated and mostly hidden behind the center card after reveal.
2. Fixes applied.
   - Reduced and raised the handwritten `기억` layer so it reads as an annotation while the base word stays intact.
   - Allowed mobile side captions to wrap and brought revealed side cards above the center card, making the newly added records readable while preserving the taped collage.
   - Made the reveal state cumulative so records do not disappear when the user scrolls back upward.
3. Final full-view comparison — no actionable P0/P1/P2 differences remain.
   - The implementation matches the selected option's centered hierarchy, whitespace, rounded floating header, compact CTA row, and dominant three-photo composition.
   - Emotional handwritten record titles and the one-by-one reveal are intentional user-requested extensions to the static source.

## Follow-up polish

- P3: the browser-owned scrollbar is visible in captures but is not part of the page design and does not affect layout width or interaction.
- The existing `react-globe.gl` large-chunk warning remains a performance follow-up; the globe loads and interacts successfully.

## Final result

final result: passed

---

# Handwritten memory first-paint timing QA — 2026-08-27

## Evidence

- Source visual truth: `design-qa/hero-centered-scroll/reference-selected-option-3.png` (1487 × 1058 px).
- Fresh desktop state: `design-qa/hero-centered-scroll/memory-first/desktop-initial-1440x1024.png` (1425 × 1013 px browser capture).
- Completed desktop photo state: `desktop-all-1440x1024.png` (1425 × 1013 px).
- Fresh mobile state: `mobile-initial-390x844.png` (375 × 811 px).
- Combined comparison: `comparison-reference-vs-memory-first.png` (2880 × 1084 px).
- CSS viewport and density: desktop 1440 × 1024, mobile 390 × 844, device scale 1. The comparison fits the reference and implementation proportionally into equal 1440 × 1024 panels without cropping.

## Timing and interaction checks

- `기억` is handwritten from the first rendered frame; it is no longer delayed by a scroll threshold or by the handwriting-reveal animation.
- The first 제주 photo is eagerly requested with high fetch priority and is also preloaded from the document head.
- On a fresh navigation, the handwritten title had opacity 1 while the 제주 image was complete with a non-zero natural width and the photo surface had opacity 1.
- Scrolling changes only the photo cluster: 합정 then 여수 are added while the handwritten title remains stable.
- Desktop and mobile had no horizontal overflow (`scrollWidth === clientWidth`).
- Browser console errors/warnings and React overlay: none.

## Required fidelity surfaces

- Fonts and typography: the headline keeps the selected centered scale and spacing. Only `기억` uses Nanum Pen Script; the accessible base word remains in the heading while the duplicate handwritten layer is presentation-only.
- Spacing and layout rhythm: hiding the printed visual word does not collapse its reserved headline width, so surrounding copy does not jump when the font loads.
- Colors and visual tokens: the handwritten word uses the existing mint accent and preserves light/dark theme contrast.
- Image quality and asset fidelity: the existing team-owned 제주 photograph is unchanged; preload and fetch-priority hints affect timing only.
- Copy and content: no landing copy or product capability changed.
- Focused comparison: the full combined image keeps the headline word, CTAs, photo captions, and complete collage readable, so no additional crop was needed.

## Comparison history

1. Initial timing review — blocked.
   - [P2] The headline handwriting and photo-caption animations used separate delays, which could make a slower device appear to finish `기억` before the photo experience arrived.
2. Fixes applied.
   - Removed the first-load animation dependency from the headline and rendered the handwritten `기억` immediately.
   - Added document preload plus eager/high-priority loading to the first 제주 photograph.
   - Kept the scroll interaction focused only on adding the two secondary photos.
3. Final desktop/mobile review — no actionable P0/P1/P2 issues remain. The headline and representative photo are present together on entry, and the later scroll still adds records progressively.

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

---

# Floating header, photo-cluster hero, and add-to-map QA — 2026-08-27

## Evidence

- Source visual truth: `design-qa/polarsteps-layout/reference-polarsteps.png` (1140 × 670 px), supplied by the user as the structural reference for the floating header and center-emphasized product visual.
- Same-state implementation: `design-qa/polarsteps-layout/implementation-1140x670.png` (1125 × 662 px browser content capture), rendered with a 1140 × 670 CSS viewport override and device scale 1. The in-app browser reserves 15 px horizontally and 8 px vertically for its scroll surface.
- Combined comparison: `design-qa/polarsteps-layout/comparison-reference-vs-implementation.png` places the source and implementation side by side at identical 1140 × 670 content dimensions.
- Desktop implementation: `design-qa/polarsteps-layout/desktop-1440x1024-final.png` and `desktop-dark-1440x1024.png` (1425 × 1013 px captures), rendered with a 1440 × 1024 CSS viewport override.
- Mobile implementation: `design-qa/polarsteps-layout/mobile-390x844-final.png` (375 × 811 px capture), rendered with a 390 × 844 CSS viewport override.
- Korea empty, added, and browse states: `korea-empty-1440x1024.png`, `korea-added-1440x1024.png`, `korea-detail-1440x1024.png`, and `korea-mobile-empty-390x844.png`.
- Density normalization: the source is 1140 × 670 px and the implementation content capture is 1125 × 662 px at device scale 1. For the combined comparison only, the implementation was normalized by 1.013× horizontally and 1.012× vertically to the source's 1140 × 670 frame; this compensates only for the in-app browser's reserved scroll surface. The desktop and mobile captures are responsive-state evidence rather than pixel-fidelity inputs.
- Focused comparison: a separate crop was not needed because the equal-size combined image keeps the complete floating header, hero typography, primary visual, and CTAs readable at original density.

## States and interactions checked

- Floating rounded header in desktop/mobile and light/dark themes.
- Three taped team photographs in the first screen, with the main 제주 image centered and 합정·여수 images overlapping as secondary memories.
- Mobile headline, CTAs, captions, release note, photo cluster, and the `아래로 내려 앱 경험해보기` cue without clipping or overlap.
- Korea demo initial state: 0 / 17, no colored province, three team-owned example photo cards.
- `서울특별시 사진을 지도에 추가하기`: progress changes to 1 / 17 and 6%, 서울 is colored, and both a map hotspot and `기억 보기` control appear.
- `서울특별시 기억 보기`: opens the existing 서울 시·군·구 map and 합정·희옥 memory card.
- Browser reload: demo returns to 0 / 17, confirming that the add state is browser memory only. No upload, persistence API, or database write is used.
- Dark-theme toggle: floating header, photos, copy, and CTA contrast remain intact.
- Console: no error or warning entries; only Vite connection messages and the React development-tools notice. No React error overlay appeared.

## Required fidelity surfaces

- Fonts and typography: the existing LINE Seed Sans KR display hierarchy remains clear and editorial. At mobile width the headline was reduced to 34–38 px so all three photo memories and the next-experience cue remain within the first screen.
- Spacing and layout rhythm: the reference's detached rounded header and side-copy/center-visual composition are reflected without copying its phone mockup. The hero visual is allowed to expand beyond the ordinary text column, removing the earlier boxed-in feeling.
- Colors and visual tokens: Mapmory keeps its light canvas and mint accent instead of copying Polarsteps' dark navy/red palette. The header has a visible border, raised surface, blur, and shadow in both Mapmory themes.
- Image quality and asset fidelity: all three hero images and all Korea add-flow images are Mapmory team-owned photographs. No generated image, placeholder, fake upload surface, or CSS-drawn visual asset was introduced.
- Copy and content: the original Mapmory promise and launch-notification CTA remain. The Korea copy now describes the real product loop precisely: add a place-bearing photo, color its province, then reopen the saved memory.

## Comparison history

1. First browser comparison — blocked.
   - [P2] At 390 × 844, the enlarged three-photo cluster pushed the next-experience cue below the first viewport.
   - [P2] The horizontal example-photo tray exposed a thick native scrollbar that competed with the photo cards.
2. Fixes applied.
   - Tightened mobile headline/action spacing and reduced the photo cluster from 330 px to 270 px while preserving all three taped photographs.
   - Hid the native horizontal scrollbar while retaining touch scrolling and scroll snapping.
3. Final comparison — no actionable P0/P1/P2 differences remain.
   - `mobile-390x844-final.png` shows all primary copy, both CTAs, three photographs, and the next-experience cue without overlap.
   - The 1140 × 670 combined image shows the intended shared structure: a floating centered header, concise side copy, and a dominant center/right product visual. Palette, assets, and CTA differences are intentional Mapmory product constraints.
   - `korea-added-1440x1024.png` and `korea-detail-1440x1024.png` verify the complete add → color → browse loop.

## Follow-up polish

- P3: the three-photo tray intentionally shows the next card partially at mobile width to signal horizontal swiping; a short swipe hint could be user-tested later if discovery is weak.
- The existing large `react-globe.gl` production chunk warning remains a performance follow-up and does not block this visual or interaction QA.

## Final result

final result: passed

---

# Hero scroll performance QA — 2026-08-27

## 발견 원인

- 사진 공개 단계 상태가 최상위 `App`에 있어 첫 스크롤 때 3D 지구본과 이후 섹션까지 함께 다시 렌더링될 수 있었음.
- 숨겨진 대형 사진의 `filter: blur()` 전환이 모바일에서 추가 래스터라이징·합성 비용을 만들 수 있었음.
- 화면 아래의 WebGL 지구본 렌더링 루프가 첫 화면에서도 계속 실행되고 있었음.

## 수정 및 검증

- 사진 공개 상태와 스크롤 리스너를 독립 `HeroSection`으로 이동해 지구본과 대한민국 지도가 다시 렌더링되지 않도록 격리함.
- 사진 모션을 GPU 합성에 적합한 `opacity`와 `transform`만 사용하도록 변경하고, 보조 사진은 비동기 디코딩함.
- 지구본이 화면 밖에 있을 때 `pauseAnimation()`, 체험 구간 120px 안으로 들어오면 `resumeAnimation()`을 호출함.
- 390 × 844 실제 스크롤 입력에서 사진 단계가 `0 → 1 → 2`로 전환되고 가로 오버플로가 없음을 확인함.
- 지구본 구간으로 이동한 뒤 중국 선택과 `상하이 · 와이탄` 별도 기억 패널 전환이 정상 작동함.
- 브라우저 콘솔 오류/경고와 React 오류 오버레이: 없음.
- `npm run build`: passed. 기존 WebGL 대형 청크 경고만 유지됨.
- `npm test`: 10/10 passed.

## Final result

final result: passed
