import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { parse } from '../src/vendor/exifr.js';
import { unzipSync, strFromU8 } from '../src/vendor/fflate.js';
import { captureDate, coordinates, safeSegment, createLocationResolver, classifyPhoto, finishRecords, planArchives, isPhoto } from '../src/organize.js';
import { makeArchive } from '../src/zip.js';
import { createCityResolver } from '../src/cities.js';

// A real little-endian TIFF metadata segment with EXIF and rational GPS fields.
function exifFixture() {
  const bytes = new Uint8Array(178);
  const view = new DataView(bytes.buffer);
  const u16 = (o, v) => view.setUint16(o, v, true);
  const u32 = (o, v) => view.setUint32(o, v, true);
  const entry = (o, tag, type, count, value) => { u16(o, tag); u16(o + 2, type); u32(o + 4, count); u32(o + 8, value); };
  bytes.set([0x49, 0x49, 42, 0, 8, 0, 0, 0]);
  u16(8, 2); entry(10, 0x8769, 4, 1, 38); entry(22, 0x8825, 4, 1, 56);
  u16(38, 1); entry(40, 0x9003, 2, 20, 110);
  u16(56, 4); entry(58, 1, 2, 2, 78); entry(70, 2, 5, 3, 130); entry(82, 3, 2, 2, 69); entry(94, 4, 5, 3, 154);
  bytes.set(new TextEncoder().encode('2026:09:21 00:03:04\0'), 110);
  for (const [offset, degrees, minutes, seconds] of [[130, 37, 34, 48], [154, 126, 58, 48]]) {
    for (const [i, value] of [degrees, minutes, seconds].entries()) { u32(offset + i * 8, value); u32(offset + i * 8 + 4, 1); }
  }
  return bytes;
}
const boundaries = JSON.parse(await readFile(new URL('../src/data/regions.json', import.meta.url), 'utf8'));
const cities = JSON.parse(await readFile(new URL('../src/data/cities.json', import.meta.url), 'utf8'));
const locate = createLocationResolver(boundaries, cities);

test('real EXIF is parsed, geolocated and grouped by the camera local date', async () => {
  const metadata = await parse(exifFixture(), { tiff: true, gps: true, reviveValues: false });
  assert.equal(metadata.DateTimeOriginal, '2026:09:21 00:03:04');
  assert.ok(Math.abs(metadata.latitude - 37.58) < 1e-8);
  assert.ok(Math.abs(metadata.longitude - 126.98) < 1e-8);
  const record = classifyPhoto(new File([exifFixture()], '여행.tiff'), 0, metadata, locate);
  assert.equal(record.location, '서울');
  assert.equal(record.folder, '대한민국.서울.2026년 9월/2026-09-21');
  assert.equal(record.date.time, '00:03:04');
});
test('missing or invalid dates never use the file modification date', () => {
  const file = new File(['no exif'], '스크린샷.png', { type: 'image/png', lastModified: Date.now() });
  const record = classifyPhoto(file, 0, {}, locate);
  assert.equal(record.folder, '위치 정보 없음.도시 정보 없음.촬영일 정보 없음');
  for (const value of ['0000:00:00 00:00:00', '2026:02:29 12:00:00', '2024:13:01 00:00:00', '2026:05:01 26:00:00']) assert.equal(captureDate({ DateTimeOriginal: value }), null);
  assert.equal(captureDate({ DateTimeOriginal: '2024:02:29 00:30:00+14:00' }).day, '2024-02-29');
  assert.equal(captureDate({ DateTimeOriginal: 'bad', CreateDate: '2026-09-22T23:40:00-12:00' }).day, '2026-09-22');
});
test('GPS accepts zero and southern/western coordinates and rejects missing/invalid values', () => {
  assert.deepEqual(coordinates({ latitude: 0, longitude: 0 }), { latitude: 0, longitude: 0 });
  for (const metadata of [{}, { latitude: NaN, longitude: 1 }, { latitude: 91, longitude: 2 }, { latitude: 1, longitude: 181 }, { latitude: '', longitude: 1 }]) assert.equal(coordinates(metadata), null);
  assert.equal(locate({ latitude: -33.86, longitude: 151.21 }).countryCode, 'AU');
  assert.equal(createLocationResolver()({ latitude: 0, longitude: 0 }).label, '도시 정보 없음');
});
test('bundled boundaries distinguish actual places and retain country fallback', () => {
  assert.equal(locate({ latitude: 37.4979, longitude: 127.0276 }).city, '서울');
  assert.equal(locate({ latitude: 35.1631, longitude: 129.1635 }).city, '부산');
  assert.equal(locate({ latitude: 35.6812, longitude: 139.7671 }).countryCode, 'JP');
  assert.equal(locate({ latitude: 48.8566, longitude: 2.3522 }).countryCode, 'FR');
});
test('duplicate, case-insensitive and unsafe names never overwrite or escape folders', () => {
  const names = ['IMG.JPG', 'img.jpg', 'IMG (2).JPG', '../evil.jpg', 'CON.jpg', '사진.jpg', '사진.jpg'];
  const records = finishRecords(names.map((name, i) => classifyPhoto(new File(['x'], name), i, {}, locate)));
  assert.equal(new Set(records.map(r => r.path.toLowerCase().normalize('NFC'))).size, names.length);
  assert.ok(records.every(r => !r.path.split('/').includes('..')));
  assert.equal(safeSegment('CON.jpg'), '_CON.jpg');
  assert.ok(new TextEncoder().encode(safeSegment('사진'.repeat(150) + '.heic')).length <= 180);
});
test('ZIP volumes retain every photo, including files too large for in-memory ZIPs', () => {
  const records = [40, 40, 60, 101, 40, 0].map((size, index) => ({ size, index }));
  const volumes = planArchives(records, 100);
  assert.deepEqual(volumes.map(v => [v.kind, v.size]), [['zip', 80], ['zip', 60], ['original', 101], ['zip', 40]]);
  assert.deepEqual(volumes.flatMap(v => v.records.map(r => r.index)), [0, 1, 2, 3, 4, 5]);
});
test('streaming ZIP extracts to correct folders with byte-identical originals and a manifest', async () => {
  const files = [new File([exifFixture()], '사진.tiff', { type: 'image/tiff' }), new File([new Uint8Array([1, 2, 3, 4])], '사진.tiff', { type: 'image/tiff' }), new File([], 'empty.jpg', { type: 'image/jpeg' })];
  const metadata = await parse(exifFixture(), { reviveValues: false });
  const records = finishRecords(files.map((file, index) => classifyPhoto(file, index, index < 2 ? metadata : {}, locate, index > 0)));
  const progress = [];
  const blob = await makeArchive(records.map(record => ({ record, file: files[record.index] })), p => progress.push(p.percent));
  const content = unzipSync(new Uint8Array(await blob.arrayBuffer()));
  assert.equal(Object.keys(content).length, 4);
  for (const record of records) assert.deepEqual(content[`정리한 사진/${record.path}`], new Uint8Array(await files[record.index].arrayBuffer()));
  const manifest = JSON.parse(strFromU8(content['정리한 사진/분류 내역.json']));
  assert.equal(manifest.photos.length, 3);
  assert.ok(manifest.photos.some(p => p.capturedAt === null));
  assert.ok(manifest.photos.some(p => p.metadataReadError));
  assert.equal(progress.at(-1), 100);
  assert.ok(progress.every((value, index) => !index || value >= progress[index - 1]));
});
test('empty images and inaccessible originals have explicit ZIP outcomes', async () => {
  const empty = new File([], 'empty.jpg');
  const [record] = finishRecords([classifyPhoto(empty, 0, {}, locate)]);
  const output = await makeArchive([{ file: empty, record }]);
  assert.ok(output.size > 0);
  const broken = { size: 10, lastModified: 0, stream: () => new ReadableStream({ start(controller) { controller.error(new Error('unavailable')); } }) };
  await assert.rejects(makeArchive([{ file: broken, record }]), /unavailable/);
});
test('phone formats with empty MIME types are accepted and unrelated files excluded', () => {
  for (const name of ['IMG.HEIC', 'photo.avif', 'photo.dng', 'photo.JPG']) assert.ok(isPhoto(new File(['x'], name)));
  assert.equal(isPhoto(new File(['x'], '._photo.jpg')), false);
  assert.equal(isPhoto(new File(['x'], 'notes.txt')), false);
});

test('Las Vegas Strip and LAX resolve to readable metropolitan city names', () => {
  for (const coords of [{ latitude: 36.1147, longitude: -115.1728 }, { latitude: 36.1699, longitude: -115.1398 }]) {
    const result = locate(coords);
    assert.equal(result.countryCode, 'US');
    assert.equal(result.city, '라스베이거스');
  }
  for (const coords of [{ latitude: 33.9416, longitude: -118.4085 }, { latitude: 34.0522, longitude: -118.2437 }]) {
    const result = locate(coords);
    assert.equal(result.countryCode, 'US');
    assert.equal(result.city, '로스앤젤레스 (LA)');
  }
});
test('first folder combines country, city and month, merging only matching city/month photos', () => {
  const samples = [
    [2026, 1, 36.1147, -115.1728], [2026, 1, 34.0522, -118.2437], [2026, 1, 36.1699, -115.1398],
    [2025, 1, 36.1147, -115.1728], [2026, 2, 34.0522, -118.2437], [2026, 1, 48.8566, 2.3522],
  ];
  const records = samples.map(([year, month, latitude, longitude], index) => classifyPhoto(new File(['photo'], `photo-${index}.jpg`), index, { DateTimeOriginal: `${year}:${String(month).padStart(2, '0')}:12 12:00:00`, latitude, longitude }, locate));
  assert.equal(new Set(records.slice(0, 3).map(r => r.groupKey)).size, 2);
  assert.equal(records[0].groupKey, records[2].groupKey);
  assert.equal(new Set(records.slice(0, 3).map(r => r.folder.split('/')[0])).size, 2);
  assert.equal(records[0].folder, '미국.라스베이거스.2026년 1월/2026-01-12');
  assert.equal(records[1].folder, '미국.로스앤젤레스 (LA).2026년 1월/2026-01-12');
  assert.equal(new Set(records.map(r => r.groupKey)).size, 5);
  const sorted = finishRecords(records);
  assert.equal(sorted.filter(r => r.countryCode === 'US')[0].date.month, '2025-01');
});
test('remote places are not labelled as distant cities, and missing data preserves GPS', () => {
  assert.equal(locate({ latitude: 36.1069, longitude: -112.1129 }).city, null); // Grand Canyon
  const original = { latitude: 36.1147, longitude: -115.1728 };
  const withoutCities = classifyPhoto(new File(['x'], 'photo.jpg'), 0, original, createLocationResolver(boundaries));
  assert.equal(withoutCities.countryCode, 'US');
  assert.equal(withoutCities.city, null);
  assert.deepEqual(withoutCities.gps, original);
  assert.equal(withoutCities.folder, '미국.도시 정보 없음.촬영일 정보 없음');
  const withoutBoundaries = createLocationResolver(undefined, cities)(original);
  assert.equal(withoutBoundaries.countryCode, 'US');
  assert.equal(withoutBoundaries.city, '라스베이거스');
});
test('city lookup respects country boundaries and handles the antimeridian', () => {
  const resolve = createCityResolver({ cities: [[1, 'AA', 'A', 'Across the line', 0, -179.99], [2, 'BB', 'B', 'Same country', 0, 179.8]] });
  assert.equal(resolve({ latitude: 0, longitude: 179.99 }).id, 1);
  assert.equal(resolve({ latitude: 0, longitude: 179.99 }, 'BB').id, 2);
  assert.equal(resolve({ latitude: 0, longitude: 160 }), null);
});
test('ZIP folders use country.city.month without a separate city subfolder and preserve originals', async () => {
  const files = [new File(['Las Vegas original'], 'IMG.jpg'), new File(['LA original'], 'IMG.jpg')];
  const coords = [{ latitude: 36.1147, longitude: -115.1728 }, { latitude: 34.0522, longitude: -118.2437 }];
  const records = finishRecords(files.map((file, index) => classifyPhoto(file, index, { ...coords[index], DateTimeOriginal: '2026:01:12 10:00:00' }, locate)));
  const zip = await makeArchive(records.map(record => ({ file: files[record.index], record })));
  const output = unzipSync(new Uint8Array(await zip.arrayBuffer()));
  assert.equal(strFromU8(output['정리한 사진/미국.라스베이거스.2026년 1월/2026-01-12/IMG.jpg']), 'Las Vegas original');
  assert.equal(strFromU8(output['정리한 사진/미국.로스앤젤레스 (LA).2026년 1월/2026-01-12/IMG.jpg']), 'LA original');
  const manifest = JSON.parse(strFromU8(output['정리한 사진/분류 내역.json']));
  assert.equal(manifest.version, 3);
  assert.equal(new Set(manifest.photos.map(p => p.groupKey)).size, 2);
  assert.ok(manifest.photos.every(p => p.city && p.cityId && p.gps));
});

test('Korean first folders use concise city names without merging namesake cities', () => {
  const photo = new File(['photo'], 'photo.jpg');
  const classify = (latitude, longitude) => classifyPhoto(photo, 0, { latitude, longitude, DateTimeOriginal: '2026:01:15 10:00:00' }, locate);
  assert.equal(classify(37.2636, 127.0286).folder, '대한민국.수원.2026년 1월/2026-01-15');
  const seoul1 = classify(37.58, 126.98), seoul2 = classify(37.4979, 127.0276);
  assert.equal(seoul1.groupLabel, '대한민국.서울.2026년 1월');
  assert.equal(seoul1.groupKey, seoul2.groupKey);
  const gwangju = classify(35.1595, 126.8526), gyeonggiGwangju = classify(37.4293, 127.2553);
  assert.equal(gwangju.city, '광주');
  assert.equal(gyeonggiGwangju.city, '광주 (경기도)');
  assert.notEqual(gwangju.groupKey, gyeonggiGwangju.groupKey);
  assert.notEqual(gwangju.groupLabel, gyeonggiGwangju.groupLabel);
});
