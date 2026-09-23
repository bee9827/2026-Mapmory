import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
import {groupByDate} from '../src/dates.js';

const source=readFileSync(new URL('../src/app.js',import.meta.url),'utf8');
const render=source.slice(source.indexOf('function renderResults()'),source.indexOf('\nfunction renderTripSetup()'));
function node(tag,className='',text=''){
  return {tag,className,text,children:[],append(...children){this.children.push(...children);},replaceChildren(){this.children=[];},setAttribute(){},addEventListener(){}};
}
test('optional folder view keeps a prominent return to candidate review above long lists',()=>{
  const app=node('main');let selected=false;
  const records=Array.from({length:500},(_,index)=>({index,size:10,city:`test-${index}`,gps:null,date:null,readError:true}));
  const context={app,state:{records,skipped:2,oversized:3,geoUnavailable:true},el:node,icon:node,n:String,formatBytes:String,
    clearAlbums(){},document:{createTextNode:text=>node('text','',text),createElement:node},
    button:(text,action,className)=>({...node('button',className,text),action}),
    renderTripSetup(){selected=true;},treeContents:()=>node('div','long-location-list'),buildTree(){},groupByDate,
    renderDownloads(){},choosePhotos(){},privacyNote:()=>node('p')};
  runInNewContext(`${render}\nrenderResults();`,context);
  assert.deepEqual(app.children.slice(0,4).map(n=>n.className),['result-header','trip-next-step','summary-grid','notice']);
  const action=app.children[1].children[0];
  assert.equal(action.text,'여행 묶어서 보기');assert.match(action.className,/button-trip/);
  action.action();assert.equal(selected,true);
  const locationIndex=app.children.findIndex(n=>n.className==='location-section');
  const dateIndex=app.children.findIndex(n=>n.className==='tree-card date-card');
  assert.ok(locationIndex>1);assert.ok(dateIndex>locationIndex);
  assert.equal(app.children[locationIndex].children.some(n=>n.className==='trip-next-step'),false,'no duplicate CTA under folders');
});

test('direct candidate view retains metadata counts and oversized-file exclusions',()=>{
  const notice=source.slice(source.indexOf('function tripImportNotice()'),source.indexOf('function renderTripSetup()'));
  const context={state:{records:[{gps:{},date:{}},{gps:null,date:{},readError:true}],oversized:3,skipped:1,geoUnavailable:true},el:node,n:String};
  const result=runInNewContext(`${notice}\ntripImportNotice();`,context);
  const copy=result.children.map(n=>n.text).join(' ');
  assert.match(copy,/2장 읽음 · 촬영일 2장 · 위치 1장/);
  assert.match(copy,/50MB 초과 사진 3장은 제외/);
  assert.match(copy,/사진이 아닌 파일 1개/);
  assert.match(copy,/1장은 촬영 정보를 완전히 읽지 못했지만 원본은 보존/);
});
