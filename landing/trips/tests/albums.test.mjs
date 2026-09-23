import test from 'node:test';
import assert from 'node:assert/strict';
import { albumDateRange, coverPhoto, createAlbumResources } from '../src/albums.js';

test('album range ignores undated photos and retains camera dates', () => {
  assert.equal(albumDateRange([{date:{day:'2026-09-03'}},{date:null},{date:{day:'2026-09-01'}}]), '2026-09-01 – 2026-09-03');
  assert.equal(albumDateRange([{date:{day:'2026-09-01'}}]), '2026-09-01');
  assert.equal(albumDateRange([]), '촬영 날짜 정보 없음');
});
test('cover prefers browser-previewable files without dropping other formats', () => {
  const photos = [{name:'a.HEIC'}, {name:'b.JPG'}];
  assert.equal(coverPhoto(photos), photos[1]);
  assert.equal(coverPhoto(photos.slice(0,1)), photos[0]);
});
test('album object URLs are reused and released on screen transitions', t => {
  let created=0; const revoked=[];
  t.mock.method(URL,'createObjectURL',()=>`blob:test-${++created}`);
  t.mock.method(URL,'revokeObjectURL',url=>revoked.push(url));
  const resources=createAlbumResources(()=>new Blob());
  assert.equal(resources.url({index:0}),resources.url({index:0}));
  resources.url({index:1}); resources.clear();
  assert.deepEqual(revoked,['blob:test-1','blob:test-2']);
  assert.equal(resources.url({index:0}),'blob:test-3');
});
