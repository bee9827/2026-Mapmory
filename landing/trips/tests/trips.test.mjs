import test from 'node:test';
import assert from 'node:assert/strict';
import {groupTrips,homeOptions} from '../src/trips.js';
const home={latitude:37.56,longitude:126.97};
const away={latitude:33.4,longitude:126.5};
const photo=(index,time,gps=away)=>({index,name:`${index}.jpg`,gps,date:time?{day:time.slice(0,10),capturedAt:time}:null});
test('home return separates trips even on same date',()=>{
 const data=[photo(0,'2026-09-01T09:00:00'),photo(1,'2026-09-01T12:00:00',home),photo(2,'2026-09-01T15:00:00')];
 const result=groupTrips(data,home);assert.equal(result.trips.length,2);assert.deepEqual(result.other.map(p=>p.index),[1]);
});
test('multiple cities stay together and over 72h splits visits',()=>{
 const result=groupTrips([photo(0,'2026-09-01T09:00:00'),photo(1,'2026-09-02T10:00:00',{latitude:35.17,longitude:129.07}),photo(2,'2026-09-06T10:00:01')],home);
 assert.deepEqual(result.trips.map(t=>t.photos.length),[2,1]);
});
test('missing metadata is preserved and never guessed into a trip',()=>{
 const data=[photo(0,null),photo(1,'2026-09-01T09:00:00',null),photo(2,'2026-09-01T09:00:00',home)];
 const result=groupTrips(data,home);assert.equal(result.trips.length,0);assert.equal(result.other.length,3);
});
test('input order is preserved and home choice is explicit',()=>{
 const data=[photo(0,'2026-09-02T09:00:00'),photo(1,'2026-09-01T09:00:00')];
 assert.deepEqual(groupTrips(data,home).trips[0].photos.map(p=>p.index),[1,0]);assert.equal(data[0].index,0);
 assert.throws(()=>groupTrips(data,null),/생활 지역/);assert.equal(homeOptions(data)[0].count,2);
});
