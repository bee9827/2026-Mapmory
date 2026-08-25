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
- Frontend tests: 7 passed.
- Production build: passed.

## Required fidelity surfaces

- Fonts and typography: existing Noto Sans KR / Be Vietnam Pro hierarchy is preserved. District labels use compact optical sizes at mobile width so the dense Jeonnam map remains readable.
- Spacing and layout rhythm: the globe selector is contained by the 3D surface. The level-3 map no longer stretches to the photo-card height, eliminating the large empty lower area found in the first comparison.
- Colors and visual tokens: the district surface now uses the source's dark slate fills, muted blue-gray boundaries and labels, and a single mint selected district.
- Image quality and asset fidelity: all six requested memories use the original developer photographs; no generated or placeholder imagery remains in the active flows.
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

## Final result

final result: passed
