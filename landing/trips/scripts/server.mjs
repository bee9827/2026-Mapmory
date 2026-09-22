import {createServer} from 'node:http';
import {readFile} from 'node:fs/promises';
import {resolve,relative,isAbsolute,extname} from 'node:path';

const mime={'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.json':'application/json; charset=utf-8','.txt':'text/plain; charset=utf-8'};
export function createTripsServer(root) {
  const directory=resolve(root);
  return createServer(async(req,res)=>{
    const url=new URL(req.url,'http://localhost');
    if(!['GET','HEAD'].includes(req.method)) {res.writeHead(405,{'Allow':'GET, HEAD'}).end();return;}
    if(url.pathname==='/trips') {res.writeHead(308,{'Location':'/trips/'+url.search}).end();return;}
    if(!url.pathname.startsWith('/trips/')) {res.writeHead(404).end('Not found');return;}
    try {
      const name=decodeURIComponent(url.pathname.slice('/trips/'.length))||'index.html';
      const file=resolve(directory,name);
      const within=relative(directory,file);
      if(!within||within.startsWith('..')||isAbsolute(within)) {res.writeHead(404).end('Not found');return;}
      const content=await readFile(file);
      res.writeHead(200,{'Content-Type':mime[extname(file)]||'application/octet-stream','Cache-Control':'no-cache','X-Content-Type-Options':'nosniff'});
      res.end(req.method==='HEAD'?undefined:content);
    } catch {res.writeHead(404).end('Not found');}
  });
}
