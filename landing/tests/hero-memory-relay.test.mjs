import test from "node:test";
import assert from "node:assert/strict";
import {
  HERO_INTRO_MEMORY,
  HERO_MOBILE_ENTRY_APPLY_AT_MS,
  HERO_MOBILE_ENTRY_DURATION_MS,
  HERO_MEMORY_RELAY_STEPS,
  MEMORY_DENSITY_LEVELS,
  getHeroGlobeRenderSize,
  getHeroMobileEntryState,
  getHeroMemoryRelayState,
  getHeroMobileMapShift,
  getHeroRelayProgress,
  getMemoryDensity,
} from "../src/heroMemoryRelay.js";

test("runs the mobile first-entry story once within the approved timing window", () => {
  assert.equal(HERO_MOBILE_ENTRY_DURATION_MS, 1800);
  assert.ok(HERO_MOBILE_ENTRY_DURATION_MS >= 1500);
  assert.ok(HERO_MOBILE_ENTRY_DURATION_MS <= 2000);
  assert.ok(HERO_MOBILE_ENTRY_APPLY_AT_MS < HERO_MOBILE_ENTRY_DURATION_MS);
  assert.ok(HERO_MOBILE_ENTRY_DURATION_MS - HERO_MOBILE_ENTRY_APPLY_AT_MS >= 360);

  assert.equal(getHeroMobileEntryState(0).phase, "ready");
  assert.equal(getHeroMobileEntryState(1).phase, "playing");
  assert.equal(getHeroMobileEntryState(HERO_MOBILE_ENTRY_DURATION_MS).phase, "complete");
  assert.equal(getHeroMobileEntryState(HERO_MOBILE_ENTRY_DURATION_MS + 500).elapsedMs, HERO_MOBILE_ENTRY_DURATION_MS);
});

test("applies exactly one mobile journey record before the entry story completes", () => {
  const before = getHeroMobileEntryState(HERO_MOBILE_ENTRY_APPLY_AT_MS - 1);
  const after = getHeroMobileEntryState(HERO_MOBILE_ENTRY_APPLY_AT_MS);

  assert.equal(before.isApplied, false);
  assert.equal(before.relayState.cards[0].recordCount, 5);
  assert.equal(before.relayState.cards[0].density.level, "MEDIUM");
  assert.equal(after.isApplied, true);
  assert.equal(after.relayState.cards[0].recordCount, 6);
  assert.equal(after.relayState.cards[0].density.level, "HIGH");
});

test("mobile reduced motion resolves immediately to the same completed map meaning", () => {
  const state = getHeroMobileEntryState(0, { reducedMotion: true });

  assert.equal(state.phase, "complete");
  assert.equal(state.isApplied, true);
  assert.equal(state.isComplete, true);
  assert.equal(state.relayState.staticComposition, true);
  assert.equal(state.relayState.cards[0].density.level, "HIGH");
});

test("clamps the hero relay to its natural scroll travel", () => {
  assert.equal(getHeroRelayProgress({ sectionTop: 120, sectionHeight: 1800, frameHeight: 700, stickyTop: 80 }), 0);
  assert.equal(getHeroRelayProgress({ sectionTop: -470, sectionHeight: 1800, frameHeight: 700, stickyTop: 80 }), 0.5);
  assert.equal(getHeroRelayProgress({ sectionTop: -1500, sectionHeight: 1800, frameHeight: 700, stickyTop: 80 }), 1);
});

test("keeps the mobile globe still until the intro copy is almost gone", () => {
  assert.equal(getHeroMobileMapShift(0.82, 844), 0);
  assert.equal(getHeroMobileMapShift(0.9, 844) < 0, true);
});

test("moves the mobile globe higher after the copy transition finishes", () => {
  assert.equal(getHeroMobileMapShift(1, 568), -205);
  assert.equal(getHeroMobileMapShift(1, 844), -(844 * 0.26));
  assert.equal(getHeroMobileMapShift(1, 1200), -240);
});

test("keeps the hero globe renderer square at the actual mobile container size", () => {
  assert.equal(getHeroGlobeRenderSize(198.8), 198);
  assert.equal(getHeroGlobeRenderSize(455), 455);
  assert.equal(getHeroGlobeRenderSize(900), 520);
});

test("moves no more than one journey record at a time and reverses deterministically", () => {
  for (let step = 0; step <= 100; step += 1) {
    const progress = step / 100;
    const state = getHeroMemoryRelayState(progress);
    assert.ok(state.cards.filter(({ isMoving }) => isMoving).length <= 1);
    assert.deepEqual(getHeroMemoryRelayState(progress), state);
  }
});

test("gives the three post-intro cognitive scenes their own ordered scroll windows", () => {
  assert.equal(getHeroMemoryRelayState(0.22).phase, "moment");
  assert.equal(getHeroMemoryRelayState(0.50).phase, "record");
  assert.equal(getHeroMemoryRelayState(0.72).phase, "map");
  assert.equal(getHeroMemoryRelayState(0.92).phase, "map");
});

test("finishes the intro fade before the representative memory becomes focal", () => {
  const momentScene = getHeroMemoryRelayState(0.22);

  assert.equal(momentScene.phase, "moment");
  assert.equal(momentScene.introExit, 1);
  assert.ok(momentScene.momentReveal > 0.9);
  assert.equal(momentScene.representativeReveal, 1);
  assert.deepEqual(momentScene.supportPhotoReveals, [0, 0]);
});

test("reveals supporting photos sequentially inside the personal-moment scene", () => {
  const firstSupport = getHeroMemoryRelayState(0.275);
  const secondSupport = getHeroMemoryRelayState(0.345);

  assert.equal(firstSupport.phase, "moment");
  assert.ok(firstSupport.supportPhotoReveals[0] > 0);
  assert.equal(firstSupport.supportPhotoReveals[1], 0);
  assert.equal(secondSupport.phase, "moment");
  assert.equal(secondSupport.supportPhotoReveals[0], 1);
  assert.ok(secondSupport.supportPhotoReveals[1] > 0);
});

test("preserves the approved Jeju density on the untouched first viewport", () => {
  const state = getHeroMemoryRelayState(0);

  assert.equal(state.introCard.key, HERO_INTRO_MEMORY.key);
  assert.equal(state.introCard.country, "대한민국");
  assert.equal(state.introCard.density.level, "MEDIUM");
});

test("maps backend-compatible record counts to monotonically darker density buckets", () => {
  assert.equal(getMemoryDensity(0).level, "NONE");
  assert.equal(getMemoryDensity(1).level, "LOW");
  assert.equal(getMemoryDensity(2).level, "LOW");
  assert.equal(getMemoryDensity(3).level, "MEDIUM");
  assert.equal(getMemoryDensity(5).level, "MEDIUM");
  assert.equal(getMemoryDensity(6).level, "HIGH");
  assert.deepEqual(MEMORY_DENSITY_LEVELS.map(({ label }) => label), ["없음", "첫 기록", "쌓임", "많음"]);
});

test("counts a five-photo journey as one record when the complete bundle arrives", () => {
  const initialCounts = HERO_MEMORY_RELAY_STEPS.map(({ initialRecordCount }) => initialRecordCount);
  const before = getHeroMemoryRelayState(0.85);
  const after = getHeroMemoryRelayState(0.90);

  assert.equal(before.cards[0].photoCount, 5);
  assert.equal(before.cards[0].recordCount, 5);
  assert.equal(after.cards[0].recordCount - before.cards[0].recordCount, 1);
  assert.equal(before.cards[0].density.level, "MEDIUM");
  assert.equal(after.cards[0].density.level, "HIGH");
  assert.deepEqual(HERO_MEMORY_RELAY_STEPS.map(({ initialRecordCount }) => initialRecordCount), initialCounts);
});

test("keeps the destination lighter until the whole journey record reaches the globe", () => {
  const initial = getHeroMemoryRelayState(0.72);
  const moving = getHeroMemoryRelayState(0.83);
  const arrived = getHeroMemoryRelayState(0.84);
  const completed = getHeroMemoryRelayState(0.86);

  assert.equal(initial.cards[0].isApplied, false);
  assert.equal(initial.recordTravel, 0);
  assert.equal(moving.cards[0].isArrived, false);
  assert.equal(moving.cards[0].density.level, "MEDIUM");
  assert.equal(arrived.recordTravel, 1);
  assert.equal(arrived.cards[0].isArrived, true);
  assert.equal(arrived.cards[0].isApplied, false);
  assert.equal(arrived.cards[0].density.level, "MEDIUM");
  assert.equal(completed.cards[0].isApplied, true);
  assert.equal(completed.cards[0].density.level, "HIGH");
});

test("a normal 120px wheel step cannot skip a cognitive scene", () => {
  const viewportHeight = 1024;
  const sectionHeight = viewportHeight * 5.2;
  const frameHeight = viewportHeight - 112;
  const progressPerWheel = 120 / (sectionHeight - frameHeight);
  const phaseOrder = new Map([["intro", 0], ["moment", 1], ["record", 2], ["map", 3]]);

  for (let progress = 0; progress < 1; progress += progressPerWheel) {
    const current = getHeroMemoryRelayState(progress);
    const next = getHeroMemoryRelayState(Math.min(1, progress + progressPerWheel));
    assert.ok(phaseOrder.get(next.phase) - phaseOrder.get(current.phase) <= 1);
    const dominantReveals = [next.momentReveal, next.recordReveal, next.mapReveal]
      .filter((reveal) => reveal > 0.6);
    assert.ok(dominantReveals.length <= 1);
  }
});

test("encodes causal ordering even when scroll progress jumps forward quickly", () => {
  for (let step = 0; step <= 100; step += 1) {
    const state = getHeroMemoryRelayState(step / 100);
    const [firstSupport, secondSupport] = state.supportPhotoReveals;

    assert.ok(secondSupport <= firstSupport);
    assert.ok(firstSupport <= state.representativeReveal);
    if (state.recordFormation > 0) assert.equal(state.supportReveal, 1);
    if (state.recordTravel > 0) assert.equal(state.recordFormation, 1);
    if (state.mapResultReveal > 0) assert.equal(state.recordTravel, 1);
    if (state.cards[0].isApplied) assert.equal(state.cards[0].isArrived, true);
  }

  const jumped = getHeroMemoryRelayState(0.92);
  assert.deepEqual(jumped.supportPhotoReveals, [1, 1]);
  assert.equal(jumped.recordFormation, 1);
  assert.equal(jumped.recordTravel, 1);
  assert.equal(jumped.recordArrived, true);
  assert.equal(jumped.cards[0].recordCount, jumped.cards[0].initialRecordCount + 1);
});

test("keeps one cognitive layer dominant and the representative photo visually continuous", () => {
  for (let step = 0; step <= 100; step += 1) {
    const state = getHeroMemoryRelayState(step / 100);
    const dominantLayers = [state.momentReveal, state.recordReveal, state.mapReveal]
      .filter((reveal) => reveal > 0.6);

    assert.ok(dominantLayers.length <= 1);
    assert.ok(state.supportPhotoReveals.every((reveal) => reveal <= state.representativeReveal));
  }

  for (const progress of [0.25, 0.45, 0.55, 0.75, 0.83]) {
    assert.equal(getHeroMemoryRelayState(progress).recordObjectOpacity, 1);
  }
});

test("reduced motion resolves directly to the stable completed accumulation state", () => {
  const state = getHeroMemoryRelayState(0, { reducedMotion: true });

  assert.equal(state.phase, "map");
  assert.equal(state.staticComposition, true);
  assert.equal(state.completedCount, HERO_MEMORY_RELAY_STEPS.length);
  assert.equal(state.activeIndex, -1);
  assert.ok(state.cards.every(({ isApplied, isMoving }) => isApplied && !isMoving));
  assert.equal(state.recordArrived, true);
  assert.equal(state.recordTravel, 1);
  assert.equal(state.mapResultReveal, 1);
  assert.equal(state.recordObjectOpacity, 1);
});
