import test from 'node:test';
import assert from 'node:assert/strict';
import { proposeTrips, candidateMethod, mergeCandidates, splitCandidate, dismissCandidate, confirmCandidates } from '../src/trip-candidates.js';
const seoul = { latitude: 37.56, longitude: 126.97 }, busan = { latitude: 35.17, longitude: 129.07 };
const photo = (index, day, gps = busan, hour = 12) => ({ index, name: `${index}.jpg`, gps, city: gps ? '테스트 지역' : null,
  date: day ? { day: `2026-09-${String(day).padStart(2, '0')}`, capturedAt: `2026-09-${String(day).padStart(2, '0')}T${String(hour).padStart(2, '0')}:00:00` } : null });
function partition(result, input) {
  const ids = [...result.groups.flatMap(g => g.photos), ...result.other].map(p => p.index);
  assert.equal(new Set(ids).size, ids.length);
  assert.deepEqual(ids.sort((a, b) => a - b), input.map(p => p.index).sort((a, b) => a - b));
}
export function patternPhotos() {
  let index = 0;
  return [1, 3, 5, 7, 9, 11, 13, 15, 16].flatMap(day => Array.from({ length: day >= 15 ? 20 : 2 }, () => photo(index++, day, null)));
}
test('no home required; different places or nonconsecutive dates split visits', () => {
  const input = [photo(0, 1), photo(1, 2), photo(2, 4), photo(3, 4, seoul), photo(4, 4, busan, 14)];
  const result = proposeTrips(input);
  assert.deepEqual(result.groups.map(g => g.photos.map(p => p.index)), [[0, 1], [2], [3], [4]]);
  partition(result, input); assert.equal(result.mode, 'location');
});
test('visits use an anchored radius rather than chain drifting through cities', () => {
  const input = [photo(0, 1, { latitude: 37, longitude: 127 }, 9), photo(1, 1, { latitude: 37.2, longitude: 127 }, 10), photo(2, 1, { latitude: 37.4, longitude: 127 }, 11)];
  assert.deepEqual(proposeTrips(input).groups.map(g => g.photos.length), [2, 1]);
});
test('partial GPS links only bounded, nearby-in-time photos; never synthesizes coordinates', () => {
  const input = [photo(0, 1, busan, 10), photo(1, 1, null, 11), photo(2, 1, busan, 12), photo(3, 1, null, 13), photo(4, null, busan)];
  const before = JSON.stringify(input), result = proposeTrips(input);
  assert.equal(result.mode, 'mixed');
  assert.deepEqual(result.groups[0].photos.map(p => p.index), [0, 1, 2]);
  assert.equal(result.groups[0].basis[1], 'time'); assert.equal(candidateMethod(result.groups[0]), 'time_assisted');
  assert.equal(result.groups[1].basis[3], 'date'); assert.deepEqual(result.other.map(p => p.index), [4]);
  assert.equal(JSON.stringify(input), before); partition(result, input);
});
test('ambiguous location intervals and long unobserved gaps do not guess a location', () => {
  const input = [photo(0, 1, busan, 10), photo(1, 1, seoul, 10), photo(2, 1, null, 10)];
  assert.equal(proposeTrips(input).groups.find(g => g.photos.some(p => p.index === 2)).basis[2], 'date');
  const overnight = [photo(0, 1, busan, 8), photo(1, 1, null, 20), photo(2, 2, busan, 8)];
  assert.equal(proposeTrips(overnight).groups.find(g => g.photos.some(p => p.index === 1)).basis[1], 'date');
});
test('GPS-free burst inference is paused even with adequate observed history', () => {
  const input = patternPhotos();
  assert.equal(proposeTrips(input).pattern, null);
  assert.deepEqual(proposeTrips(input).groups, []);
  assert.equal(proposeTrips(input).mode, 'holding');
  const insufficient = proposeTrips(input.filter(p => p.date.day >= '2026-09-15'));
  assert.equal(insufficient.mode, 'holding');
  assert.deepEqual(insufficient.groups, []);
});
test('holding preserves ordinary photos and a large burst without classification', () => {
  const input = patternPhotos(), result = proposeTrips(input);
  assert.equal(result.other.length, input.length);
  assert.equal(result.groups.length, 0);
  partition(result, input);
});
test('many photos every day is not treated as a burst', () => {
  const input = Array.from({ length: 14 * 20 }, (_, i) => photo(i, Math.floor(i / 20) + 1, null));
  const result = proposeTrips(input);
  assert.equal(result.mode, 'holding'); assert.equal(result.groups.length, 0);
  partition(result, input);
});
test('no dates and invalid GPS have safe fallbacks', () => {
  const input = [photo(0, null), photo(1, null, null)];
  assert.equal(proposeTrips(input).mode, 'holding'); assert.equal(proposeTrips(input).groups.length, 0);
  partition(proposeTrips(input), input);
  assert.equal(proposeTrips([photo(0, 1, { latitude: 999, longitude: 127 })]).mode, 'holding');
  assert.deepEqual(proposeTrips([]).groups, []);
});
test('merge, split and dismiss preserve every photo and original per-photo evidence', () => {
  const input = [photo(0, 1), photo(1, 1, null, 13), photo(2, 4), photo(3, null)];
  const original = proposeTrips(input), merged = mergeCandidates(original, original.groups.map(g => g.id));
  assert.equal(merged.groups.length, 1); assert.equal(candidateMethod(merged.groups[0]), 'combined');
  assert.equal(original.groups.length, 3); partition(merged, input);
  const split = splitCandidate(merged, merged.groups[0].id, [0, 1]);
  assert.equal(split.groups.length, 2); assert.equal(split.groups[0].basis[1], 'date'); partition(split, input);
  const dismissed = dismissCandidate(split, split.groups[0].id); partition(dismissed, input);
  assert.equal(dismissed.other.length, 3);
});
test('confirmation is explicit, edit clears it; invalid edits cannot lose photos', () => {
  const review = proposeTrips([photo(0, 1), photo(1, 3)]), confirmed = confirmCandidates(review);
  assert.equal(review.confirmed, false); assert.equal(confirmed.confirmed, true);
  assert.equal(mergeCandidates(confirmed, confirmed.groups.map(g => g.id)).confirmed, false);
  assert.throws(() => mergeCandidates(review, [review.groups[0].id]));
  assert.throws(() => splitCandidate(review, review.groups[0].id, []));
  assert.throws(() => splitCandidate(review, review.groups[0].id, [0]));
  assert.throws(() => confirmCandidates(proposeTrips([])));
  assert.throws(() => proposeTrips([photo(0, 1), photo(0, 2)]));
});
test('1000 mixed metadata photos remain a deterministic exact partition', () => {
  const input = Array.from({ length: 1000 }, (_, i) => photo(i, i % 11 === 0 ? null : i % 28 + 1, i % 3 ? busan : null, i % 24));
  const before = JSON.stringify(input), result = proposeTrips(input);
  partition(result, input); assert.equal(JSON.stringify(input), before);
  assert.deepEqual(proposeTrips([...input].reverse()), result);
});
