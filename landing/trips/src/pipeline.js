import {readMetadata} from './metadata.js';
import {parsePhotoBatches} from './photoProcessing.js';
import {classifyPhoto,createLocationResolver,finishRecords} from './organize.js';
export async function organizePhotos(files,{cancelled=()=>false,onProgress=()=>{}}={}) {
  const check=()=>{if(cancelled())throw new Error('cancelled');};
  const load=async path=>{try {const response=await fetch(new URL(path,import.meta.url));return response.ok?await response.json():undefined;}catch{return undefined;}};
  const [boundaries,cities]=await Promise.all([load('./data/regions.json'),load('./data/cities.json')]);
  check();const resolve=createLocationResolver(boundaries,cities);let done=0;
  const records=await parsePhotoBatches(files,async(file,index)=>{
    check();const {metadata,readError}=await readMetadata(file);check();
    const record=classifyPhoto(file,index,metadata,resolve,readError);
    onProgress({phase:'classify',completed:++done,total:files.length,percent:Math.floor(done/files.length*100)});
    return record;
  });
  return {records:finishRecords(records),locationDataUnavailable:!boundaries||!cities};
}
