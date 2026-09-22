import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { createAnalytics, clientEnvironment, metadataSummary, safePageLocation, CONSENT_KEY } from '../src/analytics.js';
import { publicAnalyticsConfig, analyticsConfigSource } from '../scripts/analytics-config.mjs';

function harness({ href='https://map-mory.com/trips/', consent=null, ua='iPhone Safari', storageFails=false, config={} }={}) {
  const storage=new Map(consent?[[CONSENT_KEY,consent]]:[]), scripts=[];
  const win={ location:new URL(href), navigator:{userAgent:ua,maxTouchPoints:1}, localStorage:{
    getItem(key){if(storageFails)throw Error();return storage.get(key)??null;},
    setItem(key,value){if(storageFails)throw Error();storage.set(key,value);},
  }};
  const doc={ referrer:'https://example.com/private/person?email=secret@example.com',createElement:()=>({}),head:{append:script=>scripts.push(script)} };
  const analytics=createAnalytics({config:{measurementId:'G-TEST',...config},win,doc});
  return {analytics,win,scripts,storage,commands:()=>Array.from(win.dataLayer??[],args=>Array.from(args)),events:()=>Array.from(win.dataLayer??[],args=>Array.from(args)).filter(args=>args[0]==='event')};
}

test('consent is required before loading GA; refusal never sends even a denied ping',()=>{
  const h=harness();
  h.analytics.track('trips_picker_open',{picker_type:'photos'});
  h.analytics.setConsent('denied');
  assert.equal(h.scripts.length,0);assert.equal(h.commands().length,0);
  h.analytics.setConsent('granted');h.analytics.setConsent('granted');
  assert.equal(h.scripts.length,1);
  assert.equal(h.events().length,1);assert.equal(h.events()[0][1],'page_view');
  assert.deepEqual(h.commands()[0],['consent','default',{analytics_storage:'denied',ad_storage:'denied',ad_user_data:'denied',ad_personalization:'denied'}]);
  const config=h.commands().find(args=>args[0]==='config')[2];
  assert.equal(config.send_page_view,false);assert.equal(config.allow_google_signals,false);assert.equal(config.allow_ad_personalization_signals,false);
  assert.equal(config.page_referrer,'https://example.com');
  h.analytics.track('trips_picker_open',{picker_type:'files'});
  assert.equal(h.events().length,2,'no replay of events before consent');
  h.analytics.setConsent('denied');
  assert.equal(h.win['ga-disable-G-TEST'],true);
  assert.equal(h.analytics.track('trips_survey_click'),false);
  assert.equal(h.events().length,2);
});

test('local/LAN/preview tracking off even with stored consent unless explicitly enabled',()=>{
  for(const href of ['http://127.0.0.1:5174/trips/','http://192.168.1.4/trips/','https://preview.example/trips/']){
    const h=harness({href,consent:'granted'});assert.equal(h.analytics.enabled,false);assert.equal(h.scripts.length,0);
  }
  const h=harness({href:'http://localhost:5174/trips/',consent:'granted',config:{captureLocal:true,debug:true}});
  assert.equal(h.events()[0][2].traffic_type,'internal');assert.equal(h.scripts.length,1);
  assert.equal(h.commands().find(args=>args[0]==='config')[2].debug_mode,true);
});

test('unavailable storage does not break analytics consent or photo usage',()=>{
  const h=harness({storageFails:true});
  assert.doesNotThrow(()=>h.analytics.setConsent('granted'));
  assert.equal(h.analytics.track('trips_picker_open',{picker_type:'photos'}),true);
});

test('recruitment is not QA; internal query honored without forwarding arbitrary query text',()=>{
  const h=harness({href:'https://map-mory.com/trips/?utm_source=wooteco&utm_medium=community&utm_campaign=trips_test&email=secret#private',consent:'granted'});
  assert.equal(h.events()[0][2].traffic_type,'external');
  assert.equal(h.events()[0][2].page_location,'https://map-mory.com/trips/?utm_source=wooteco&utm_medium=community&utm_campaign=trips_test');
  assert.equal(harness({href:'https://map-mory.com/trips/?internal=1',consent:'granted'}).events()[0][2].traffic_type,'internal');
  assert.equal(safePageLocation('https://map-mory.com/trips/?utm_source=secret@example.com&utm_content=my-home&ga_debug=1'),'https://map-mory.com/trips/');
});

test('strict event/property/value allowlists prevent file/metadata/raw error leakage',()=>{
  const h=harness({consent:'granted'});
  h.analytics.setContext({photo_count:500,photo_bucket:'500_plus',gps_coverage:'some',date_coverage:'all',evaluation_group:'eligible',filename:'private.jpg',gps:{latitude:37},picker_type:'secret'});
  h.analytics.track('trips_processing_complete',{processing_seconds:12,gps_count:2,dated_count:500,usable_count:2,read_error_count:0,geo_data_available:true,filename:'private.jpg',date:'2026-09-01',latitude:37,error:'secret'});
  const event=h.events().at(-1)[2];
  assert.equal(event.gps_count,2);assert.equal(event.photo_count,500);assert.equal(event.gps_coverage,'some');
  assert.equal(event.filename,undefined);assert.equal(event.picker_type,undefined);
  assert.doesNotMatch(JSON.stringify(h.commands()),/private\.jpg|2026-09-01|latitude|secret/);
  assert.equal(h.analytics.track('user_supplied_event'),false);
  h.analytics.track('trips_album_open',{album_kind:'my-home',album_photo_count:Infinity});
  assert.equal(h.events().at(-1)[2].album_kind,undefined);assert.equal(h.events().at(-1)[2].album_photo_count,undefined);
});

test('dedup is per accepted selection, not render; new selections never inherit old metadata',()=>{
  const h=harness({consent:'granted'});
  h.analytics.resetFlow({photo_count:500,gps_coverage:'none'});
  assert.equal(h.analytics.track('trips_album_open',{album_kind:'trip'},'trip_album_open'),true);
  assert.equal(h.analytics.track('trips_album_open',{album_kind:'trip'},'trip_album_open'),false);
  h.analytics.track('trips_selection_received',{picker_type:'files',photo_count:10});
  assert.equal(h.events().at(-1)[2].gps_coverage,undefined);
  h.analytics.resetFlow({photo_count:10});
  assert.equal(h.analytics.track('trips_album_open',{album_kind:'trip'},'trip_album_open'),true);
});

test('Android non-Kakao is measured diagnostically but excluded regardless of metadata success',()=>{
  const chrome=clientEnvironment('Mozilla Android Chrome Safari');
  const kakao=clientEnvironment('Mozilla Android Chrome Safari KAKAOTALK 26');
  const ios=clientEnvironment('iPhone Safari');
  assert.equal(chrome.environment_eligible,false);assert.equal(kakao.environment_eligible,true);
  assert.equal(clientEnvironment('Macintosh Safari',5).platform,'ios');
  const records=[{gps:{latitude:37,longitude:127},date:{day:'private'}},{date:{day:'private'}},{}];
  assert.deepEqual(metadataSummary(records,ios),{gps_count:1,dated_count:2,usable_count:1,read_error_count:0,gps_coverage:'some',date_coverage:'some',evaluation_group:'eligible'});
  assert.equal(metadataSummary(records,chrome).evaluation_group,'android_non_kakao');
  assert.equal(metadataSummary(records,kakao).evaluation_group,'eligible');
  const h=harness({ua:'Android Chrome',consent:'granted'});
  assert.equal(h.events()[0][2].environment_group,'android_non_kakao','exclusion available even on entry');
});

test('date-only, GPS-only, fully missing and full metadata are distinguished',()=>{
  const env=clientEnvironment('iPhone');
  assert.equal(metadataSummary([{date:{}}],env).gps_coverage,'none');
  assert.equal(metadataSummary([{date:{}}],env).date_coverage,'all');
  assert.equal(metadataSummary([{gps:{}}],env).date_coverage,'none');
  assert.equal(metadataSummary([{readError:true}],env).read_error_count,1);
  assert.equal(metadataSummary([{},{}],env).evaluation_group,'no_usable_metadata');
  assert.equal(metadataSummary([{gps:{},date:{}}],env).gps_coverage,'all');
  assert.equal(metadataSummary([{gps:{}},{date:{}}],env).evaluation_group,'no_usable_metadata','must be paired on the same photo');
});

test('build exposes only allowlisted public configuration; disabled when missing',()=>{
  const env={VITE_GA_MEASUREMENT_ID:' G-ABC123 ',VITE_GA_CAPTURE_LOCAL:'true',VITE_GA_DEBUG:'false',AWS_SECRET_ACCESS_KEY:'do-not-publish'};
  assert.deepEqual(publicAnalyticsConfig(env),{measurementId:'G-ABC123',captureLocal:true,debug:false});
  assert.doesNotMatch(analyticsConfigSource(env),/SECRET|do-not-publish/);
  assert.equal(publicAnalyticsConfig({}).measurementId,'');
  assert.throws(()=>publicAnalyticsConfig({VITE_GA_MEASUREMENT_ID:'bad<script>'}));
});

test('analytics preserves file inputs and the photo-only CSP protections',async()=>{
  const html=await readFile(new URL('../src/index.html',import.meta.url),'utf8');
  assert.match(html,/id="photo-input" type="file" accept="\.jpg,\.jpeg,\.heic,\.heif,\.png,\.tif,\.tiff,\.avif,\.webp" multiple hidden/);
  assert.match(html,/id="original-input" type="file" multiple hidden/);
  assert.match(html,/worker-src 'self'/);assert.match(html,/object-src 'none'/);
  assert.doesNotMatch(html,/unsafe-inline|unsafe-eval/);
});
