import { makeArchive } from './zip.js';

self.addEventListener('message', async ({ data }) => {
  try {
    if (data.type === 'zip') {
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
