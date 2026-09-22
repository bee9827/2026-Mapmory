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
test('travel CTA precedes summary, all warnings and long location lists, and keeps home selection action',()=>{
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
