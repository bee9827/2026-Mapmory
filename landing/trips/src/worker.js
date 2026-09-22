import { readMetadata } from './metadata.js';
import { classifyPhoto, createLocationResolver, finishRecords } from './organize.js';
import { makeArchive } from './zip.js';

self.addEventListener('message', async ({ data }) => {
  try {
    if (data.type === 'organize') {
      self.postMessage({ type: 'progress', phase: 'prepare', completed: 0, total: data.files.length, percent: 0 });
      const loadData = async path => {
        try {
          const response = await fetch(new URL(path, import.meta.url), { signal: AbortSignal.timeout(20000) });
          if (!response.ok) return undefined;
          return await response.json();
        } catch { return undefined; }
      };
      const [boundaries, cities] = await Promise.all([loadData('./data/regions.json'), loadData('./data/cities.json')]);
      const resolveLocation = createLocationResolver(boundaries, cities);
      const records = [];
      for (let index = 0; index < data.files.length; index++) {
        const file = data.files[index];
        let metadata = {}, readError = false;
        try {
          ({metadata,readError} = await readMetadata(file));
        } catch { readError = true; }
        records.push(classifyPhoto(file, index, metadata, resolveLocation, readError));
        self.postMessage({ type: 'progress', phase: 'classify', completed: index + 1, total: data.files.length, percent: Math.floor((index + 1) / data.files.length * 100) });
      }
      self.postMessage({ type: 'complete', records: finishRecords(records), locationDataUnavailable: !boundaries || !cities });
    } else if (data.type === 'zip') {
      let lastPercent = -1;
      const blob = await makeArchive(data.entries, progress => {
        if (progress.percent !== lastPercent) {
          self.postMessage({ type: 'zip-progress', ...progress }); lastPercent = progress.percent;
        }
      });
      self.postMessage({ type: 'zip-ready', blob });
    }
  } catch (error) {
    self.postMessage({ type: 'error', message: error instanceof Error ? error.message : '처리 중 문제가 생겼습니다.' });
  }
});
