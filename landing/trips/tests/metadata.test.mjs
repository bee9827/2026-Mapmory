import test from 'node:test';
import assert from 'node:assert/strict';
import {readMetadata} from '../src/metadata.js';
import {captureDate,coordinates} from '../src/organize.js';
import {groupByDate} from '../src/dates.js';
import {readFileSync} from 'node:fs';
const file=new File([new Uint8Array([1,2,3])],'IMG.HEIC',{type:''});
test('recap options and File-first fallback are retained',async()=>{
  let seen;
  const result=await readMetadata(file,{parse:async(_file,options)=>{seen=options;return {DateTimeOriginal:new Date(2026,8,21,0,3,4)};},gps:async()=>{throw Error();}},async()=>({load:async(input,options)=>{assert.equal(input,file);assert.equal(options.length,'auto');return {gps:{Latitude:-37,Longitude:127}};}}));
  assert.deepEqual(seen,{gps:true,tiff:true,xmp:true,exif:true,ifd0:true,interop:false,makerNote:false,userComment:false});
  assert.equal(captureDate(result.metadata).day,'2026-09-21');assert.equal(result.metadata.latitude,-37);
});
test('actual synthetic TIFF works through the recap reader',async()=>{
  const data=readFileSync(new URL('./synthetic-date-gps.tiff',import.meta.url));
  const result=await readMetadata(new File([data],'test.tiff'));
  assert.equal(captureDate(result.metadata).day,'2026-09-21');assert.ok(Math.abs(result.metadata.latitude-37.58)<1e-8);
});
test('file failure retries bytes without converting original',async()=>{
  let calls=0;
  const {metadata,readError}=await readMetadata(file,{parse:async(input)=>{calls++;if(input===file)throw Error();assert.ok(input instanceof Uint8Array);return {DateTimeOriginal:'2026:09:21 00:03:04',latitude:37,longitude:127};}},()=>{throw Error('not needed');});
  assert.equal(calls,2);assert.equal(readError,false);assert.equal(captureDate(metadata).day,'2026-09-21');
});
test('secondary reader recovers date and GPS when primary returns nothing',async()=>{
  const {metadata,readError}=await readMetadata(file,{parse:async()=>({}),gps:async()=>({})},async()=>({load:async()=>({exif:{DateTimeOriginal:{description:'2026:09:21 00:03:04'}},gps:{Latitude:37,Longitude:127}})}));
  assert.equal(readError,false);assert.equal(captureDate(metadata).day,'2026-09-21');assert.deepEqual(coordinates(metadata),{latitude:37,longitude:127});
});
test('both parser failures are explicit, with no invented date or GPS',async()=>{
  const result=await readMetadata(file,{parse:async()=>{throw Error();},gps:async()=>{throw Error();}},async()=>{throw Error();});
  assert.equal(result.readError,true);assert.equal(captureDate(result.metadata),null);assert.equal(coordinates(result.metadata),null);
});
test('dates span locations and sort photos chronologically, unknown date last',()=>{
  const records=[{index:0,date:null},{index:1,date:{day:'2026-09-22',capturedAt:'2026-09-22T12:00:00'}},{index:2,date:{day:'2026-09-21',capturedAt:'2026-09-21T10:00:00'}},{index:3,date:{day:'2026-09-21',capturedAt:'2026-09-21T09:00:00'}}];
  const result=groupByDate(records);assert.deepEqual(result.map(r=>r.day),['2026-09-21','2026-09-22','촬영일 정보 없음']);assert.deepEqual(result[0].photos.map(r=>r.index),[3,2]);assert.equal(records[0].index,0);
});
