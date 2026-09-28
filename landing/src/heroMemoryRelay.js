export const MEMORY_DENSITY_LEVELS = Object.freeze([
  Object.freeze({ level: "NONE", label: "없음", min: 0, max: 0, color: "#e7ebe6" }),
  Object.freeze({ level: "LOW", label: "첫 기록", min: 1, max: 2, color: "#bdeed7" }),
  Object.freeze({ level: "MEDIUM", label: "쌓임", min: 3, max: 5, color: "#65d7a7" }),
  Object.freeze({ level: "HIGH", label: "많음", min: 6, max: Number.POSITIVE_INFINITY, color: "#0a9d67" }),
]);

export const HERO_INTRO_MEMORY = Object.freeze({
  key: "jeju-coast",
  country: "대한민국",
  recordCount: 5,
});

export const HERO_MEMORY_RELAY_STEPS = Object.freeze([
  Object.freeze({
    key: "usa-west",
    country: "미국",
    initialRecordCount: 5,
    photoCount: 5,
  }),
]);

export const HERO_MOBILE_ENTRY_DURATION_MS = 1800;
export const HERO_MOBILE_ENTRY_APPLY_AT_MS = 1368;
export const HERO_MOBILE_ENTRY_INITIAL_PROGRESS = 0.21;
export const HERO_MOBILE_ENTRY_COMPLETE_PROGRESS = 0.93;

const STORY_BEATS = Object.freeze({
  moment: Object.freeze([0.16, 0.21, 0.38, 0.45]),
  record: Object.freeze([0.41, 0.47, 0.63, 0.70]),
});

const SUPPORT_REVEAL_RANGES = Object.freeze([
  Object.freeze([0.24, 0.31]),
  Object.freeze([0.31, 0.38]),
]);

export function clampUnit(value) {
  return Math.min(1, Math.max(0, Number.isFinite(value) ? value : 0));
}

function easeInOut(progress) {
  const safeProgress = clampUnit(progress);
  return safeProgress * safeProgress * (3 - (2 * safeProgress));
}

export function getMemoryDensity(count) {
  const safeCount = Math.max(0, Math.floor(Number.isFinite(count) ? count : 0));
  return MEMORY_DENSITY_LEVELS.find(({ min, max }) => safeCount >= min && safeCount <= max)
    ?? MEMORY_DENSITY_LEVELS[0];
}

export function getHeroRelayProgress({ sectionTop, sectionHeight, frameHeight, stickyTop = 0 }) {
  const travel = Math.max(1, sectionHeight - frameHeight);
  return clampUnit((stickyTop - sectionTop) / travel);
}

function getWindowOpacity(progress, [start, enterEnd, exitStart, end]) {
  const enter = clampUnit((progress - start) / Math.max(0.001, enterEnd - start));
  const exit = 1 - clampUnit((progress - exitStart) / Math.max(0.001, end - exitStart));
  return Math.min(enter, exit);
}

export function getHeroMobileMapShift(introExit, viewportHeight) {
  const moveProgress = clampUnit((clampUnit(introExit) - 0.82) / 0.18);
  if (moveProgress === 0) return 0;
  const travel = Math.min(240, Math.max(205, viewportHeight * 0.26));
  return -(travel * moveProgress);
}

export function getHeroGlobeRenderSize(containerWidth) {
  const safeWidth = Number.isFinite(containerWidth) ? containerWidth : 1;
  return Math.max(1, Math.min(520, Math.floor(safeWidth)));
}

export function getHeroMemoryRelayState(rawProgress, { reducedMotion = false } = {}) {
  const progress = reducedMotion ? 1 : clampUnit(rawProgress);
  const representativeReveal = easeInOut(clampUnit((progress - 0.16) / 0.05));
  const supportPhotoReveals = SUPPORT_REVEAL_RANGES.map(([start, end]) => (
    easeInOut(clampUnit((progress - start) / (end - start)))
  ));
  const supportReveal = supportPhotoReveals.at(-1) ?? 0;
  const recordFormation = easeInOut(clampUnit((progress - 0.43) / 0.10));
  const recordTravel = easeInOut(clampUnit((progress - 0.72) / 0.12));
  const recordArrived = progress >= 0.84;
  const isApplied = progress >= 0.86;
  const isMoving = progress >= 0.72 && !recordArrived;
  const cards = HERO_MEMORY_RELAY_STEPS.map((step) => {
    const recordCount = step.initialRecordCount + (isApplied ? 1 : 0);

    return {
      ...step,
      segmentProgress: recordTravel,
      localProgress: recordTravel,
      isMoving,
      isArrived: recordArrived,
      isApplied,
      recordCount,
      density: getMemoryDensity(recordCount),
    };
  });

  const activeIndex = (isMoving || (recordArrived && !isApplied)) ? 0 : -1;
  const completedCount = cards.filter(({ isApplied: applied }) => applied).length;
  const phase = progress < 0.16
    ? "intro"
    : progress < 0.43
      ? "moment"
      : progress < 0.68
        ? "record"
        : "map";
  const momentReveal = getWindowOpacity(progress, STORY_BEATS.moment);
  const recordReveal = getWindowOpacity(progress, STORY_BEATS.record);
  const mapReveal = clampUnit((progress - 0.66) / 0.08);
  const mapResultReveal = clampUnit((progress - 0.86) / 0.07);
  const recordObjectOpacity = reducedMotion
    ? 1
    : representativeReveal * (1 - mapResultReveal);

  return {
    progress,
    phase,
    reducedMotion,
    staticComposition: reducedMotion,
    activeIndex,
    activeCountry: activeIndex >= 0 ? cards[activeIndex].country : null,
    completedCount,
    cards,
    introCard: {
      ...HERO_INTRO_MEMORY,
      density: getMemoryDensity(HERO_INTRO_MEMORY.recordCount),
    },
    introExit: clampUnit((progress - 0.10) / 0.08),
    momentReveal,
    representativeReveal,
    supportPhotoReveals,
    supportReveal,
    recordReveal,
    recordFormation,
    mapReveal,
    recordTravel,
    recordArrived,
    mapResultReveal,
    recordObjectOpacity,
    globeOpacity: progress < 0.16 ? 1 : mapReveal,
    globeScale: progress < 0.16 ? 1.04 : 0.90 + (0.14 * mapReveal),

    // Transitional aliases keep the existing renderer stable while App/CSS adopt
    // the three-scene vocabulary. They can be removed with that integration.
    stackReveal: representativeReveal,
    handoffReveal: mapResultReveal,
    memoryReveal: momentReveal,
    unfoldReveal: supportReveal,
    bundleReveal: recordReveal,
    accumulateReveal: mapReveal,
    resultReveal: mapResultReveal,
    recordVisualOpacity: recordObjectOpacity,
    bundleTravel: recordTravel,
  };
}

export function getHeroMobileEntryState(rawElapsedMs, { reducedMotion = false } = {}) {
  if (reducedMotion) {
    return {
      elapsedMs: HERO_MOBILE_ENTRY_DURATION_MS,
      phase: "complete",
      isApplied: true,
      isComplete: true,
      relayState: getHeroMemoryRelayState(1, { reducedMotion: true }),
    };
  }

  const elapsedMs = Math.min(
    HERO_MOBILE_ENTRY_DURATION_MS,
    Math.max(0, Number.isFinite(rawElapsedMs) ? rawElapsedMs : 0),
  );
  const isApplied = elapsedMs >= HERO_MOBILE_ENTRY_APPLY_AT_MS;
  const isComplete = elapsedMs >= HERO_MOBILE_ENTRY_DURATION_MS;

  return {
    elapsedMs,
    phase: isComplete ? "complete" : elapsedMs > 0 ? "playing" : "ready",
    isApplied,
    isComplete,
    relayState: getHeroMemoryRelayState(
      isApplied ? HERO_MOBILE_ENTRY_COMPLETE_PROGRESS : HERO_MOBILE_ENTRY_INITIAL_PROGRESS,
    ),
  };
}
