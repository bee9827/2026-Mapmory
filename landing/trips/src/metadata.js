import * as defaultParser from './vendor/exifr.js';
let readerPromise;
async function defaultReader() {
  readerPromise ??= import('./vendor/exifreader.js').then(module => module.default ?? globalThis.ExifReader);
  return readerPromise;
}
function firstDefined(...values) {
  return values.find((value) => value !== undefined && value !== null && value !== "");
}

export function normalizeGpsCoordinate(value, ref, axis) {
  const rawValue = value && typeof value === "object" && !Array.isArray(value) && "value" in value
    ? value.value
    : value;
  const rawRef = String(ref ?? "").trim().toUpperCase();
  const inlineRef = typeof rawValue === "string" ? rawValue.trim().match(/[NSEW]$/i)?.[0]?.toUpperCase() : "";
  const direction = inlineRef || rawRef;
  let coordinate;

  if (typeof rawValue === "number") {
    coordinate = rawValue;
  } else if (Array.isArray(rawValue)) {
    const [degrees, minutes = 0, seconds = 0] = rawValue.map(Number);
    if ([degrees, minutes, seconds].every(Number.isFinite)) {
      coordinate = Math.abs(degrees) + minutes / 60 + seconds / 3600;
      if (degrees < 0) coordinate *= -1;
    }
  } else if (typeof rawValue === "string") {
    const cleaned = rawValue.trim().replace(/[NSEW]$/i, "").trim();
    const parts = cleaned.match(/-?\d+(?:\.\d+)?/g)?.map(Number) ?? [];
    if (parts.length === 1) {
      coordinate = parts[0];
    } else if (parts.length >= 2) {
      coordinate = Math.abs(parts[0]) + parts[1] / 60 + (parts[2] ?? 0) / 3600;
      if (parts[0] < 0) coordinate *= -1;
    }
  }

  if (!Number.isFinite(coordinate)) return Number.NaN;
  if ((direction === "S" || direction === "W") && coordinate > 0) coordinate *= -1;
  const limit = axis === "latitude" ? 90 : 180;
  return Math.abs(coordinate) <= limit ? coordinate : Number.NaN;
}

// Generated from /recap; only the date/output adapter differs.
export async function readMetadata(file, exifr=defaultParser, getExifReader=defaultReader) {
    let metadata = null;
    let metadataError = null;
    let gpsMetadata = null;
    let gpsError = null;
    let fallbackMetadata = null;
    let fallbackError = null;
    let arrayBufferPromise = null;
    const getArrayBuffer = () => {
      arrayBufferPromise ??= file.arrayBuffer();
      return arrayBufferPromise;
    };
    const getByteInput = () => {
      return getArrayBuffer().then((buffer) => new Uint8Array(buffer));
    };
    const metadataOptions = {
      gps: true,
      tiff: true,
      xmp: true,
      exif: true,
      ifd0: true,
      interop: false,
      makerNote: false,
      userComment: false,
    };
    try {
      metadata = await exifr.parse(file, metadataOptions);
    } catch {
      try {
        metadata = await exifr.parse(await getByteInput(), metadataOptions);
      } catch (error) {
        metadataError = error;
        metadata = null;
      }
    }

    try {
      gpsMetadata = await exifr.gps(file);
    } catch {
      try {
        gpsMetadata = await exifr.gps(await getByteInput());
      } catch (error) {
        gpsError = error;
      }
    }

    const primaryLat = firstDefined(gpsMetadata?.latitude, metadata?.latitude, metadata?.GPSLatitude, metadata?.LocationLatitude);
    const primaryLng = firstDefined(gpsMetadata?.longitude, metadata?.longitude, metadata?.GPSLongitude, metadata?.LocationLongitude);
    if (primaryLat === undefined || primaryLng === undefined) {
      try {
        const ExifReader = await getExifReader();
        fallbackMetadata = await ExifReader.load(file, {
          expanded: true,
          computed: true,
          includeOffsets: true,
          length: "auto",
        });
      } catch {
        try {
          const ExifReader = await getExifReader();
          fallbackMetadata = await ExifReader.load(await getArrayBuffer(), { expanded: true, computed: true });
        } catch (error) {
          fallbackError = error;
        }
      }
    }

    const lat = normalizeGpsCoordinate(
      firstDefined(primaryLat, fallbackMetadata?.gps?.Latitude),
      firstDefined(metadata?.GPSLatitudeRef, metadata?.LatitudeRef),
      "latitude",
    );
    const lng = normalizeGpsCoordinate(
      firstDefined(primaryLng, fallbackMetadata?.gps?.Longitude),
      firstDefined(metadata?.GPSLongitudeRef, metadata?.LongitudeRef),
      "longitude",
    );
    const hasGps = Number.isFinite(lat) && Number.isFinite(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180;

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
