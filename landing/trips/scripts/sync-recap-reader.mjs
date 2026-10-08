import {readFileSync,writeFileSync} from 'node:fs';
// Mechanical extraction keeps the proven /recap read sequence and options intact.
const recap=readFileSync(new URL('../../travel-map-campaign/src/journeyData.js',import.meta.url),'utf8');
const helpers=recap.slice(recap.indexOf('function firstDefined('),recap.indexOf('\nconst knownPlaces'));
const reads=recap.slice(recap.indexOf('    let metadata = null;'),recap.indexOf('    const fallbackHasMetadata'));
const coords=recap.slice(recap.indexOf('    const lat = normalizeGpsCoordinate(',recap.indexOf('export async function analyzePhotoFiles')),recap.indexOf('    const extension =',recap.indexOf('export async function analyzePhotoFiles')));
const preamble=`import * as defaultParser from './vendor/exifr.js';
let readerPromise;
async function defaultReader() {
  readerPromise ??= import('./vendor/exifreader.js').then(module => module.default ?? globalThis.ExifReader);
  return readerPromise;
}
`;
const adapter=`
    const result = {...(metadata ?? {})};
    for (const key of ['DateTimeOriginal','CreateDate','DateTimeDigitized','DateCreated']) {
      const value = result[key] ?? fallbackMetadata?.exif?.[key]?.description ?? fallbackMetadata?.xmp?.[key]?.description;
      // exifr revives camera timestamps in local time. Do not shift midnight using ISO UTC.
      if (value instanceof Date && Number.isFinite(value.getTime())) {
        const pad=n=>String(n).padStart(2,'0');
        result[key]=value.getFullYear()+'-'+pad(value.getMonth()+1)+'-'+pad(value.getDate())+'T'+pad(value.getHours())+':'+pad(value.getMinutes())+':'+pad(value.getSeconds());
      }
      else if (value !== undefined) result[key]=value;
    }
    if(hasGps){result.latitude=lat;result.longitude=lng;}
    return {metadata:result,readError:Boolean(metadataError && fallbackError)};
}
`;
writeFileSync(new URL('../src/metadata.js',import.meta.url),preamble+helpers+'\n// Generated from /recap; only the date/output adapter differs.\nexport async function readMetadata(file, exifr=defaultParser, getExifReader=defaultReader) {\n'+reads+coords+adapter);
const batches=readFileSync(new URL('../../travel-map-campaign/src/photoProcessing.js',import.meta.url),'utf8');
writeFileSync(new URL('../src/photoProcessing.js',import.meta.url),batches);
