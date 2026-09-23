import { chronological, photoTime } from './trip-candidates.js';

// Initial experimental thresholds, not validated travel definitions.
export const PATTERN_RULES = Object.freeze({ spanDays: 14, activeDays: 7, ordinaryDays: 5, ratio: 3, minPhotos: 10 });
const DAY = 86400000;
const dayOf = p => Math.floor(photoTime(p) / DAY);
export const lacksLocation = p => !Number.isFinite(p.gps?.latitude) || !Number.isFinite(p.gps?.longitude)
  || Math.abs(p.gps.latitude) > 90 || Math.abs(p.gps.longitude) > 180;
function byDay(records) {
  const days = new Map();
  for (const p of chronological(records.filter(p => Number.isFinite(photoTime(p))))) {
    const day = dayOf(p);
    if (!days.has(day)) days.set(day, []);
    days.get(day).push(p);
  }
  return days;
}

// Baseline: ALL selected dated photos, not only GPS-missing leftovers.
// Targets: only unclassified GPS-missing photos supplied by the result view.
// Missing selected days are unknown, never zero. This does not establish a daily-life baseline.
export function estimatePatterns(records, pending) {
  const days = byDay(records), counts = [...days.values()].map(photos => photos.length).sort((a, b) => a - b);
  const middle = Math.floor(counts.length / 2);
  const median = counts.length ? (counts.length % 2 ? counts[middle] : (counts[middle - 1] + counts[middle]) / 2) : 0;
  const threshold = Math.max(PATTERN_RULES.minPhotos, median * PATTERN_RULES.ratio);
  const keys = [...days.keys()];
  const span = keys.length ? keys.at(-1) - keys[0] + 1 : 0;
  const targets = pending.filter(lacksLocation), targetDays = byDay(targets);
  const stats = {
    active_days: days.size, span_days: span, empty_selected_days: span - days.size,
    daily_mean: counts.length ? Math.round(counts.reduce((a, b) => a + b, 0) / counts.length * 100) / 100 : 0,
    daily_median: median, daily_max: counts.at(-1) || 0, burst_threshold: threshold,
    burst_day_count: [...targetDays.values()].filter(photos => photos.length >= threshold).length,
  };
  let status = !targetDays.size ? 'no_dates'
    : stats.span_days < PATTERN_RULES.spanDays || days.size < PATTERN_RULES.activeDays
      || counts.filter(n => n < threshold).length < PATTERN_RULES.ordinaryDays ? 'insufficient' : 'ready';
  const groups = [];
  let current = null, lastDay = null;
  if (status === 'ready') for (const [day, photos] of targetDays) {
    if (photos.length < threshold) { current = null; continue; }
    if (!current || day - lastDay !== 1) {
      current = { photos: [], basis: {} }; groups.push(current);
    }
    current.photos.push(...photos);
    for (const p of photos) current.basis[p.index] = 'pattern';
    lastDay = day;
  }
  if (status === 'ready' && !groups.length) status = 'no_burst';
  const assigned = new Set(groups.flatMap(g => g.photos.map(p => p.index)));
  return { groups, remaining: pending.filter(p => !assigned.has(p.index)), stats, status };
}
