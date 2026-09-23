import test from 'node:test';
import assert from 'node:assert/strict';
import { createTripReview } from '../src/trip-review.js';
import { createAnalytics, CONSENT_KEY } from '../src/analytics.js';

class Node {
  constructor(tag) { this.tag = tag; this.children = []; this.events = {}; this.attributes = {}; this.textContent = ''; }
  append(...children) { this.children.push(...children); for(const child of children)child.parentNode=this; }
  replaceChildren(...children) { this.replacements=(this.replacements||0)+1; for(const child of this.children)child.parentNode=null; this.children=[]; this.append(...children); }
  setAttribute(key, value) { this.attributes[key] = value; }
  addEventListener(type, action) { this.events[type] = action; }
  focus() { this.focused = true; }
  click() { if (!this.disabled) this.events.click?.(); }
  change(checked) { this.checked = checked; this.events.change?.(); }
}
const descendants = root => [root, ...root.children.flatMap(descendants)];
const find = (root, text) => descendants(root).find(node => node.tag === 'button' && node.textContent === text);
const title = root => descendants(root).find(node => node.tag === 'h1').textContent;
const gps = { latitude: 35.17, longitude: 129.07 };
const photo = (index, day, location = gps, hour = 12) => ({ index, name: `PRIVATE-${index}.jpg`, city: 'PRIVATE CITY', gps: location,
  date: { day: `2026-09-${String(day).padStart(2, '0')}`, capturedAt: `2026-09-${String(day).padStart(2, '0')}T${hour}:00:00` } });
function setup(records) {
  const root = new Node('main'), albums = [];
  const doc = { createElement: tag => new Node(tag), head: new Node('head'), referrer: '' };
  const win = { location: new URL('https://map-mory.com/trips/'), navigator: { userAgent: 'iPhone Safari' },
    localStorage: { getItem: key => key === CONSENT_KEY ? 'granted' : null, setItem() {} } };
  const analytics = createAnalytics({ config: { measurementId: 'G-TEST' }, win, doc });
  const events = () => win.dataLayer.filter(args => args[0] === 'event').map(args => ({ event: args[1], ...args[2] }));
  let back = 0, tick = 0;
  const review = createTripReview({ records, root, analytics, doc, now: () => tick++ * 1000,
    photoAlbum: options => { albums.push(options); return new Node('details'); }, clearAlbums() { albums.length = 0; },
    photoUrl: () => 'blob:test', onBack() { back++; }, createSurvey: () => new Node('section') });
  return { review, root, albums, events, back: () => back };
}

test('read-only results show albums first, survey below, with no editing controls', () => {
  const records = [photo(0,1,gps,10),photo(1,1,null,11),photo(2,1,gps,12),photo(3,4)];
  const original=JSON.stringify(records), h=setup(records); h.review.open();
  assert.equal(title(h.root),'2개 여행 후보를 찾았어요');
  const all=descendants(h.root);
  assert.equal(all.some(n=>n.type==='checkbox'),false);
  assert.equal(all.some(n=>n.tag==='button'&&/합치기|나누기|확정|되돌리기|여행에서 제외/.test(n.textContent)),false);
  assert.equal(h.root.children[3].className,'album-list classified-albums');
  assert.equal(h.albums[0].label,'관측 + 추정 · 촬영 시간으로 연결');
  h.albums[0].onExpand();h.albums[0].onExpand();
  find(h.root,'원본 위치·날짜별 보기 / ZIP').click();assert.equal(h.back(),1);
  h.review.open();
  assert.equal(h.events().filter(e=>e.event==='trips_grouping_start').length,1);
  assert.equal(h.events().filter(e=>e.event==='trips_results_view').length,1);
  assert.equal(h.events().filter(e=>e.event==='trips_album_open').length,1);
  assert.equal(h.events().some(e=>/trips_review_/.test(e.event)),false);
  assert.doesNotMatch(JSON.stringify(h.events()),/PRIVATE|2026-09|latitude|longitude|candidate-/);
  assert.equal(JSON.stringify(records),original);
});
test('GPS-free bursts, ordinary dates and undated photos all form one holding album',()=>{
  let index=0;
  const records=[1,3,5,7,9,11,13,15,16].flatMap(day=>Array.from({length:day>=15?20:2},()=>photo(index++,day,null)));
  records.push({index:index++,name:'unknown.jpg',date:null,gps:null});
  const h=setup(records);h.review.open();
  assert.equal(title(h.root),'분류 보류 사진을 한곳에 모았어요');
  assert.equal(h.albums.length,1);
  assert.equal(h.albums[0].title,'분류 보류');
  assert.equal(h.albums[0].photos.length,55);
  assert.equal(new Set(h.albums.flatMap(a=>a.photos.map(p=>p.index))).size,55);
  const event=h.events().find(e=>e.event==='trips_results_view');
  assert.equal(event.candidate_count,0);assert.equal(event.other_photo_count,55);
  assert.equal(event.grouping_mode,'holding');
  assert.equal(event.experiment_version,'trips_v3');
  assert.equal(event.analytics_schema_version,'4');
  assert.equal(h.events().some(e=>e.event.startsWith('trips_pattern_')),false);
  assert.equal(h.root.children.at(-1).attributes['aria-label'],'선택형 촬영 패턴 실험');
  const run=find(h.root,'촬영 패턴으로 찾아보기');run.click();run.click();
  assert.equal(h.albums.length,2,'experiment adds a separate preview, preserving original holding album');
  assert.equal(h.albums[0].photos.length,55);
  assert.equal(h.albums[1].photos.length,40);
  assert.equal(h.albums[1].label,'실험 · 촬영 패턴으로 추정');
  const stats=h.events().find(e=>e.event==='trips_pattern_stats');
  assert.equal(stats.daily_mean,6);assert.equal(stats.empty_selected_days,7);
  assert.equal(stats.pattern_status,'ready');
  h.albums[1].onExpand();h.albums[1].onExpand();
  h.review.open();
  assert.equal(h.albums.length,2);assert.equal(find(h.root,'촬영 패턴으로 찾아보기'),undefined);
  for(const name of ['trips_pattern_start','trips_pattern_stats','trips_pattern_result','trips_pattern_album_open'])
    assert.equal(h.events().filter(e=>e.event===name).length,1);
  assert.equal(h.events().filter(e=>e.event==='trips_results_view').length,1);
  assert.doesNotMatch(JSON.stringify(h.events()),/PRIVATE|2026-09|latitude|longitude|candidate-/);
});
test('insufficient or undated input stays visible in one holding album, not tiny date groups',()=>{
  for(const records of [[{index:0,name:'unknown.jpg',date:null,gps:null}],
    [photo(0,1,null),photo(1,3,null),photo(2,5,null)]]){
    const h=setup(records);h.review.open();
    assert.equal(title(h.root),'분류 보류 사진을 한곳에 모았어요');
    assert.equal(h.albums.length,1);assert.equal(h.albums[0].title,'분류 보류');
    assert.equal(h.albums[0].photos.length,records.length);
    find(h.root,'촬영 패턴으로 찾아보기').click();
    assert.equal(h.albums.length,1);
    assert.equal(h.events().find(e=>e.event==='trips_pattern_result').candidate_count,0);
  }
});
test('holding album includes ambiguous GPS-free photos alongside missing dates',()=>{
  const records=[photo(0,1),photo(1,3,null),photo(2,5,null),{index:3,name:'unknown',date:null,gps:null}];
  const h=setup(records);h.review.open();
  assert.equal(h.albums.length,2);assert.equal(h.albums[1].photos.length,3);
  assert.equal(h.albums[1].title,'분류 보류');
});
