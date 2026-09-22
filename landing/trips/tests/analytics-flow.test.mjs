import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
import {createAnalytics,metadataSummary,CONSENT_KEY} from '../src/analytics.js';
import {validateTripSelection,excludeOversizedPhotos} from '../src/selection.js';
import {homeOptions,groupTrips} from '../src/trips.js';

const source=readFileSync(new URL('../src/app.js',import.meta.url),'utf8');
const start=source.slice(source.indexOf('async function startOrganization('),source.indexOf('\nfunction buildTree('));
const grouping=source.slice(source.indexOf('function renderTripSetup('),source.indexOf('\nfunction createSurvey('));
function setup(ua='iPhone Safari'){
  const win={location:new URL('https://map-mory.com/trips/'),navigator:{userAgent:ua},localStorage:{getItem:key=>key===CONSENT_KEY?'granted':null,setItem(){}}};
  const analytics=createAnalytics({config:{measurementId:'G-TEST'},win,doc:{referrer:'',createElement:()=>({}),head:{append(){}}}});
  const events=()=>win.dataLayer.filter(args=>args[0]==='event').map(args=>({name:args[1],...args[2]}));
  const context={analytics,metadataSummary,performance,processingStarted:0,processingSeconds:()=>3,
    state:{records:[]},readGeneration:0,isPhoto:()=>true,validateTripSelection,excludeOversizedPhotos,n:String,
    reset(){context.readGeneration++;},renderProgress(){},progressUpdate(){},renderResults(){},focusHeading(){},announce(){},
    planArchives:()=>[],renderStart(){},showResultError(){},
  };
  return {context,events};
}
test('real selection handler emits start/completion with GPS missing and Android environment exclusion',async()=>{
  const h=setup('Android Chrome Safari');
  h.context.files=Array.from({length:500},()=>({size:1}));
  h.context.organizePhotos=async files=>({records:files.map(()=>({date:{day:'private'}})),locationDataUnavailable:false});
  await runInNewContext(`${start}\nstartOrganization(files,'files');`,h.context);
  assert.deepEqual(h.events().map(e=>e.name),['page_view','trips_selection_received','trips_processing_start','trips_processing_complete']);
  const completed=h.events().at(-1);
  assert.equal(completed.gps_coverage,'none');assert.equal(completed.date_coverage,'all');
  assert.equal(completed.evaluation_group,'android_non_kakao');assert.equal(completed.photo_bucket,'500_plus');
  assert.equal(completed.picker_type,'files');assert.equal(completed.processing_seconds,3);
  assert.equal(h.context.state.phase,'complete');
});
test('rejected oversized-only selection does not emit processing or wipe previous results',async()=>{
  const h=setup(); const previous=[{index:0}];h.context.state.records=previous;
  h.context.files=[{size:51*1024*1024}];
  await runInNewContext(`${start}\nstartOrganization(files,'photos');`,h.context);
  assert.equal(h.events().at(-1).reason,'all_oversized');
  assert.equal(h.events().at(-1).oversized_count,1);assert.equal(h.context.state.records,previous);
  assert.equal(h.events().some(e=>e.name==='trips_processing_start'),false);
});
test('current processing failure is explicit but raw exception text is never sent',async()=>{
  const h=setup();h.context.files=[{size:1}];
  h.context.organizePhotos=async()=>{throw new Error('private filename and GPS');};
  await runInNewContext(`${start}\nstartOrganization(files,'photos');`,h.context);
  assert.equal(h.events().at(-1).name,'trips_processing_failed');
  assert.doesNotMatch(JSON.stringify(h.events()),/private filename/);
});
test('late completion/error after cancellation cannot produce completion or failure',async()=>{
  for(const shouldThrow of [false,true]){
    const h=setup();h.context.files=[{size:1}];
    h.context.organizePhotos=async()=>{h.context.readGeneration++;if(shouldThrow)throw Error();return {records:[{}]};};
    await runInNewContext(`${start}\nstartOrganization(files,'photos');`,h.context);
    assert.equal(h.events().at(-1).name,'trips_processing_start');
  }
});
test('actual grouping handlers record screen commit and first candidate expansion, without place/date labels',()=>{
  const h=setup(), albums=[];
  const node=()=>({append(){},replaceChildren(){}});
  h.context.analytics.resetFlow({photo_count:2,evaluation_group:'eligible',gps_coverage:'all',date_coverage:'all'});
  h.context.state.records=[
    {index:0,city:'PRIVATE HOME',gps:{latitude:37,longitude:127},date:{capturedAt:'2026-09-01T12:00:00'}},
    {index:1,city:'PRIVATE TRIP',gps:{latitude:33,longitude:126},date:{capturedAt:'2026-09-03T12:00:00'}},
  ];
  Object.assign(h.context,{el:node,app:node(),clearAlbums(){},homeOptions,groupTrips,button:node,createSurvey:node,privacyNote:node,
    photoAlbum:options=>{albums.push(options);return node();},selectedHome:null});
  runInNewContext(`${grouping}\nrenderTripSetup();renderTripSetup();`,h.context);
  assert.equal(h.events().filter(e=>e.name==='trips_grouping_start').length,1);
  albums[0].onSelect();
  assert.equal(h.events().at(-1).name,'trips_results_view');assert.equal(h.events().at(-1).candidate_count,1);
  const trip=albums.find(a=>a.label==='날짜와 위치로 찾은 여행 후보');
  trip.onExpand();trip.onExpand();
  assert.equal(h.events().filter(e=>e.name==='trips_album_open'&&e.album_kind==='trip').length,1);
  assert.doesNotMatch(JSON.stringify(h.events()),/PRIVATE|2026-09|latitude|longitude/);
});
