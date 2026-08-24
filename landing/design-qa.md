# Mapmory landing page — Korea detail revision QA

## Source truth

- User feedback: replace all AI-looking imagery with real credited photography; show the shipping product's detailed Korea map; remove the inaccurate record editor; connect product trial to conversion.
- Product implementation checked: `TripMapScreen.kt` (`대한민국`/`전세계`, `나의 대한민국 지도`, visited count and fill rate) and the 17-region generated boundary files under `client/shared/.../map/data/`.
- Before evidence: `audit-v3/02-before-experience.png`.
- After evidence: `qa/01-v3-hero.png`, `qa/02-v3-globe.png`, `qa/03-v3-korea-detail.png`, `qa/04-v3-mobile-detail.png`.
- Before/after images were inspected together at the same desktop viewport.

## Blocking findings and fixes

- [P1] The previous editor implied a recording workflow that does not match the current client. Removed it completely and replaced it with the actual Korea-map scope, summary, 17-region artwork, visited fills, and region memories.
- [P1] Product trial stopped after a country photo and did not create a natural path toward download. Added a visible `대한민국 상세지역으로 이어보기` step, region selection, and `내 기억 지도도 만들기` CTA before the final download state.
- [P1] Korea had no detailed region experience. Reused the shipping Kotlin client's exact generated province coordinates and added interactive Daejeon, Hapjeong, and Yeosu memory triggers.
- [P1] AI-generated Korea and Mongolia visuals conflicted with a memory product. Replaced every visible photo with real photography and added visible linked attribution. Removed the three unused AI assets.
- [P2] Mobile could become visually dense. Kept the sequence vertical: heading, map summary, scope, region choices, map, memory photo, conversion CTA. The 390 × 844 check has no horizontal overflow.

## Interaction and technical verification

- Country buttons update the adjacent real image, title, and source.
- Daejeon, Hapjeong, and Yeosu controls update the selected region, province fill, image, title, and attribution.
- Korea scope and world return links render; the region CTA points to the final download section.
- The Korea canvas renders all 17 province records from the client source. Both globe and Korea-map canvases load.
- All visible images completed with non-zero natural dimensions.
- Light default and dark-mode control remain present.
- Browser content check: passed; Vite overlay: absent; console errors/warnings: none.
- Desktop 1440 × 1000 and mobile 390 × 844 visual checks: passed.
- Production build: passed.

## Final result

final result: passed
