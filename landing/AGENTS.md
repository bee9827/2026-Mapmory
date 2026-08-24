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
- Lead with travel, then let distinctive places such as a bakery, ramen shop, or dessert shop show how a personal map can trigger memory.
- Keep the first viewport focused on the value proposition and conversion CTA; do not make the headline, globe, and photo compete at once.
- Place the memory/location selector before the globe and make its purpose explicit before the user starts interacting.
- Give the 3D globe a visible bounded panel and preserve vertical touch scrolling with `touch-action: pan-y`.
- Use genuine photography only. Every external photo must have a visible photographer/platform credit and source link.
- Keep the globe and photo preview concise. The next product-proof step must use the real app's 대한민국/전세계 scope pattern and the same 17-region boundary data as the Kotlin client.
- Do not show a fake editor, collection form, or recording workflow that differs from the shipping client.
- Preserve the conversion path `3D 지구본 → 대한민국 상세지도 → 지역의 실제 장소 기억 → 내 기억 지도 만들기 → 다운로드`.
- Preserve basic landing-page sequence: clear promise, primary CTA, interactive product proof, feature explanation, simple how-it-works, final conversion CTA.

Build app UI in `src/`. Keep `.openai/hosting.json`, `worker/index.js`, `scripts/prepare-sites-build.mjs`, and `tests/sites-worker.test.mjs` intact so the same local prototype can be handed to Sites. Before a Sites handoff, run `npm run build` and `npm run test:sites`; the build must leave `dist/client/index.html`, `dist/server/index.js`, and `dist/.openai/hosting.json`.
