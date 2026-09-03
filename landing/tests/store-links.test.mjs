import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const appSource = await readFile(new URL("../src/App.jsx", import.meta.url), "utf8");
const storyCss = await readFile(new URL("../src/hero-memory-story.css", import.meta.url), "utf8");

test("the landing page exposes the supplied App Store URL alongside Google Play", () => {
  assert.match(appSource, /https:\/\/apps\.apple\.com\/kr\/app\/mapmory-[^\"]+\/id6807056166/);
  assert.match(appSource, /platform="ios" label="App Store"/);
  assert.match(appSource, /platform="android" label="Google Play"/);
});

test("reduced motion collapses the scroll relay and keeps a static summary", () => {
  assert.match(appSource, /className="hero-reduced-summary"/);
  assert.match(storyCss, /prefers-reduced-motion: reduce[\s\S]*\.hero-memory-story \{ height: auto; min-height: 0; \}/);
  assert.match(storyCss, /hero-reduced-summary[\s\S]*display: flex/);
});
