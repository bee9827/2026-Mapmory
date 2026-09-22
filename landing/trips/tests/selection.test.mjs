import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
import {validateTripSelection, excludeOversizedPhotos, TRIP_SELECTION_LIMITS} from '../src/selection.js';
import {organizePhotos} from '../src/pipeline.js';
import {parsePhotoBatches} from '../src/photoProcessing.js';
import {localDay} from '../src/dates.js';
import {metadataSummary} from '../src/analytics.js';
const analytics=()=>({track(){},resetFlow(){},setContext(){},environment:{environment_eligible:true}});

const photo = {size: 5 * 1024 * 1024};
test('Trips supports the 500+ experiment, including 1,000 files over 500MB', () => {
  assert.doesNotThrow(() => validateTripSelection(Array(500).fill(photo)));
  assert.doesNotThrow(() => validateTripSelection(Array(1000).fill(photo)));
  assert.doesNotThrow(() => validateTripSelection([{size: TRIP_SELECTION_LIMITS.fileBytes}]));
  assert.throws(() => validateTripSelection(Array(1001).fill(photo)), /1,000/);
  assert.throws(() => validateTripSelection([{size: TRIP_SELECTION_LIMITS.fileBytes + 1}]), /50MB/);
});

test('pipeline refuses oversized selections before reading files or fetching data', async () => {
  const oldFetch = globalThis.fetch;
  let fetched = false, read = false;
  globalThis.fetch = () => { fetched = true; throw Error('unexpected fetch'); };
  try {
    await assert.rejects(organizePhotos([{size: TRIP_SELECTION_LIMITS.fileBytes + 1, arrayBuffer() {read = true;}}]), /50MB/);
    assert.equal(fetched, false);
    assert.equal(read, false);
  } finally { globalThis.fetch = oldFetch; }
});

test('invalid replacement preserves existing results and never resets or starts processing', async () => {
  const source = readFileSync(new URL('../src/app.js', import.meta.url), 'utf8');
  const start = source.slice(source.indexOf('async function startOrganization('), source.indexOf('\nfunction buildTree('));
  for (const records of [[], [{index: 0}]]) {
    let message = '', reset = false;
    const context = {analytics:analytics(),state: {records}, isPhoto: () => true, validateTripSelection, excludeOversizedPhotos,
      renderStart: text => {message = text;}, showResultError: text => {message = text;},
      reset: () => {reset = true; throw Error('should not reset');}};
    await runInNewContext(`${start}\nstartOrganization(Array(1001).fill({size: 1}));`, context);
    assert.match(message, /1,000/);
    assert.equal(reset, false);
    assert.equal(context.state.records, records);
  }
});

test('oversized photos are excluded without dropping or reordering acceptable photos', () => {
  const large = {size: TRIP_SELECTION_LIMITS.fileBytes + 1};
  const boundary = {size: TRIP_SELECTION_LIMITS.fileBytes};
  const files = [photo, large, boundary];
  assert.deepEqual(excludeOversizedPhotos(files), {photos: [photo, boundary], oversized: [large]});
  assert.deepEqual(files, [photo, large, boundary]);
});

test('mixed input keeps processing; all oversized input preserves previous results', async () => {
  const source = readFileSync(new URL('../src/app.js', import.meta.url), 'utf8');
  const start = source.slice(source.indexOf('async function startOrganization('), source.indexOf('\nfunction buildTree('));
  const large = {size: TRIP_SELECTION_LIMITS.fileBytes + 1};
  let message = '', received, resetCalls = 0;
  const previous = [{index: 0}];
  const state = {records: previous};
  const context = {analytics:analytics(),metadataSummary,performance,processingStarted:0,processingSeconds:()=>1,state, isPhoto: () => true, validateTripSelection, excludeOversizedPhotos,
    n: String, readGeneration: 0, reset() {resetCalls++;}, renderProgress() {},
    organizePhotos: async files => {received = files; return {records: files, locationDataUnavailable: false};},
    planArchives: records => records, renderResults() {}, focusHeading() {}, announce() {}, progressUpdate() {},
    renderStart: text => {message = text;}, showResultError: text => {message = text;}, files: [large]};
  await runInNewContext(`${start}\nstartOrganization(files);`, context);
  assert.match(message, /모두 50MB/);
  assert.equal(resetCalls, 0);
  assert.equal(state.records, previous);
  context.files = [photo, large, photo];
  await runInNewContext(`${start}\nstartOrganization(files);`, context);
  assert.deepEqual(received, [photo, photo]);
  assert.equal(resetCalls, 1);
  assert.equal(state.oversized, 1);
  assert.equal(state.skipped, 0);
  assert.equal(state.phase, 'complete');
  assert.match(source, /if \(state\.oversized\) text\.append/);
});

test('large selections retain order and at most three metadata reads in flight', async () => {
  let active = 0, maxActive = 0;
  const files = Array.from({length: 501}, (_, index) => index);
  const parsed = await parsePhotoBatches(files, async value => {
    active++; maxActive = Math.max(maxActive, active);
    await new Promise(resolve => setTimeout(resolve, 0));
    active--; return value;
  });
  assert.equal(maxActive, 3);
  assert.deepEqual(parsed, files);
});

test('archive date uses local calendar components near midnight', () => {
  const previousTZ = process.env.TZ;
  process.env.TZ = 'Asia/Seoul';
  try {
    const date = new Date('2026-09-21T15:05:00Z');
    assert.equal(localDay(date), '2026-09-22');
    assert.equal(date.toISOString().slice(0, 10), '2026-09-21');
  } finally {
    if (previousTZ === undefined) delete process.env.TZ;
    else process.env.TZ = previousTZ;
  }
});
