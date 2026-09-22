import {cp,readFile,mkdir,rm} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
const source=new URL('../src/',import.meta.url);
const output=new URL('../dist/trips/',import.meta.url);
const html=await readFile(new URL('index.html',source),'utf8');
if(!html.includes('id="photo-input"')||!html.includes('src="./app.js"')) throw new Error('Missing Trips entry point');
// Fixed generated output only; never clean the source, project root or uploads.
await rm(output,{recursive:true,force:true});
await mkdir(output,{recursive:true});
await cp(source,output,{recursive:true});
console.log('Trips static build: '+fileURLToPath(output));
