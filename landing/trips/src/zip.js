import { Zip, ZipPassThrough, strToU8 } from './vendor/fflate.js';
import { manifestFor } from './organize.js';

export async function makeArchive(entries, onProgress = () => {}) {
  const parts = [];
  let failure;
  let complete = false;
  const zip = new Zip((error, data, final) => {
    if (error) { failure = error; return; }
    parts.push(data);
    complete = final;
  });
  const total = entries.reduce((sum, entry) => sum + entry.file.size, 0);
  let bytes = 0;
  try {
    for (const { file, record } of entries) {
      const stream = new ZipPassThrough(`정리한 사진/${record.path}`);
      const modified = new Date(file.lastModified);
      if (Number.isFinite(+modified) && modified.getFullYear() >= 1980 && modified.getFullYear() <= 2099) stream.mtime = modified;
      zip.add(stream);
      const reader = file.stream().getReader();
      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) { stream.push(new Uint8Array(0), true); break; }
          stream.push(value);
          if (failure) throw failure;
          bytes += value.length;
          onProgress({ bytes, total, percent: total ? Math.min(99, Math.floor(bytes / total * 100)) : 0 });
        }
      } finally { reader.releaseLock(); }
    }
    const manifest = new ZipPassThrough('정리한 사진/분류 내역.json');
    zip.add(manifest);
    manifest.push(strToU8(JSON.stringify(manifestFor(entries.map(e => e.record)), null, 2)), true);
    zip.end();
    if (failure) throw failure;
    if (!complete) throw new Error('ZIP 생성이 끝나지 않았습니다.');
    const blob = new Blob(parts, { type: 'application/zip' });
    onProgress({ bytes: total, total, percent: 100 });
    return blob;
  } catch (error) {
    zip.terminate();
    throw error;
  }
}
