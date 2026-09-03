import test from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

const appSource = await readFile(new URL("../src/App.jsx", import.meta.url), "utf8");
const stylesSource = await readFile(new URL("../src/styles.css", import.meta.url), "utf8");
const heroStylesSource = await readFile(new URL("../src/hero-memory-story.css", import.meta.url), "utf8");

test("mobile hero has one focal photo, the approved headline, and a non-looping entry sequence", () => {
  assert.match(appSource, /<span>여행의 순간을,<\/span><em>나만의 지도로\.<\/em>/);
  assert.match(appSource, /useMediaQuery\("\(max-width: 560px\)"\)/);
  assert.equal((appSource.match(/className="hero-mobile-source-record"/g) ?? []).length, 1);
  assert.match(appSource, /waitForIdle/);
  assert.match(appSource, /polygonsTransitionDuration=\{360\}/);
  assert.match(heroStylesSource, /hero-mobile-record-absorb 1800ms/);
  assert.doesNotMatch(heroStylesSource, /hero-mobile-record-absorb[^;]*infinite/);
});

test("mobile gallery separates the caption from controls and keeps touch targets readable", () => {
  assert.match(appSource, /memory-mobile-photo-caption/);
  assert.match(stylesSource, /\.memory-photo-meta > \.memory-photo-caption \{ display: none; \}/);
  assert.match(stylesSource, /\.memory-mobile-photo-caption \{[^}]*display: block;[^}]*word-break: keep-all;/s);
  assert.match(stylesSource, /\.memory-photo-dots button,[^{]*\{ width: 44px; height: 44px;/s);
  assert.match(stylesSource, /\.world-memory-close \{[^}]*min-height: 44px;[^}]*border-color: var\(--accent\);/s);
});

test("mobile memory panel owns one reversible browser history entry", () => {
  assert.match(appSource, /window\.history\.pushState\(/);
  assert.match(appSource, /window\.addEventListener\("popstate", handleHistoryBack\)/);
  assert.match(appSource, /isWorldMemoryHistoryEntry\(window\.history\.state\)/);
  assert.match(appSource, /window\.history\.back\(\)/);
  assert.match(stylesSource, /\.experience-stage\.is-memory-open \.globe-panel \{ display: none; \}/);
  assert.match(stylesSource, /\.experience-stage\.is-memory-open \.world-memory-card \{ position: relative;/);
});
