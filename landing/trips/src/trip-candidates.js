// Experimental defaults, not validated definitions of a trip. No home required.
export const TRIP_RULES = Object.freeze({
  visitRadiusKm: 30, visitGapHours: 36, linkHours: 6,
});
const DAY = 86400000;
export function distance(a, b) {
  const r = Math.PI / 180;
  const x = Math.sin((b.latitude - a.latitude) * r / 2) ** 2
    + Math.cos(a.latitude * r) * Math.cos(b.latitude * r) * Math.sin((b.longitude - a.longitude) * r / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, x))));
}
export function photoTime(record) {
  const value = record.date?.capturedAt;
  return typeof value === 'string' ? Date.parse(value + 'Z') : NaN;
}
const hasGps = record => Number.isFinite(record.gps?.latitude) && Number.isFinite(record.gps?.longitude)
  && Math.abs(record.gps.latitude) <= 90 && Math.abs(record.gps.longitude) <= 180;
export function chronological(photos) {
  return [...photos].sort((a, b) => {
    const at = photoTime(a), bt = photoTime(b);
    return (Number.isFinite(at) ? at : Infinity) - (Number.isFinite(bt) ? bt : Infinity) || a.index - b.index;
  });
}
function candidate(photos, basis) {
  return { id: `candidate-${Math.min(...photos.map(p => p.index))}`, photos: chronological(photos),
    basis: Object.fromEntries(photos.map(p => [p.index, basis])), edited: false };
}
function dayGroups(photos, basis = 'date') {
  const days = new Map();
  for (const photo of chronological(photos)) {
    const day = Math.floor(photoTime(photo) / DAY);
    if (!days.has(day)) days.set(day, []);
    days.get(day).push(photo);
  }
  return [...days.values()].map(photos => candidate(photos, basis));
}
/** Partition every input once; never write inferred GPS back to an original record. */
export function proposeTrips(records, { rules = TRIP_RULES } = {}) {
  if (new Set(records.map(p => p.index)).size !== records.length) throw new Error('사진 식별자가 중복됐어요.');
  const dated = chronological(records.filter(p => Number.isFinite(photoTime(p))));
  const undated = chronological(records.filter(p => !Number.isFinite(photoTime(p))));
  const located = dated.filter(hasGps);
  if (!located.length) {
    // Capture-volume inference is paused. No usable location/date anchors means
    // every photo stays visible in the single holding album, regardless of volume.
    return { groups: [], other: chronological(records), pattern: null, mode: 'holding', confirmed: false };
  }
  const groups = [];
  let current = null;
  for (const photo of located) {
    const time = photoTime(photo), day = Math.floor(time / DAY);
    if (!current || distance(current.photos[0].gps, photo.gps) > rules.visitRadiusKm
      || time - photoTime(current.photos.at(-1)) > rules.visitGapHours * 3600000
      || day - Math.floor(photoTime(current.photos.at(-1)) / DAY) > 1) {
      current = candidate([photo], 'location'); groups.push(current);
    } else { current.photos.push(photo); current.basis[photo.index] = 'location'; }
  }
  const anchors = groups.map(group => ({ group, photos: [...group.photos] }));
  const pending = [];
  for (const photo of dated.filter(p => !hasGps(p))) {
    const time = photoTime(photo);
    const matches = anchors.filter(({ photos }) => time >= photoTime(photos[0]) && time <= photoTime(photos.at(-1))
      && photos.some(anchor => Math.abs(time - photoTime(anchor)) <= rules.linkHours * 3600000));
    if (matches.length === 1) {
      const group = matches[0].group; group.photos.push(photo); group.basis[photo.index] = 'time';
    } else pending.push(photo);
  }
  for (const group of groups) group.photos = chronological(group.photos);
  groups.push(...dayGroups(pending));
  groups.sort((a, b) => photoTime(a.photos[0]) - photoTime(b.photos[0]));
  return { groups, other: [...undated], pattern: null, mode: located.length === dated.length ? 'location' : 'mixed', confirmed: false };
}

/** Low-cardinality provenance survives manual merges and splits. */
export function candidateMethod(group) {
  const types = new Set(Object.values(group.basis));
  if (types.size === 1) return [...types][0];
  return types.size === 2 && types.has('location') && types.has('time') ? 'time_assisted' : 'combined';
}
export function mergeCandidates(review, ids) {
  const keys = new Set(ids), selected = review.groups.filter(group => keys.has(group.id));
  if (selected.length < 2) throw new Error('합칠 묶음을 두 개 이상 선택해주세요.');
  const photos = chronological(selected.flatMap(group => group.photos));
  const merged = { id: `candidate-${Math.min(...photos.map(p => p.index))}`, photos,
    basis: Object.assign({}, ...selected.map(group => group.basis)), edited: true };
  const groups = review.groups.filter(group => !keys.has(group.id));
  groups.push(merged); groups.sort((a, b) => photoTime(a.photos[0]) - photoTime(b.photos[0]));
  return { ...review, groups, confirmed: false };
}
export function splitCandidate(review, id, photoIds) {
  const group = review.groups.find(group => group.id === id), selected = new Set(photoIds);
  if (!group) throw new Error('나눌 묶음을 찾지 못했어요.');
  const chosen = group.photos.filter(p => selected.has(p.index)), remaining = group.photos.filter(p => !selected.has(p.index));
  if (!chosen.length || !remaining.length) throw new Error('일부 사진만 선택해야 두 묶음으로 나눌 수 있어요.');
  const groups = review.groups.flatMap(item => item !== group ? [item] : [chosen, remaining].map(photos => ({
    id: `candidate-${Math.min(...photos.map(p => p.index))}`, photos: chronological(photos),
    basis: Object.fromEntries(photos.map(p => [p.index, group.basis[p.index]])), edited: true,
  })));
  groups.sort((a, b) => photoTime(a.photos[0]) - photoTime(b.photos[0]));
  return { ...review, groups, confirmed: false };
}
export function dismissCandidate(review, id) {
  const group = review.groups.find(group => group.id === id);
  if (!group) throw new Error('묶음을 찾지 못했어요.');
  return { ...review, groups: review.groups.filter(item => item !== group), other: chronological([...review.other, ...group.photos]), confirmed: false };
}
export function confirmCandidates(review) {
  if (!review.groups.length) throw new Error('확정할 묶음이 없어요.');
  return { ...review, confirmed: true };
}
