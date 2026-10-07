// Timeline for the hero demo: library scan -> place search -> matching photos fly to the map.
export const PHOTO_FINDER_LIBRARY_COUNT = 21474;

export const PHOTO_FINDER_TIMELINE = Object.freeze({
  scanEndMs: 1500,
  typeStepMs: 260,
  matchHoldMs: 700,
  flyMs: 1100,
});

// Only places the shipping app can search today: Korean provinces and whole countries.
export const PHOTO_FINDER_PLACES = Object.freeze([
  {
    key: "japan",
    query: "일본",
    label: "일본",
    scope: "world",
    regionCode: "392",
    target: [138.6, 36.4],
    view: { minLng: 120, maxLng: 150, minLat: 26, maxLat: 46 },
    foundCount: 86,
    photos: ["/assets/team-tokyo-street.jpeg"],
  },
  {
    key: "jeju",
    query: "제주",
    label: "제주",
    scope: "korea",
    regionCode: "KR-49",
    target: [126.55, 33.38],
    foundCount: 31,
    photos: ["/assets/team-jeju-coast.jpg", "/assets/team-jeju-coast-hero.jpg"],
  },
  {
    key: "usa",
    query: "미국",
    label: "미국",
    scope: "world",
    regionCode: "840",
    target: [-112.5, 37.4],
    view: { minLng: -128, maxLng: -64, minLat: 22, maxLat: 52 },
    foundCount: 52,
    photos: [
      "/assets/team-usa-bryce-canyon.jpg",
      "/assets/team-usa-antelope-canyon.jpg",
      "/assets/team-usa-las-vegas-day.jpg",
      "/assets/team-usa-las-vegas-fountain.jpg",
      "/assets/team-usa-las-vegas-venetian.jpg",
    ],
  },
]);

export function getPhotoFinderDuration(query) {
  const { scanEndMs, typeStepMs, matchHoldMs, flyMs } = PHOTO_FINDER_TIMELINE;
  return scanEndMs + query.length * typeStepMs + matchHoldMs + flyMs;
}

export function getPhotoFinderState(elapsedMs, query) {
  const { scanEndMs, typeStepMs, matchHoldMs, flyMs } = PHOTO_FINDER_TIMELINE;
  const typeEndMs = scanEndMs + query.length * typeStepMs;
  const matchEndMs = typeEndMs + matchHoldMs;
  const flyEndMs = matchEndMs + flyMs;
  const elapsed = Math.max(0, elapsedMs);
  const scanned = Math.min(1, elapsed / scanEndMs);

  let phase = "scan";
  if (elapsed >= flyEndMs) phase = "done";
  else if (elapsed >= matchEndMs) phase = "fly";
  else if (elapsed >= typeEndMs) phase = "match";
  else if (elapsed >= scanEndMs) phase = "type";

  const typedLength = phase === "scan"
    ? 0
    : Math.min(query.length, Math.floor((elapsed - scanEndMs) / typeStepMs) + 1);

  return {
    phase,
    typedQuery: query.slice(0, typedLength),
    scannedCount: Math.round(PHOTO_FINDER_LIBRARY_COUNT * (1 - (1 - scanned) ** 3)),
    isMapFilled: phase === "done" || (phase === "fly" && elapsed >= matchEndMs + flyMs * 0.7),
  };
}
