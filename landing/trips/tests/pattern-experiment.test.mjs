import test from 'node:test';
import assert from 'node:assert/strict';
import { estimatePatterns } from '../src/pattern-experiment.js';
import { proposeTrips, candidateMethod } from '../src/trip-candidates.js';
const photo = (index, day, gps = null) => ({index, gps, date: day ? {capturedAt: `${day}T12:00:00`} : null});
function fixture() {
  let i = 0;
  return [1,3,5,7,9,11,13,15,16].flatMap(day => Array.from({length:day >= 15 ? 20 : 2},
    () => photo(i++, `2026-09-${String(day).padStart(2,'0')}`)));
}
test('opt-in estimates burst days and reports active-day stats without zero filling', () => {
  const records = fixture(), before = JSON.stringify(records), result = estimatePatterns(records, records);
  assert.equal(result.status, 'ready');
  assert.deepEqual(result.stats, {active_days:9, span_days:16, empty_selected_days:7,
    daily_mean:6, daily_median:2, daily_max:20, burst_threshold:10, burst_day_count:2});
  assert.equal(result.groups.length,1); assert.equal(result.groups[0].photos.length,40);
  assert.equal(result.remaining.length,14);
  assert.equal(JSON.stringify(records),before);
  assert.equal(proposeTrips(records).groups.length,0,'default classification never runs the experiment');
});
test('empty days count inclusive calendar gaps across a leap day; unknown dates excluded', () => {
  const records = [photo(0,'2024-02-28'),photo(1,'2024-03-01'),photo(2,null)];
  const result = estimatePatterns(records,records);
  assert.equal(result.stats.span_days,3); assert.equal(result.stats.empty_selected_days,1);
  assert.equal(result.stats.daily_mean,1); assert.equal(result.status,'insufficient');
  assert.equal(result.remaining.length,3);
  const noDates = estimatePatterns([photo(0,null)],[photo(0,null)]);
  assert.equal(noDates.status,'no_dates'); assert.equal(noDates.stats.span_days,0);
  assert.equal(noDates.stats.empty_selected_days,0);
});
test('baseline includes selected GPS photos, targets exclude already classified photos', () => {
  const records = fixture();
  for (const p of records.slice(0,14)) p.gps={latitude:35,longitude:129};
  const defaultResult=proposeTrips(records);
  const pending=[...defaultResult.groups.filter(g=>candidateMethod(g)==='date').flatMap(g=>g.photos),...defaultResult.other];
  const result=estimatePatterns(records,pending);
  assert.equal(result.status,'ready'); assert.equal(result.stats.daily_median,2);
  assert.equal(result.groups[0].photos.length,40);
  assert.ok(result.groups.flatMap(g=>g.photos).every(p=>!p.gps));
  assert.equal(estimatePatterns(pending,pending).status,'insufficient','leftovers alone cannot establish the baseline');
});
test('insufficient history and uniformly high counts never force a candidate', () => {
  const records=fixture().filter(p=>p.index>=14), result=estimatePatterns(records,records);
  assert.equal(result.status,'insufficient'); assert.equal(result.groups.length,0);
  const uniform=Array.from({length:280},(_,i)=>photo(i,`2026-09-${String(Math.floor(i/20)+1).padStart(2,'0')}`));
  const noBurst=estimatePatterns(uniform,uniform);
  assert.equal(noBurst.status,'no_burst'); assert.equal(noBurst.groups.length,0);
});
test('separated bursts remain separate and every pending photo is retained exactly once', () => {
  const records=fixture();
  for (const p of records.filter(p=>p.index>=34)) p.date.capturedAt='2026-09-18T12:00:00';
  records.push(photo(54,null),photo(55,null,{latitude:35,longitude:129}));
  const result=estimatePatterns(records,records);
  assert.equal(result.groups.length,2);
  const ids=[...result.groups.flatMap(g=>g.photos),...result.remaining].map(p=>p.index);
  assert.equal(new Set(ids).size,records.length); assert.equal(ids.length,records.length);
  assert.deepEqual(estimatePatterns([...records].reverse(),records),result);
});
