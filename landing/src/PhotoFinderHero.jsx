import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";
import { MagnifyingGlass, MapPin, ShieldCheck } from "@phosphor-icons/react";
import koreaProvinces from "./data/korea-provinces.json";
import { ANALYTICS_EVENTS, trackEvent } from "./analytics.js";
import {
  PHOTO_FINDER_LIBRARY_COUNT,
  PHOTO_FINDER_PLACES,
  getPhotoFinderDuration,
  getPhotoFinderState,
} from "./photoFinderDemo.js";
import { useWorldCountries } from "./worldCountries.js";

const GRID_COLUMNS = 7;
const GRID_ROWS = 16;
const VISIBLE_ROWS = 4;
const FINAL_WINDOW_START = (GRID_ROWS - VISIBLE_ROWS) * GRID_COLUMNS;
// Positions inside the final window (first three fully visible rows) where the searched place's photos sit.
const MATCH_SLOTS = [2, 8, 12, 15, 20];
const DECOY_SLOTS = [5, 10, 17, 23];
const MATCH_COUNT = MATCH_SLOTS.length;
const AUTOPLAY_DELAY_MS = 700;
const TILE_TONES = [
  ["#c9d8e4", "#9fb6c9"], ["#e6d6bf", "#c9ae8a"], ["#c8dcc4", "#8fb289"], ["#ead0c8", "#c99a8c"],
  ["#d8d2e6", "#a99cc4"], ["#dfe3d2", "#b4bd98"], ["#cfe2de", "#8fbab2"], ["#ecdcc6", "#d1a978"],
];
const KOREA_BOUNDS = { minLng: 124.5, maxLng: 130.05, minLat: 33, maxLat: 38.75 };
const KOREA_LONGITUDE_SCALE = 0.81;
const numberFormat = new Intl.NumberFormat("ko-KR");

function prefersReducedMotion() {
  return typeof window !== "undefined" && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

function tileTone(index) {
  const [from, to] = TILE_TONES[(index * 7 + Math.floor(index / 5)) % TILE_TONES.length];
  return { background: `linear-gradient(${(index * 47) % 180}deg, ${from}, ${to})` };
}

function ringsOf(geometry) {
  if (!geometry) return [];
  if (geometry.type === "Polygon") return geometry.coordinates;
  if (geometry.type === "MultiPolygon") return geometry.coordinates.flat();
  return [];
}

function ringPath(ring, project) {
  return `${ring.map(([lng, lat], index) => {
    const [x, y] = project(lng, lat);
    return `${index === 0 ? "M" : "L"}${x.toFixed(2)} ${y.toFixed(2)}`;
  }).join("")}Z`;
}

function useMapScene(place, worldCountries) {
  return useMemo(() => {
    if (place.scope === "korea") {
      const project = (lng, lat) => [
        (lng - KOREA_BOUNDS.minLng) * KOREA_LONGITUDE_SCALE * 100,
        (KOREA_BOUNDS.maxLat - lat) * 100,
      ];
      const width = (KOREA_BOUNDS.maxLng - KOREA_BOUNDS.minLng) * KOREA_LONGITUDE_SCALE * 100;
      const height = (KOREA_BOUNDS.maxLat - KOREA_BOUNDS.minLat) * 100;
      return {
        scopeLabel: "대한민국",
        viewBox: `-10 -10 ${width + 20} ${height + 20}`,
        target: project(...place.target),
        regions: koreaProvinces.map((province) => ({
          id: province.code,
          d: province.rings.map((ring) => ringPath(ring, project)).join(""),
        })),
      };
    }

    const { minLng, maxLng, minLat, maxLat } = place.view;
    const longitudeScale = Math.cos((((minLat + maxLat) / 2) * Math.PI) / 180);
    const project = (lng, lat) => [(lng - minLng) * longitudeScale * 10, (maxLat - lat) * 10];
    const width = (maxLng - minLng) * longitudeScale * 10;
    const height = (maxLat - minLat) * 10;
    const regions = worldCountries.map((country) => {
      const rings = ringsOf(country.geometry).filter((ring) => {
        const longitudes = ring.map(([lng]) => lng);
        const ringMin = Math.min(...longitudes);
        const ringMax = Math.max(...longitudes);
        // Skip antimeridian-spanning rings and anything far outside the view.
        return ringMax - ringMin < 180 && ringMax > minLng - 40 && ringMin < maxLng + 40;
      });
      return { id: String(country.id), d: rings.map((ring) => ringPath(ring, project)).join("") };
    }).filter(({ d }) => d.length > 0);
    return {
      scopeLabel: "전세계",
      viewBox: `0 0 ${width} ${height}`,
      target: project(...place.target),
      regions,
    };
  }, [place, worldCountries]);
}

function PhotoFinderHero({ storeActions, onPlaySelect }) {
  const [placeKey, setPlaceKey] = useState(PHOTO_FINDER_PLACES[0].key);
  const [run, setRun] = useState({ id: 0, startedAt: null });
  const [isReducedMotion] = useState(prefersReducedMotion);
  const [demo, setDemo] = useState(() => (
    isReducedMotion
      ? getPhotoFinderState(Number.POSITIVE_INFINITY, PHOTO_FINDER_PLACES[0].query)
      : { phase: "idle", typedQuery: "", scannedCount: 0, isMapFilled: false }
  ));
  const [flights, setFlights] = useState([]);
  const [pinPosition, setPinPosition] = useState(null);
  const mapRef = useRef(null);
  const cardRef = useRef(null);
  const counterRef = useRef(null);
  const targetRef = useRef(null);
  const matchRefs = useRef([]);
  const place = PHOTO_FINDER_PLACES.find(({ key }) => key === placeKey);
  const needsWorld = place.scope === "world" || demo.phase !== "idle";
  const worldCountries = useWorldCountries(needsWorld);
  const scene = useMapScene(place, worldCountries);

  const tiles = useMemo(() => {
    const otherPhotos = PHOTO_FINDER_PLACES
      .filter(({ key }) => key !== place.key)
      .flatMap(({ photos }) => photos);
    return Array.from({ length: GRID_COLUMNS * GRID_ROWS }, (_, index) => {
      const slot = index - FINAL_WINDOW_START;
      const matchIndex = MATCH_SLOTS.indexOf(slot);
      if (matchIndex >= 0) return { index, matchIndex, photo: place.photos[matchIndex] ?? null };
      const decoyIndex = DECOY_SLOTS.indexOf(slot);
      if (decoyIndex >= 0) return { index, matchIndex: -1, photo: otherPhotos[decoyIndex % otherPhotos.length] };
      return { index, matchIndex: -1, photo: null };
    });
  }, [place]);

  const play = useCallback((nextKey, source) => {
    setPlaceKey(nextKey);
    setFlights([]);
    if (isReducedMotion) {
      const nextPlace = PHOTO_FINDER_PLACES.find(({ key }) => key === nextKey);
      setDemo(getPhotoFinderState(Number.POSITIVE_INFINITY, nextPlace.query));
    } else {
      setRun((current) => ({ id: current.id + 1, startedAt: performance.now() }));
    }
    if (source !== "autoplay") {
      trackEvent(ANALYTICS_EVENTS.HERO_DEMO_SELECT, { experience_type: "hero_demo", demo_place: nextKey });
      onPlaySelect?.(nextKey);
    }
  }, [isReducedMotion, onPlaySelect]);

  useEffect(() => {
    if (isReducedMotion) return undefined;
    const timer = window.setTimeout(() => play(PHOTO_FINDER_PLACES[0].key, "autoplay"), AUTOPLAY_DELAY_MS);
    return () => window.clearTimeout(timer);
  }, [isReducedMotion, play]);

  useEffect(() => {
    if (run.startedAt === null) return undefined;
    const duration = getPhotoFinderDuration(place.query);
    let frame;
    let previous = null;
    const tick = (now) => {
      const next = getPhotoFinderState(now - run.startedAt, place.query);
      if (counterRef.current) counterRef.current.textContent = numberFormat.format(next.scannedCount);
      if (!previous || previous.phase !== next.phase || previous.typedQuery !== next.typedQuery || previous.isMapFilled !== next.isMapFilled) {
        previous = next;
        setDemo(next);
      }
      if (now - run.startedAt < duration) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [run, place.query]);

  useLayoutEffect(() => {
    if (demo.phase !== "fly" || !cardRef.current || !targetRef.current) return;
    const card = cardRef.current.getBoundingClientRect();
    const target = targetRef.current.getBoundingClientRect();
    const targetX = target.left + target.width / 2 - card.left;
    const targetY = target.top + target.height / 2 - card.top;
    setFlights(matchRefs.current.slice(0, MATCH_COUNT).map((node, index) => {
      if (!node) return null;
      const rect = node.getBoundingClientRect();
      return {
        index,
        photo: place.photos[index] ?? null,
        size: rect.width,
        fromX: rect.left - card.left,
        fromY: rect.top - card.top,
        toX: targetX - rect.width / 2,
        toY: targetY - rect.width / 2,
      };
    }).filter(Boolean));
  }, [demo.phase, place]);

  useLayoutEffect(() => {
    if (!demo.isMapFilled || !mapRef.current || !targetRef.current) {
      setPinPosition(null);
      return;
    }
    const map = mapRef.current.getBoundingClientRect();
    const target = targetRef.current.getBoundingClientRect();
    setPinPosition({ x: target.left + target.width / 2 - map.left, y: target.top + target.height / 2 - map.top });
  }, [demo.isMapFilled, scene]);

  const isFound = demo.phase === "match" || demo.phase === "fly" || demo.phase === "done";
  const counterText = numberFormat.format(demo.phase === "idle" ? 0 : demo.phase === "scan" ? demo.scannedCount : PHOTO_FINDER_LIBRARY_COUNT);
  useLayoutEffect(() => {
    if (counterRef.current) counterRef.current.textContent = counterText;
  }, [counterText]);
  const resultText = `${numberFormat.format(PHOTO_FINDER_LIBRARY_COUNT)}장 중 ${place.label} 사진 ${place.foundCount}장을 찾았어요`;

  return (
    <section className="finder-hero" aria-labelledby="finder-hero-title">
      <div className="finder-hero-copy">
        <h1 id="finder-hero-title"><span>사진첩 {numberFormat.format(PHOTO_FINDER_LIBRARY_COUNT)}장,</span><em>검색 한 번이면 그 여행만 지도로</em></h1>
        <p className="finder-hero-description">장소만 검색하세요.<br /> 그곳에서 찍은 사진은 폰이 찾아 줘요.</p>
        <div className="finder-hero-actions">{storeActions}</div>
        <p className="finder-trust-note"><ShieldCheck size={18} weight="fill" />사진은 폰 안에서 찾고, 고른 사진만 올라가요.</p>
      </div>

      <div className="finder-demo-wrap">
        <div
          className="finder-demo"
          ref={cardRef}
          data-phase={demo.phase}
          data-place={place.key}
          aria-label={`예시: 사진첩에서 ${place.label}을 검색하면 그곳에서 찍은 사진만 찾아 지도에 칠하는 모습`}
          role="img"
        >
          <div className="finder-demo-bar" aria-hidden="true">
            <span className="finder-search">
              <MagnifyingGlass size={16} weight="bold" />
              <span className="finder-search-text">{demo.typedQuery || <span className="finder-search-placeholder">장소 검색</span>}</span>
              {(demo.phase === "type" || demo.phase === "scan") && <span className="finder-caret" />}
            </span>
            <span className="finder-counter">사진 <strong ref={counterRef} />장</span>
          </div>

          <div className="finder-grid-window" aria-hidden="true">
            <div className="finder-grid-strip" key={`${run.id}-${place.key}`}>
              {tiles.map((tile) => (
                <span
                  key={tile.index}
                  className={`finder-tile ${tile.matchIndex >= 0 ? "is-match" : ""}`}
                  ref={tile.matchIndex >= 0 ? (node) => { matchRefs.current[tile.matchIndex] = node; } : undefined}
                  style={{ "--match-order": Math.max(tile.matchIndex, 0) }}
                >
                  <span style={tile.photo ? undefined : tileTone(tile.index)}>
                    {tile.photo && <img src={tile.photo} alt="" loading="lazy" decoding="async" />}
                  </span>
                </span>
              ))}
            </div>
            <span className="finder-sample-badge">예시</span>
          </div>

          <div className={`finder-map ${demo.isMapFilled ? "is-filled" : ""}`} ref={mapRef} aria-hidden="true">
            <span className="finder-map-scope">{scene.scopeLabel}</span>
            <svg viewBox={scene.viewBox} preserveAspectRatio="xMidYMid meet">
              {scene.regions.map((region) => (
                <path key={region.id} d={region.d} className={region.id === place.regionCode ? "is-target" : undefined} />
              ))}
              <circle ref={targetRef} cx={scene.target[0]} cy={scene.target[1]} r="1" className="finder-map-anchor" />
            </svg>
            {pinPosition && (
              <span className="finder-map-pin" style={{ left: `${pinPosition.x}px`, top: `${pinPosition.y}px` }}>
                <MapPin size={14} weight="fill" />{place.label}
              </span>
            )}
          </div>

          {flights.map((flight) => (
            <span
              key={`${run.id}-${flight.index}`}
              className="finder-flight"
              aria-hidden="true"
              style={{
                "--size": `${flight.size}px`,
                "--from-x": `${flight.fromX}px`,
                "--from-y": `${flight.fromY}px`,
                "--to-x": `${flight.toX}px`,
                "--to-y": `${flight.toY}px`,
                "--delay": `${flight.index * 90}ms`,
              }}
            >
              <span>
                {flight.photo && <img src={flight.photo} alt="" />}
              </span>
            </span>
          ))}

          <p className={`finder-result ${demo.phase === "done" ? "is-visible" : ""}`} aria-hidden={demo.phase !== "done"}>
            <strong>{resultText}</strong>
          </p>
        </div>
        <p className="sr-only" aria-live="polite">{demo.phase === "done" ? resultText : ""}</p>

        <div className="finder-chips" role="group" aria-label="다른 장소로 찾아보기">
          <span>{isFound ? "다른 곳도 찾아보기" : "직접 해보기"}</span>
          {PHOTO_FINDER_PLACES.map((option) => (
            <button
              key={option.key}
              type="button"
              className={option.key === place.key ? "is-active" : ""}
              aria-pressed={option.key === place.key}
              onClick={() => play(option.key, "chip")}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}

export { PhotoFinderHero };
