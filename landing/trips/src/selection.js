// Trips tests large libraries; Recap's 200-photo / 500MB video limits do not apply.
// Only three files are read concurrently; File handles are retained, not all bytes.
export const TRIP_SELECTION_LIMITS = Object.freeze({count: 1000, fileBytes: 50 * 1024 * 1024});

export function excludeOversizedPhotos(files) {
  const photos = [], oversized = [];
  for (const file of files) {
    (file.size > TRIP_SELECTION_LIMITS.fileBytes ? oversized : photos).push(file);
  }
  return {photos, oversized};
}

export function validateTripSelection(files) {
  if (files.length > TRIP_SELECTION_LIMITS.count) {
    throw new Error('사진은 한 번에 최대 1,000장까지 선택해주세요. 나누어 다시 선택해 주세요.');
  }
  if (files.some(file => !Number.isFinite(file.size) || file.size < 0 || file.size > TRIP_SELECTION_LIMITS.fileBytes)) {
    throw new Error('사진 한 장의 크기는 50MB 이하여야 해요. 큰 파일을 제외하고 다시 선택해 주세요.');
  }
}
