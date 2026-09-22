import test from 'node:test';
import assert from 'node:assert/strict';
import {once} from 'node:events';
import {fileURLToPath} from 'node:url';
import {createTripsServer} from '../scripts/server.mjs';

test('Trips redirect retains the query and nested assets stay scoped',async t=>{
  const server=createTripsServer(fileURLToPath(new URL('../src/',import.meta.url)));
  server.listen(0,'127.0.0.1');await once(server,'listening');
  t.after(()=>new Promise(resolve=>{server.closeAllConnections();server.close(resolve);}));
  const base='http://127.0.0.1:'+server.address().port;
  for(const query of ['', '?utm_source=kakao&name=%EC%97%AC%ED%96%89']){
    const response=await fetch(base+'/trips'+query,{redirect:'manual'});
    assert.equal(response.status,308);assert.equal(response.headers.get('location'),'/trips/'+query);
  }
  const shell=await fetch(base+'/trips/');
  assert.equal(shell.status,200);assert.match(await shell.text(),/id="photo-input"/);
  for(const asset of ['app.js','metadata.js','albums.js','worker.js','style.css','data/cities.json','vendor/exifreader.js']){
    const response=await fetch(base+'/trips/'+asset);assert.equal(response.status,200,asset);
    assert.notEqual(response.headers.get('content-type'),'text/html; charset=utf-8',asset);
  }
  for(const path of ['/','/recap/','/app.js','/trips/missing.js','/trips/%2e%2e%2fpackage.json']){
    assert.equal((await fetch(base+path)).status,404,path);
  }
  assert.equal((await fetch(base+'/trips/',{method:'POST'})).status,405);
});
