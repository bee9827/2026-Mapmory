// Local-only UI QA. Uses the real app with public test config and a local tag stub.
// No Google request is made; never package this test server or call this production evidence.
import {readFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import {createTripsServer} from '../scripts/server.mjs';
const root=new URL('../src/',import.meta.url);
const resultPreview=process.argv.includes('--results');
const server=createTripsServer(fileURLToPath(root));
const serve=server.listeners('request')[0];server.removeAllListeners('request');
server.on('request',async(req,res)=>{
  let content;
  if(resultPreview && req.url==='/trips/app.js'){
    content=(await readFile(new URL('app.js',root),'utf8'))+`
      // Synthetic layout fixture for this local-only server. No user photos or GA transmission.
      state.records=Array.from({length:500},(_,index)=>({index,name:'test-'+index+'.jpg',size:1000,
        city:'테스트 지역 '+(index%20+1),countryCode:'KR',folder:'테스트 지역 '+(index%20+1),
        path:'테스트 지역 '+(index%20+1)+'/test-'+index+'.jpg',date:null,gps:null}));
      state.phase='complete';state.oversized=2;state.archives=[];
      renderResults();
    `;
  }
  if(req.url==='/trips/analytics-config.js')content="export const analyticsConfig={measurementId:'G-LOCALQA',captureLocal:true,debug:true};";
  if(req.url==='/trips/analytics.js'){
    content=(await readFile(new URL('analytics.js',root),'utf8')).replace('https://www.googletagmanager.com/gtag/js?id=${config.measurementId}','/trips/ga-stub.js');
  }
  if(req.url==='/trips/ga-stub.js')content=`
    const log=document.createElement('pre');log.id='qa-events';log.setAttribute('aria-label','로컬 QA 이벤트');document.body.append(log);
    const show=()=>{log.textContent='LOCAL QA ONLY — Google 전송 없음\\n'+JSON.stringify(Array.from(window.dataLayer,args=>Array.from(args)),null,2);};
    const push=window.dataLayer.push.bind(window.dataLayer);window.dataLayer.push=(...items)=>{const length=push(...items);show();return length;};show();`;
  if(content!==undefined){res.writeHead(200,{'Content-Type':'text/javascript; charset=utf-8','Cache-Control':'no-store'}).end(content);return;}
  serve(req,res);
});
server.listen(5176,'127.0.0.1',()=>console.log('Local-only stubbed GA preview: http://127.0.0.1:5176/trips/'));
