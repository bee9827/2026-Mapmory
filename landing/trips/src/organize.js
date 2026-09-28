import { createCityResolver } from './cities.js';

export const MAX_ARCHIVE_BYTES = 128 * 1024 * 1024;
const MAX_ARCHIVE_ENTRIES = 8000;
const IMAGE_EXTENSION = /\.(jpe?g|heic|heif|hif|png|webp|tiff?|dng|avif|gif|bmp|arw|cr2|cr3|nef|orf|raf|rw2)$/i;
const numberFormat = new Intl.NumberFormat('ko-KR');
export const formatNumber = value => numberFormat.format(value);
export function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  const unit = bytes >= 1024 ** 3 ? 3 : bytes >= 1024 ** 2 ? 2 : 1;
  return `${(bytes / 1024 ** unit).toLocaleString('ko-KR', { maximumFractionDigits: 1 })} ${['B', 'KB', 'MB', 'GB'][unit]}`;
}
export function isPhoto(file) {
  return !file.name.startsWith('._') && (file.type.startsWith('image/') || IMAGE_EXTENSION.test(file.name));
}
export function safeSegment(value, fallback = '이름 없음') {
  let name = String(value ?? '').normalize('NFC').replace(/[\x00-\x1f\x7f<>:"/\\|?*]/g, '_').trim().replace(/[. ]+$/g, '');
  if (!name || name === '.' || name === '..') name = fallback;
  if (/^(con|prn|aux|nul|com[1-9]|lpt[1-9])(?:\.|$)/i.test(name)) name = `_${name}`;
  // Keep UTF-8 names short enough for common filesystems, retaining extensions.
  const encoder = new TextEncoder();
  const dot = name.lastIndexOf('.');
  const extension = dot > 0 && name.length - dot <= 12 ? name.slice(dot) : '';
  let stem = extension ? name.slice(0, dot) : name;
  while (encoder.encode(stem + extension).length > 180) stem = [...stem].slice(0, -1).join('');
  return stem + extension;
}
export function captureDate(metadata = {}) {
  for (const tag of ['DateTimeOriginal', 'CreateDate', 'DateTimeDigitized', 'DateCreated']) {
    // Preserve the camera's local date; do not shift midnight across time zones.
    const value = metadata[tag];
    if (typeof value !== 'string') continue;
    const match = value.match(/^(\d{4})[:-](\d{2})[:-](\d{2})(?:[ T](\d{2}):(\d{2})(?::(\d{2}))?)?/);
    if (!match) continue;
    const [, y, m, d, h = '00', min = '00', sec = '00'] = match;
    const date = new Date(Date.UTC(+y, +m - 1, +d));
    if (+y < 1800 || date.getUTCFullYear() !== +y || date.getUTCMonth() !== +m - 1 || date.getUTCDate() !== +d || +h > 23 || +min > 59 || +sec > 59) continue;
    return { day: `${y}-${m}-${d}`, month: `${y}-${m}`, time: `${h}:${min}:${sec}`, capturedAt: `${y}-${m}-${d}T${h}:${min}:${sec}`, source: tag };
  }
  return null;
}
export function coordinates(metadata = {}) {
  const { latitude, longitude } = metadata;
  return typeof latitude === 'number' && typeof longitude === 'number' && Number.isFinite(latitude) && Number.isFinite(longitude) && Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180
    ? { latitude, longitude } : null;
}
export function pointInRing(lon, lat, ring) {
  let inside = false;
  for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
    const [x, y] = ring[i], [px, py] = ring[j];
    const dx = x - px, dy = y - py;
    // Zero-length segments must not count every point as a boundary point.
    if ((dx !== 0 || dy !== 0) && Math.abs((lat - py) * dx - (lon - px) * dy) < 1e-10 && lon >= Math.min(x, px) && lon <= Math.max(x, px) && lat >= Math.min(y, py) && lat <= Math.max(y, py)) return true;
    if ((y > lat) !== (py > lat) && lon < (px - x) * (lat - y) / (py - y) + x) inside = !inside;
  }
  return inside;
}
function indexedShapes(shapes) {
  return shapes.map(shape => ({ ...shape, rings: shape.rings.map(points => {
    const bounds = [Infinity, Infinity, -Infinity, -Infinity];
    for (const [x, y] of points) {
      bounds[0] = Math.min(bounds[0], x); bounds[1] = Math.min(bounds[1], y);
      bounds[2] = Math.max(bounds[2], x); bounds[3] = Math.max(bounds[3], y);
    }
    return { points, bounds };
  }) }));
}
export function createLocationResolver(data = { districts: [], countries: [] }, cityData) {
  const districts = indexedShapes(data.districts);
  const countries = indexedShapes(data.countries);
  const resolveCity = createCityResolver(cityData);
  const isMetro = province => /(?:특별시|광역시|특별자치시)$/.test(province);
  const shortCityName = district => isMetro(district.province)
    ? district.province.replace(/(?:특별시|광역시|특별자치시)$/, '')
    : district.name.replace(/(?:시|군)$/, '');
  const cityProvinces = new Map();
  for (const district of districts) {
    const name = shortCityName(district);
    if (!cityProvinces.has(name)) cityProvinces.set(name, new Set());
    cityProvinces.get(name).add(district.province);
  }
  const regionNames = new Intl.DisplayNames(['ko'], { type: 'region' });
  const contains = (shape, lat, lon) => shape.rings.some(({ points, bounds: [x0, y0, x1, y1] }) => lon >= x0 && lon <= x1 && lat >= y0 && lat <= y1 && pointInRing(lon, lat, points));
  return coords => {
    if (!coords) return { label: '위치 정보 없음', countryCode: null, country: '위치 정보 없음', city: null, kind: 'missing' };
    const { latitude: lat, longitude: lon } = coords;
    if (lat >= 32 && lat <= 40 && lon >= 124 && lon <= 132) {
      const district = districts.find(s => contains(s, lat, lon));
      if (district) {
        const metro = isMetro(district.province);
        const shortName = shortCityName(district);
        const city = !metro && cityProvinces.get(shortName).size > 1 ? `${shortName} (${district.province})` : shortName;
        return { label: city, countryCode: 'KR', country: '대한민국', city, cityId: `KR:${metro ? district.province : district.code}`, district: district.name, kind: 'district' };
      }
    }
    const countryShape = countries.find(s => contains(s, lat, lon));
    const city = resolveCity(coords, countryShape?.code);
    const countryCode = countryShape?.code ?? city?.countryCode ?? null;
    let country = '국가 정보 없음';
    if (countryCode) {
      try { country = regionNames.of(countryCode); } catch { country = countryShape?.name ?? countryCode; }
      if (!country || country === countryCode || country === '알 수 없는 지역') country = countryShape?.name ?? countryCode;
    }
    return { label: city?.name ?? '도시 정보 없음', countryCode, country, city: city?.name ?? null, cityId: city?.id ?? null, cityDistanceKm: city?.distanceKm ?? null, kind: city?.kind ?? 'unknown-city' };
  };
}
export function classifyPhoto(file, index, metadata, resolveLocation, readError = false) {
  const date = captureDate(metadata);
  const gps = coordinates(metadata);
  const location = resolveLocation(gps);
  const monthLabel = date ? `${date.month.slice(0, 4)}년 ${Number(date.month.slice(5))}월` : '촬영일 정보 없음';
  const groupKey = `${location.countryCode ?? (gps ? 'unknown-country' : 'no-gps')}:${location.cityId ?? 'unknown-city'}:${date?.month ?? 'undated'}`;
  const groupLabel = `${location.country}.${location.city ?? '도시 정보 없음'}.${monthLabel}`;
  const folder = [safeSegment(groupLabel), ...(date ? [date.day] : [])].join('/');
  return { index, name: file.name, size: file.size, folder, groupKey, groupLabel, country: location.country, countryCode: location.countryCode, city: location.city, cityId: location.cityId ?? null, cityDistanceKm: location.cityDistanceKm ?? null, location: location.label, locationKind: location.kind, date, gps, readError };
}
export function finishRecords(records) {
  const used = new Set();
  const sorted = [...records].sort((a, b) => (a.country ?? '').localeCompare(b.country ?? '', 'ko') || (a.date?.month ?? '9999').localeCompare(b.date?.month ?? '9999') || a.folder.localeCompare(b.folder, 'ko') || (a.date?.capturedAt ?? '').localeCompare(b.date?.capturedAt ?? '') || a.name.localeCompare(b.name, 'ko') || a.index - b.index);
  for (const record of sorted) {
    const base = safeSegment(record.name, 'photo');
    const dot = base.lastIndexOf('.');
    const stem = dot > 0 ? base.slice(0, dot) : base, ext = dot > 0 ? base.slice(dot) : '';
    let filename = base, suffix = 2;
    while (used.has(`${record.folder}/${filename}`.normalize('NFC').toLowerCase())) filename = `${stem} (${suffix++})${ext}`;
    record.path = `${record.folder}/${filename}`;
    used.add(record.path.normalize('NFC').toLowerCase());
  }
  return sorted;
}
export function planArchives(records, maxBytes = MAX_ARCHIVE_BYTES) {
  const archives = [];
  let batch = [];
  let size = 0;
  function flush() {
    if (batch.length) archives.push({ kind: 'zip', records: batch, size });
    batch = []; size = 0;
  }
  for (const record of records) {
    if (record.size > maxBytes) {
      flush(); archives.push({ kind: 'original', records: [record], size: record.size }); continue;
    }
    if (batch.length && (size + record.size > maxBytes || batch.length >= MAX_ARCHIVE_ENTRIES)) flush();
    batch.push(record); size += record.size;
  }
  flush();
  return archives;
}
export function manifestFor(records) {
  return {
    version: 3,
    generatedAt: new Date().toISOString(),
    description: '첫 폴더를 나라.도시.연월로 묶고 그 안을 촬영일별로 분류했습니다. 날짜는 촬영 메타데이터 기준입니다. 도시명은 국내 지도 경계 또는 GeoNames의 인근 도시를 기준으로 하며 LA·라스베이거스는 여행 권역으로 묶습니다. 사진 원본과 GPS는 변경하지 않았습니다.',
    cityDataAttribution: 'GeoNames, CC BY 4.0 — https://www.geonames.org/; compacted and localized for this app.',
    photos: records.map(({ name, path, size, groupKey, country, countryCode, city, cityId, cityDistanceKm, locationKind, date, gps, readError }) => ({ originalName: name, path, bytes: size, groupKey, country, countryCode, city, cityId, cityDistanceKm, locationKind, capturedAt: date?.capturedAt ?? null, dateSource: date?.source ?? null, gps, metadataReadError: readError })),
  };
}
