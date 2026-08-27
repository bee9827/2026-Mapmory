# Prototype Instructions

Run the local server yourself and open the preview in the browser available to this environment. Do not give the user server-start instructions when you can run it.

Before making substantial visual changes, use the Product Design plugin's `get-context` skill when the visual source is unclear or no longer matches the current goal. When the user gives durable prototype-specific design feedback, preferences, or decisions, record them in `AGENTS.md`.

When implementing from a selected generated mock, treat that image as the source of truth for layout, component anatomy, density, spacing, color, typography, visible content, and hierarchy.

## Mapmory Landing Decisions

- Default to the light theme and provide an explicit dark-theme toggle.
- Keep the detailed dark globe as the focal product surface in both themes.
- The globe must rotate, and only activated/visited regions open travel memories.
- Selecting an activated region swaps the adjacent photo and record content.
- Use the real current product flow: place selection opens a separate memory panel. Do not imply photos are pinned directly onto the globe.
- Primary conversion goal is app download; until the public store URL is available, keep the final CTA clearly marked as launch preparation.
- While the existing private test remains in progress, do not alter its tester setup. Use one inline email field on the landing page for a one-time public-launch notification, with required collection consent and a 14+ confirmation.
- Lead with travel, then let distinctive places such as a bakery, ramen shop, or dessert shop show how a personal map can trigger memory.
- Keep the first viewport focused on the value proposition and conversion CTA; do not make the headline, globe, and photo compete at once.
- Make the memory/location selector's purpose explicit before the user starts interacting.
- Keep the country selector inside the bounded 3D globe surface so it remains visible while the user rotates the globe; use country names as the primary shortcuts.
- Give the 3D globe a visible bounded panel and preserve vertical touch scrolling with `touch-action: pan-y`.
- Use genuine photography only. Every external photo must have a visible photographer/platform credit and source link.
- Prefer Mapmory team-owned photography for every visible memory. Credit it as `Mapmory 개발팀 촬영`; the current world set is Korea/Hapjeong, China/Shanghai, Japan/Tokyo, and Nepal/volunteer work.
- Keep the globe and photo preview concise. The next product-proof step must use the real app's 대한민국/전세계 scope pattern and the same 17-region boundary data as the Kotlin client.
- The Korea experience is hierarchical in one surface: 17-province map (level 2) → the selected province's real city/county/district boundary map (level 3) → back to the province map. Reuse the client district JSON instead of drawing approximate boundaries.
- Do not show a fake editor, collection form, or recording workflow that differs from the shipping client.
- Preserve the conversion path `3D 지구본 → 대한민국 상세지도 → 지역의 실제 장소 기억 → 내 기억 지도 만들기 → 다운로드`.
- Preserve basic landing-page sequence: clear promise, primary CTA, interactive product proof, feature explanation, simple how-it-works, final conversion CTA.
- Keep the landing visually natural, clean, and immediately understandable. Never add or imply a capability that is not present in the shipping Mapmory product.
- Treat the current editorial hero and product-flow direction as the preferred design; user review found it substantially better than the previous landing version.
- On mobile, keep the header, copy, forms, and ordinary content in a centered column with 24px side gutters. Only immersive product-proof surfaces such as the globe and detailed map may expand to an 8px edge gutter.
- On desktop, align the floating rounded header, ordinary copy, forms, and footer to a centered 1080px column. The editorial hero photo cluster may expand to 1240px, while the globe and detailed map may expand to 1180px as immersive product-proof surfaces.
- Keep the header as a clearly separated floating surface with outer margin, a rounded border, and enough contrast in both themes.
- Use the selected centered hero reference at `design-qa/hero-centered-scroll/reference-selected-option-3.png`: a single-axis headline, centered CTAs, and three taped Mapmory team photographs. Show 제주 first, then reveal 합정 and 여수 one at a time during a short natural scroll; never lock or hijack scrolling.
- Express the act of recording with a restrained handwritten `기억` and handwritten photo captions. Show the handwritten headline word from the first paint instead of gating it on scroll or image loading; use experience-led factual captions and honor reduced-motion preferences.
- Keep hero scrolling lightweight: isolate its reveal state from the 3D globe, animate large photos with compositor-friendly opacity/transform only, and pause the globe rendering loop while its surface is offscreen.
- The Korea landing demo may mirror the real app's add-to-map loop using team-owned example photos: add an example, color its province, then browse the added memory. Keep this state in browser memory only; never upload a file, call a persistence API, or write to the database from the landing demo.
- Keep the shared community-memory globe out of the main landing because it can misrepresent Mapmory as a community or travel-statistics product. Treat it only as a future separate experiment or campaign; the executable concept brief lives in `docs/community-memory-globe-experiment.md`.

Build app UI in `src/`. Keep `.openai/hosting.json`, `worker/index.js`, `scripts/prepare-sites-build.mjs`, and `tests/sites-worker.test.mjs` intact so the same local prototype can be handed to Sites. Before a Sites handoff, run `npm run build` and `npm run test:sites`; the build must leave `dist/client/index.html`, `dist/server/index.js`, and `dist/.openai/hosting.json`.
