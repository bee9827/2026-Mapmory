import {cp,readFile,mkdir,rm,writeFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import {analyticsConfigSource} from './analytics-config.mjs';
const analyticsSource=analyticsConfigSource(process.env);
const source=new URL('../src/',import.meta.url);
const output=new URL('../dist/trips/',import.meta.url);
const html=await readFile(new URL('index.html',source),'utf8');
if(!html.includes('id="photo-input"')||!html.includes('src="./app.js"')) throw new Error('Missing Trips entry point');
// Fixed generated output only; never clean the source, project root or uploads.
await rm(output,{recursive:true,force:true});
await mkdir(output,{recursive:true});
await cp(source,output,{recursive:true});
await writeFile(new URL('analytics-config.js',output),analyticsSource);
console.log('Trips static build: '+fileURLToPath(output));
