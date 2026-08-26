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
