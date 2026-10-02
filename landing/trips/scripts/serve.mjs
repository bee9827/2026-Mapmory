import {fileURLToPath} from 'node:url';
import {createTripsServer} from './server.mjs';
const host=process.argv.includes('--lan')?'0.0.0.0':'127.0.0.1';
const position=process.argv.indexOf('--port');
const port=position<0?5174:Number(process.argv[position+1]);
if(!Number.isInteger(port)||port<1||port>65535)throw new Error('Invalid port');
const source=fileURLToPath(new URL('../src/',import.meta.url));
createTripsServer(source).listen(port,host,()=>console.log('Trips: http://'+host+':'+port+'/trips/'));
