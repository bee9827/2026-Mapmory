// Synthetic, local-only UI fixtures. Never copied into the production build.
import {readFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import {createTripsServer} from '../scripts/server.mjs';
const root=new URL('../src/',import.meta.url);
const server=createTripsServer(fileURLToPath(root));
const serve=server.listeners('request')[0];server.removeAllListeners('request');
server.on('request',async(req,res)=>{
  if(req.url!=='/trips/app.js')return serve(req,res);
  const source=await readFile(new URL('app.js',root),'utf8');
  res.writeHead(200,{'Content-Type':'text/javascript; charset=utf-8','Cache-Control':'no-store'}).end(source+`
    const scenario=new URLSearchParams(location.search).get('case')||'mixed';
    const records=[];
    function add(day,hour,place){
      const index=records.length, stamp='2026-09-'+String(day).padStart(2,'0');
      records.push({index,name:'qa-'+index+'.jpg',size:1000,date:{day:stamp,capturedAt:stamp+'T'+String(hour).padStart(2,'0')+':00:00'},
        gps:place===1?{latitude:37.55,longitude:126.97}:place===2?{latitude:33.45,longitude:126.57}:null,
        city:place===1?'서울':place===2?'제주':null,folder:'로컬 QA',path:'로컬 QA/qa-'+index+'.jpg'});
    }
    if(scenario==='pattern'||scenario==='insufficient'){
      for(const day of [1,3,5,7,9,11,13]){add(day,10);add(day,12);}
      for(const day of [15,16])for(let i=0;i<20;i++)add(day,10);
      if(scenario==='insufficient')records.splice(6);
    }else{
      add(1,10,1);add(1,11);add(1,12,1);add(3,10,2);add(3,11,2);add(4,10,2);add(10,10,2);add(10,12,2);
      if(scenario==='location')records[1].gps={latitude:37.55,longitude:126.97};
    }
    state.records=records;
    state.files=records.map((p,i)=>new File(['<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="'+['#b3decf','#aecde2','#eed9a9'][i%3]+'"/><text x="30" y="160" font-size="35">QA '+i+'</text></svg>'],p.name,{type:'image/svg+xml'}));
    state.phase='complete';state.archives=[];renderTripSetup();
    const banner=document.createElement('p');banner.textContent='LOCAL QA · 합성 사진 · 실제 업로드 결과가 아님';
    document.body.prepend(banner);
  `);
});
server.listen(5178,'127.0.0.1',()=>console.log('Candidate QA: http://127.0.0.1:5178/trips/?case=mixed'));
