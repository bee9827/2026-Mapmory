import { lazy, Suspense, useEffect, useMemo, useRef, useState } from "react";
import koreaProvinces from "./data/korea-provinces.json";
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  Bell,
  CheckCircle,
  EnvelopeSimple,
  GlobeHemisphereEast,
  MapPin,
  MapTrifold,
  Moon,
  NavigationArrow,
  Sun,
} from "@phosphor-icons/react";
import { ANALYTICS_EVENTS, trackEvent } from "./analytics.js";
import { subscribeToLaunchWaitlist } from "./waitlist.js";
import { useExperienceAnalytics } from "./useExperienceAnalytics.js";

const GOOGLE_PLAY_URL = import.meta.env.VITE_GOOGLE_PLAY_URL?.trim();
const Globe = lazy(() => import("react-globe.gl"));

const memories = [
  {
    key: "hapjeong",
    id: "410",
    country: "대한민국",
    location: "서울 · 합정",
    title: "기다림 끝에 만난 희옥의 시오 라멘",
    shortDescription: "감칠맛이 선명하고 산미를 영리하게 살린 한 그릇. 긴 웨이팅까지 합정의 기억으로 남았어요.",
    image: "/assets/team-hapjeong-huiok.jpg",
    photoCredit: "Mapmory 개발팀 촬영",
    lat: 37.549,
    lng: 126.914,
  },
  {
    key: "shanghai",
    id: "156",
    country: "중국",
    location: "상하이 · 와이탄",
    title: "황푸강 건너로 번지던 상하이의 밤",
    shortDescription: "불빛이 켜진 푸둥의 스카이라인을 오래 바라보던 여행의 한 장면이에요.",
    image: "/assets/team-shanghai-bund.jpg",
    photoCredit: "Mapmory 개발팀 촬영",
    lat: 31.2304,
    lng: 121.4737,
  },
  {
    key: "tokyo",
    id: "392",
    country: "일본",
    location: "도쿄",
    title: "초록불을 따라 걷던 도쿄의 골목",
    shortDescription: "복잡한 전선과 작은 가게, 평범해서 더 오래 남은 도쿄의 오후예요.",
    image: "/assets/team-tokyo-street.jpeg",
    photoCredit: "Mapmory 개발팀 촬영",
    lat: 35.6762,
    lng: 139.6503,
  },
];

const koreaMemories = [
  {
    key: "hapjeong",
    provinceCode: "KR-11",
    districtCode: "11440",
    province: "서울특별시",
    provinceShort: "서울",
    location: "합정 · 희옥",
    category: "라멘",
    title: "기다림 끝에 만난 희옥의 시오 라멘",
    description: "감칠맛이 선명하고 산미를 아주 영리하게 살린 시오 라멘. 긴 웨이팅까지도 합정의 한 장면으로 남았어요.",
    image: "/assets/team-hapjeong-huiok.jpg",
    photoCredit: "Mapmory 개발팀 촬영",
    lat: 37.549,
    lng: 126.914,
  },
  {
    key: "yeosu",
    provinceCode: "KR-46",
    districtCode: "46130",
    province: "전라남도",
    provinceShort: "전남",
    location: "여수 · 딸기모찌",
    category: "디저트",
    title: "상자를 열자마자 웃음이 나던 딸기모찌",
    description: "여수 바닷길을 걷다 고른 모찌 한 상자. 함께 나눠 먹던 달콤함이 여행 전체를 다시 불러와요.",
    image: "/assets/team-yeosu-mochi.jpg",
    photoCredit: "Mapmory 개발팀 촬영",
    lat: 34.76,
    lng: 127.662,
  },
  {
    key: "jeju",
    provinceCode: "KR-49",
    districtCode: "50110",
    province: "제주특별자치도",
    provinceShort: "제주",
    location: "제주 · 바닷가",
    category: "여행",
    title: "검은 바위 사이로 밀려오던 제주 바다",
    description: "파도 소리와 해 질 무렵의 빛만으로도 그날의 제주가 선명하게 돌아와요.",
    image: "/assets/team-jeju-coast.jpg",
    photoCredit: "Mapmory 개발팀 촬영",
    lat: 33.4996,
    lng: 126.5312,
  },
];

const memoryByCountry = new Map(memories.map((memory) => [memory.id, memory]));
const koreaBounds = { minLng: 124.5, maxLng: 130.05, minLat: 33, maxLat: 38.75 };
const districtMapCache = new Map();

function Brand() {
  return (
    <a className="brand" href="#top" aria-label="Mapmory 홈">
      <span>Map</span><strong>mory</strong>
    </a>
  );
}

function ThemeToggle({ theme, onChange }) {
  return (
    <div className="theme-toggle" role="group" aria-label="색상 테마 선택">
      <button type="button" className={theme === "light" ? "is-active" : ""} onClick={() => onChange("light")} aria-pressed={theme === "light"} aria-label="라이트 테마"><Sun size={17} weight="bold" /></button>
      <button type="button" className={theme === "dark" ? "is-active" : ""} onClick={() => onChange("dark")} aria-pressed={theme === "dark"} aria-label="다크 테마"><Moon size={17} weight="fill" /></button>
    </div>
  );
}

function DownloadButton({ className = "", placement }) {
  const handleClick = () => {
    trackEvent(
      GOOGLE_PLAY_URL ? ANALYTICS_EVENTS.DOWNLOAD_CLICK : ANALYTICS_EVENTS.WAITLIST_CTA_CLICK,
      { cta_placement: placement },
    );
  };

  if (GOOGLE_PLAY_URL) {
    return <a className={`button button-primary ${className}`} href={GOOGLE_PLAY_URL} target="_blank" rel="noreferrer" onClick={handleClick}><ArrowDown size={19} weight="bold" />Mapmory 다운로드</a>;
  }

  return <a className={`button button-primary ${className}`} href="#download" onClick={handleClick}><Bell size={19} weight="fill" />출시 알림 신청하기</a>;
}

function LaunchWaitlistForm() {
  const sectionRef = useRef(null);
  const emailRef = useRef(null);
  const hasTrackedView = useRef(false);
  const hasTrackedStart = useRef(false);
  const viewTimerRef = useRef(null);
  const [email, setEmail] = useState("");
  const [privacyConsent, setPrivacyConsent] = useState(false);
  const [ageConfirmed, setAgeConfirmed] = useState(false);
  const [submission, setSubmission] = useState({ state: "idle", message: "" });

  useEffect(() => {
    const section = sectionRef.current;
    if (!section || hasTrackedView.current) return undefined;

    const clearViewTimer = () => {
      if (viewTimerRef.current) {
        window.clearTimeout(viewTimerRef.current);
        viewTimerRef.current = null;
      }
    };
    const observer = new IntersectionObserver(([entry]) => {
      const isVisible = entry.isIntersecting && entry.intersectionRatio >= 0.5;
      if (!isVisible) {
        clearViewTimer();
        return;
      }
      if (!hasTrackedView.current && !viewTimerRef.current) {
        viewTimerRef.current = window.setTimeout(() => {
          viewTimerRef.current = null;
          hasTrackedView.current = true;
          trackEvent(ANALYTICS_EVENTS.WAITLIST_FORM_VIEW);
          observer.disconnect();
        }, 1000);
      }
    }, { threshold: 0.5 });
    observer.observe(section);
    return () => {
      clearViewTimer();
      observer.disconnect();
    };
  }, []);

  const trackFormStart = () => {
    if (hasTrackedStart.current) return;
    hasTrackedStart.current = true;
    trackEvent(ANALYTICS_EVENTS.WAITLIST_FORM_START);
  };

  const failValidation = (message, reason, focusTarget) => {
    setSubmission({ state: "error", message });
    trackEvent(ANALYTICS_EVENTS.WAITLIST_SUBMIT_ERROR, {
      error_type: "validation",
      validation_field: reason,
    });
    focusTarget?.focus();
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!emailRef.current?.checkValidity()) {
      failValidation("올바른 이메일 주소를 입력해 주세요.", "invalid_email", emailRef.current);
      return;
    }
    if (!privacyConsent) {
      failValidation("개인정보 수집 및 이용에 동의해 주세요.", "privacy_consent_required");
      return;
    }
    if (!ageConfirmed) {
      failValidation("만 14세 이상임을 확인해 주세요.", "age_confirmation_required");
      return;
    }

    setSubmission({ state: "submitting", message: "" });
    try {
      const status = await subscribeToLaunchWaitlist({
        email: email.trim(),
        privacyConsent,
        ageConfirmed,
      });
      const alreadySubscribed = status === "ALREADY_SUBSCRIBED";
      setSubmission({
        state: "success",
        message: alreadySubscribed
          ? "이미 출시 알림을 신청한 이메일이에요. 출시되면 알려드릴게요."
          : "신청됐어요. Mapmory가 출시되면 가장 먼저 알려드릴게요.",
      });
      trackEvent(ANALYTICS_EVENTS.WAITLIST_SUBMIT, {
        result: alreadySubscribed ? "already_subscribed" : "subscribed",
      });
      setEmail("");
    } catch (error) {
      const reason = error?.reason || "unknown";
      setSubmission({
        state: "error",
        message: reason === "network"
          ? "네트워크 연결을 확인한 뒤 다시 시도해 주세요."
          : "잠시 후 다시 시도해 주세요. 계속되면 Mapmory 팀에 알려주세요.",
      });
      trackEvent(ANALYTICS_EVENTS.WAITLIST_SUBMIT_ERROR, {
        error_type: reason,
      });
    }
  };

  return (
    <div className="waitlist-panel" ref={sectionRef}>
      <form className="waitlist-form" onSubmit={handleSubmit} onFocusCapture={trackFormStart} onChangeCapture={trackFormStart} noValidate>
        <label className="email-field" htmlFor="waitlist-email">
          <span className="sr-only">출시 알림을 받을 이메일</span>
          <EnvelopeSimple size={21} weight="duotone" aria-hidden="true" />
          <input
            id="waitlist-email"
            ref={emailRef}
            type="email"
            inputMode="email"
            autoComplete="email"
            maxLength={254}
            placeholder="이메일 주소"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>
        <button className="button button-primary" type="submit" disabled={submission.state === "submitting"}>
          <Bell size={19} weight="fill" />
          {submission.state === "submitting" ? "신청 중…" : "출시 알림 받기"}
        </button>
        <div className="waitlist-agreements">
          <label>
            <input type="checkbox" checked={privacyConsent} onChange={(event) => setPrivacyConsent(event.target.checked)} />
            <span><b>[필수]</b> 출시 알림을 위한 이메일 수집·이용에 동의합니다.</span>
          </label>
          <p>수집 항목: 이메일 · 이용 목적: Mapmory 출시 알림 · 보유 기간: 출시 알림 발송 후 지체 없이 파기</p>
          <label>
            <input type="checkbox" checked={ageConfirmed} onChange={(event) => setAgeConfirmed(event.target.checked)} />
            <span><b>[필수]</b> 만 14세 이상입니다.</span>
          </label>
        </div>
        {submission.message && (
          <p className={`waitlist-feedback is-${submission.state}`} role={submission.state === "error" ? "alert" : "status"}>
            {submission.message}
          </p>
        )}
      </form>
    </div>
  );
}

function InteractiveGlobe({ selected, onSelect, onInteract, theme, countrySelector }) {
  const globeRef = useRef(null);
  const containerRef = useRef(null);
  const [size, setSize] = useState({ width: 540, height: 540 });
  const [hoveredId, setHoveredId] = useState(null);
  const [globeMaterial, setGlobeMaterial] = useState(null);
  const [countries, setCountries] = useState([]);

  useEffect(() => {
    let active = true;
    Promise.all([
      import("topojson-client"),
      import("world-atlas/countries-110m.json"),
    ]).then(([{ feature }, { default: topology }]) => {
      if (active) setCountries(feature(topology, topology.objects.countries).features);
    });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    let material;
    import("three").then(({ MeshPhongMaterial }) => {
      material = new MeshPhongMaterial({ color: theme === "dark" ? "#0b111c" : "#121c27", emissive: theme === "dark" ? "#07121b" : "#0b1118", shininess: 12 });
      if (active) setGlobeMaterial(material);
      else material.dispose();
    });
    return () => { active = false; material?.dispose(); };
  }, [theme]);

  useEffect(() => {
    if (!containerRef.current) return undefined;
    const observer = new ResizeObserver(([entry]) => {
      const next = Math.max(280, Math.min(590, Math.floor(entry.contentRect.width)));
      setSize({ width: next, height: next });
    });
    observer.observe(containerRef.current);
    return () => observer.disconnect();
  }, []);

  useEffect(() => { globeRef.current?.pointOfView({ lat: selected.lat, lng: selected.lng, altitude: 2.05 }, 850); }, [selected, globeMaterial]);

  useEffect(() => {
    const controls = globeRef.current?.controls();
    if (!controls) return undefined;
    controls.enablePan = false;
    controls.minDistance = 210;
    controls.maxDistance = 410;
    controls.autoRotate = true;
    controls.autoRotateSpeed = 0.25;
    const stopAutoRotate = () => { controls.autoRotate = false; };
    const element = containerRef.current;
    element?.addEventListener("pointerdown", stopAutoRotate, { once: true });
    return () => element?.removeEventListener("pointerdown", stopAutoRotate);
  }, [size, globeMaterial]);

  const isVisited = (polygon) => memoryByCountry.has(String(polygon.id));

  return (
    <div
      className="globe-shell"
      ref={containerRef}
      role="region"
      aria-label="회전 가능한 Mapmory 세계 지구본"
      onPointerDown={() => onInteract("globe_drag")}
      onWheel={() => onInteract("globe_zoom")}
    >
      {countrySelector}
      <Suspense fallback={<div className="globe-loading"><GlobeHemisphereEast size={28} weight="duotone" /><span>지구본을 준비하고 있어요</span></div>}>
        {globeMaterial && countries.length > 0 && <Globe ref={globeRef} width={size.width} height={size.height} backgroundColor="rgba(0,0,0,0)" globeMaterial={globeMaterial} showAtmosphere atmosphereColor="#93a6b8" atmosphereAltitude={0.12} polygonsData={countries}
          polygonCapColor={(polygon) => { const id = String(polygon.id); if (id === selected.id) return "#21e69a"; if (id === hoveredId && isVisited(polygon)) return "#72efbd"; return isVisited(polygon) ? "#3fd09a" : "#303b4d"; }}
          polygonSideColor={(polygon) => (isVisited(polygon) ? "#189a6d" : "#1b2532")}
          polygonStrokeColor={(polygon) => (isVisited(polygon) ? "#a3f4d3" : "#778497")}
          polygonAltitude={(polygon) => (String(polygon.id) === selected.id ? 0.025 : isVisited(polygon) ? 0.012 : 0.003)}
          polygonsTransitionDuration={250}
          onPolygonHover={(polygon) => { const visited = polygon && isVisited(polygon); setHoveredId(visited ? String(polygon.id) : null); if (containerRef.current) containerRef.current.style.cursor = visited ? "pointer" : "grab"; }}
          onPolygonClick={(polygon) => { const memory = memoryByCountry.get(String(polygon.id)); if (memory) onSelect(memory, "globe"); }} />}
      </Suspense>
      <p className="globe-instruction"><GlobeHemisphereEast size={18} weight="duotone" />드래그해 돌리고, 민트색 장소를 눌러보세요</p>
    </div>
  );
}

function LocationSelector({ selected, onSelect }) {
  return (
    <div className="globe-country-dock" onPointerDown={(event) => event.stopPropagation()}>
      <div className="selector-copy"><span className="selector-step">나라 선택</span><strong>기억이 있는 나라</strong></div>
      <div className="location-shortcuts" role="group" aria-label="기억이 있는 나라 바로 선택">
        {memories.map((memory) => <button type="button" key={memory.id} className={selected.id === memory.id ? "is-active" : ""} aria-pressed={selected.id === memory.id} onClick={() => onSelect(memory, "shortcut")}><MapPin size={16} weight={selected.id === memory.id ? "fill" : "regular"} />{memory.country}</button>)}
      </div>
    </div>
  );
}

function PhotoCredit({ label, url }) {
  if (url) return <a className="photo-credit" href={url} target="_blank" rel="noreferrer">Photo: {label}</a>;
  return <span className="photo-credit photo-credit-owned">Photo: {label}</span>;
}

function MemoryCard({ memory }) {
  return (
    <article className="memory-card" aria-live="polite">
      <header><MapPin size={18} weight="fill" /><span>{memory.location}</span><small>{memory.country}</small></header>
      <div className="memory-image-wrap"><img key={memory.image} src={memory.image} alt={`${memory.location}에서 남긴 실제 여행 장면`} loading="lazy" decoding="async" /></div>
      <div className="memory-card-body">
        <span className="memory-kind">실제 사진으로 열린 기억</span>
        <h2>{memory.title}</h2>
        <p>{memory.shortDescription}</p>
        <PhotoCredit label={memory.photoCredit} url={memory.photoCreditUrl} />
        <a className="memory-next" href="#korea-detail"><span>{memory.id === "410" ? "대한민국 상세지역으로 이어보기" : "대한민국 상세지도도 체험하기"}</span><ArrowRight size={18} weight="bold" /></a>
      </div>
    </article>
  );
}

function projectPoint(lng, lat, width, height) {
  const padX = width * 0.11;
  const padY = height * 0.08;
  const x = padX + ((lng - koreaBounds.minLng) / (koreaBounds.maxLng - koreaBounds.minLng)) * (width - padX * 2);
  const y = height - padY - ((lat - koreaBounds.minLat) / (koreaBounds.maxLat - koreaBounds.minLat)) * (height - padY * 2);
  return [x, y];
}

function KoreaMap({ selected, onSelect, theme }) {
  const shellRef = useRef(null);
  const canvasRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 620, height: 650 });
  const visitedCodes = useMemo(() => new Set(koreaMemories.map((memory) => memory.provinceCode)), []);

  useEffect(() => {
    if (!shellRef.current) return undefined;
    const observer = new ResizeObserver(([entry]) => {
      const width = Math.max(300, Math.floor(entry.contentRect.width));
      const height = Math.round(width * 1.04);
      setDimensions((current) => (
        current.width === width && current.height === height ? current : { width, height }
      ));
    });
    observer.observe(shellRef.current);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const { width, height } = dimensions;
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = width * ratio;
    canvas.height = height * ratio;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    const context = canvas.getContext("2d");
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    context.clearRect(0, 0, width, height);
    context.lineJoin = "round";

    koreaProvinces.forEach((province) => {
      context.beginPath();
      province.rings.forEach((ring) => {
        ring.forEach(([lng, lat], index) => {
          const [x, y] = projectPoint(lng, lat, width, height);
          if (index === 0) context.moveTo(x, y);
          else context.lineTo(x, y);
        });
        context.closePath();
      });
      const visited = visitedCodes.has(province.code);
      const active = selected?.provinceCode === province.code;
      context.fillStyle = active ? "#21e69a" : visited ? "#6ce1b2" : theme === "dark" ? "#25343c" : "#dfe8e2";
      context.strokeStyle = active ? "#08784f" : theme === "dark" ? "#53626a" : "#aebbb3";
      context.lineWidth = active ? 2.5 : 1;
      context.fill("evenodd");
      context.stroke();
    });
  }, [dimensions, selected, theme, visitedCodes]);

  return (
    <div className="korea-map-shell" ref={shellRef}>
      <canvas ref={canvasRef} role="img" aria-label="대한민국 17개 시도 상세 지도. 지도 위의 서울, 전라남도, 제주특별자치도 버튼으로 장소 기억을 선택할 수 있습니다." />
      {koreaMemories.map((memory) => {
        const [x, y] = projectPoint(memory.lng, memory.lat, dimensions.width, dimensions.height);
        return <button key={memory.key} type="button" className={`map-hotspot ${selected?.key === memory.key ? "is-active" : ""}`} style={{ left: x, top: y }} onClick={() => onSelect(memory, "map")} aria-label={`${memory.province} 상세지도 보기`} aria-pressed={selected?.key === memory.key}><span className="hotspot-dot"><MapPin size={15} weight="fill" /></span><span className="hotspot-label">{memory.provinceShort}<small>{memory.location.split(" · ")[1]}</small></span></button>;
      })}
      <div className="map-legend"><i />기억이 있는 지역 <span>{koreaMemories.length}곳</span></div>
    </div>
  );
}

function calculateDistrictBounds(districts) {
  const bounds = { minLng: Infinity, maxLng: -Infinity, minLat: Infinity, maxLat: -Infinity };
  for (const district of districts) {
    for (const ring of district.rings) {
      for (const [lng, lat] of ring) {
        bounds.minLng = Math.min(bounds.minLng, lng);
        bounds.maxLng = Math.max(bounds.maxLng, lng);
        bounds.minLat = Math.min(bounds.minLat, lat);
        bounds.maxLat = Math.max(bounds.maxLat, lat);
      }
    }
  }
  return bounds;
}

function createDistrictProjection(width, height, bounds) {
  const padding = Math.min(width, height) * 0.08;
  const availableWidth = width - padding * 2;
  const availableHeight = height - padding * 2;
  const scale = Math.min(
    availableWidth / Math.max(bounds.maxLng - bounds.minLng, 0.001),
    availableHeight / Math.max(bounds.maxLat - bounds.minLat, 0.001),
  );
  const mapWidth = (bounds.maxLng - bounds.minLng) * scale;
  const mapHeight = (bounds.maxLat - bounds.minLat) * scale;
  const offsetX = (width - mapWidth) / 2;
  const offsetY = (height - mapHeight) / 2;
  return { scale, offsetX, offsetY, minLng: bounds.minLng, maxLat: bounds.maxLat };
}

function districtLabelPoint(district) {
  let largestRing = district.rings[0] ?? [];
  for (const ring of district.rings) {
    if (ring.length > largestRing.length) largestRing = ring;
  }
  const bounds = { minLng: Infinity, maxLng: -Infinity, minLat: Infinity, maxLat: -Infinity };
  for (const [lng, lat] of largestRing) {
    bounds.minLng = Math.min(bounds.minLng, lng);
    bounds.maxLng = Math.max(bounds.maxLng, lng);
    bounds.minLat = Math.min(bounds.minLat, lat);
    bounds.maxLat = Math.max(bounds.maxLat, lat);
  }
  return [(bounds.minLng + bounds.maxLng) / 2, (bounds.minLat + bounds.maxLat) / 2];
}

async function loadDistrictMap(provinceCode, signal) {
  if (districtMapCache.has(provinceCode)) return districtMapCache.get(provinceCode);

  const suffix = provinceCode.replace("KR-", "");
  const response = await fetch(`/assets/maps/korea-districts-${suffix}.json`, { signal });
  if (!response.ok) throw new Error(`district map ${response.status}`);
  const data = await response.json();
  if (!Array.isArray(data?.districts) || data.districts.length === 0) {
    throw new Error("invalid district map data");
  }
  districtMapCache.set(provinceCode, data.districts);
  return data.districts;
}

function DistrictMap({ memory }) {
  const shellRef = useRef(null);
  const canvasRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 620, height: 540 });
  const [mapState, setMapState] = useState({ status: "loading", districts: [] });

  useEffect(() => {
    const controller = new AbortController();
    setMapState({ status: "loading", districts: [] });
    loadDistrictMap(memory.provinceCode, controller.signal)
      .then((districts) => setMapState({ status: "ready", districts }))
      .catch((error) => {
        if (error.name !== "AbortError") setMapState({ status: "error", districts: [] });
      });
    return () => controller.abort();
  }, [memory.provinceCode]);

  useEffect(() => {
    if (!shellRef.current) return undefined;
    const observer = new ResizeObserver(([entry]) => {
      const width = Math.max(300, Math.floor(entry.contentRect.width));
      const height = Math.max(390, Math.round(width * 0.84));
      setDimensions((current) => (
        current.width === width && current.height === height ? current : { width, height }
      ));
    });
    observer.observe(shellRef.current);
    return () => observer.disconnect();
  }, []);

  const bounds = useMemo(
    () => (mapState.districts.length ? calculateDistrictBounds(mapState.districts) : null),
    [mapState.districts],
  );

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !bounds || mapState.status !== "ready") return;
    const { width, height } = dimensions;
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = width * ratio;
    canvas.height = height * ratio;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;
    const context = canvas.getContext("2d");
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    context.clearRect(0, 0, width, height);
    context.lineJoin = "round";
    const projection = createDistrictProjection(width, height, bounds);

    for (const district of mapState.districts) {
      const active = district.code === memory.districtCode;
      context.beginPath();
      for (const ring of district.rings) {
        for (let index = 0; index < ring.length; index += 1) {
          const [lng, lat] = ring[index];
          const x = projection.offsetX + (lng - projection.minLng) * projection.scale;
          const y = projection.offsetY + (projection.maxLat - lat) * projection.scale;
          if (index === 0) context.moveTo(x, y);
          else context.lineTo(x, y);
        }
        context.closePath();
      }
      context.fillStyle = active ? "#72e5b7" : "#172334";
      context.strokeStyle = active ? "#a8f3d5" : "#45546a";
      context.lineWidth = active ? 2 : 1;
      context.fill("evenodd");
      context.stroke();
    }

    context.textAlign = "center";
    context.textBaseline = "middle";
    for (const district of mapState.districts) {
      const active = district.code === memory.districtCode;
      const [lng, lat] = districtLabelPoint(district);
      const x = projection.offsetX + (lng - projection.minLng) * projection.scale;
      const y = projection.offsetY + (projection.maxLat - lat) * projection.scale;
      const fontSize = width < 420 ? (active ? 10 : 8) : (active ? 12 : 10);
      context.font = `${active ? 800 : 650} ${fontSize}px "Noto Sans KR", sans-serif`;
      context.fillStyle = active ? "#073521" : "#95a5b8";
      context.fillText(district.name, x, y);
    }
  }, [bounds, dimensions, mapState, memory.districtCode]);

  return (
    <div className="district-map-shell" ref={shellRef}>
      <div className="district-map-caption"><span><b>3단계</b>{memory.province} 시·군·구</span><small>민트색 = 기억이 있는 지역</small></div>
      {mapState.status === "loading" && <div className="district-map-status"><MapTrifold size={26} weight="duotone" />상세지도를 불러오고 있어요</div>}
      {mapState.status === "error" && <div className="district-map-status">상세지도를 불러오지 못했어요.</div>}
      <canvas ref={canvasRef} role="img" aria-label={`${memory.province} 시·군·구 상세 지도. ${memory.location}이 민트색으로 표시되어 있습니다.`} />
    </div>
  );
}

function KoreaDetailExperience({ theme }) {
  const [selected, setSelected] = useState(koreaMemories[0]);
  const [detailLevel, setDetailLevel] = useState(2);
  const analytics = useExperienceAnalytics("korea_detail");

  const handleSelect = (memory, selectionSource) => {
    analytics.startExperience("place_select");
    if (selected.key !== memory.key || detailLevel !== 3) {
      analytics.trackPlaceSelect(memory.key, selectionSource);
    }
    setSelected(memory);
    setDetailLevel(3);
  };

  const handleBack = () => setDetailLevel(2);

  return (
    <section className="detail-section" id="korea-detail" ref={analytics.sectionRef}>
      <div className="detail-heading">
        <div><p className="eyebrow">FROM GLOBE TO REGION</p><h2>대한민국에서는<br /><em>지역의 기억까지</em> 들어가요.</h2></div>
        <p>17개 시·도 지도에서 지역을 고르면 같은 화면 안에서 장소의 사진과 기억이 열립니다. 합정·여수·제주를 눌러보세요.</p>
      </div>

      <div className={`detail-demo detail-level-${detailLevel}`}>
        <header className="map-app-header">
          <div><span className="app-kicker">MY TRIP MAP</span><h3>{detailLevel === 2 ? "나의 대한민국 지도" : selected.province}</h3></div>
          <div className="map-progress"><strong>3</strong><span>/ 17</span><small>18% 채움</small></div>
        </header>
        <div className="scope-toggle" role="group" aria-label="지도 범위"><button type="button" className="is-active" aria-pressed="true"><MapTrifold size={17} weight="fill" />대한민국</button><a href="#experience"><GlobeHemisphereEast size={17} weight="duotone" />전세계</a></div>
        {detailLevel === 2 ? (
          <>
            <div className="region-shortcuts" role="group" aria-label="상세 지역 기억 선택">
              <span><b>2단계</b> 시·도를 눌러 장소로 들어가세요</span>
              <div>{koreaMemories.map((memory) => <button key={memory.key} type="button" onClick={() => handleSelect(memory, "shortcut")}>{memory.province}</button>)}</div>
            </div>
            <div className="detail-stage detail-stage-map">
              <KoreaMap selected={null} onSelect={handleSelect} theme={theme} />
              <aside className="map-guide" aria-label="대한민국 상세지도 이용 안내">
                <span className="selector-step">2단계</span>
                <h3>색칠된 지역을 눌러보세요</h3>
                <p>서울, 전남, 제주 중 하나를 고르면 이 화면이 해당 장소의 기억으로 바뀝니다.</p>
                <ol>
                  {koreaMemories.map((memory) => <li key={memory.key}><i /><span>{memory.province}</span><strong>{memory.location.split(" · ")[1]}</strong></li>)}
                </ol>
              </aside>
            </div>
          </>
        ) : (
          <div className="region-detail-stage" aria-live="polite">
            <div className="region-detail-toolbar">
              <button type="button" onClick={handleBack}><ArrowLeft size={18} weight="bold" />대한민국 지도로 돌아가기</button>
            </div>
            <div className="district-detail-grid">
              <DistrictMap memory={selected} />
              <article className="region-memory-card is-detail">
                <div className="region-photo"><img key={selected.image} src={selected.image} alt={`${selected.location}의 실제 사진`} loading="lazy" decoding="async" /><span>{selected.category}</span></div>
                <div className="region-memory-body">
                  <p className="region-location"><NavigationArrow size={17} weight="fill" />{selected.location}<small>{selected.province}</small></p>
                  <h3>{selected.title}</h3>
                  <p>{selected.description}</p>
                  <PhotoCredit label={selected.photoCredit} url={selected.photoCreditUrl} />
                  <a
                    className="button button-primary region-cta"
                    href="#download"
                    onClick={() => trackEvent(
                      GOOGLE_PLAY_URL ? ANALYTICS_EVENTS.DOWNLOAD_CLICK : ANALYTICS_EVENTS.WAITLIST_CTA_CLICK,
                      { cta_placement: "korea_memory" },
                    )}
                  >
                    내 기억 지도도 만들기<ArrowRight size={18} weight="bold" />
                  </a>
                </div>
              </article>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

function App() {
  const [theme, setTheme] = useState(() => localStorage.getItem("mapmory-theme") || "light");
  const [selectedMemory, setSelectedMemory] = useState(memories[0]);
  const globeAnalytics = useExperienceAnalytics("globe");

  const handleWorldSelect = (memory, selectionSource) => {
    globeAnalytics.startExperience("place_select");
    if (selectedMemory.id === memory.id) return;
    globeAnalytics.trackPlaceSelect(memory.key, selectionSource);
    setSelectedMemory(memory);
  };

  useEffect(() => { document.documentElement.dataset.theme = theme; localStorage.setItem("mapmory-theme", theme); }, [theme]);

  return (
    <main id="top">
      <header className="site-header">
        <Brand />
        <nav aria-label="주요 메뉴"><a href="#experience">전세계 체험</a><a href="#korea-detail">대한민국 상세지도</a><a href="#journey">사용 흐름</a></nav>
        <div className="header-actions"><ThemeToggle theme={theme} onChange={setTheme} /><DownloadButton className="header-download" placement="header" /></div>
      </header>

      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">PLACE-BASED MEMORY ARCHIVE</p>
          <h1>다녀온 곳이 쌓일수록,<br />나만의 <em>기억 지도</em>가 완성돼요.</h1>
          <p className="hero-description">사진은 앨범에 흩어져도, 장소는 기억을 다시 불러와요.<br />여행부터 데이트, 카페, 라멘까지 원하는 방식으로 모아보세요.</p>
          <div className="hero-actions"><DownloadButton placement="hero" /><a className="button button-secondary" href="#experience"><GlobeHemisphereEast size={19} weight="duotone" />설치 전에 먼저 체험하기</a></div>
          <p className="release-note"><CheckCircle size={17} weight="fill" />비공개 테스트 진행 중 · 이메일로 정식 출시 알림</p>
        </div>
      </section>

      <section className="experience-section" id="experience" ref={globeAnalytics.sectionRef}>
        <div className="section-heading"><p className="eyebrow">STEP 1 · TRY THE GLOBE</p><h2>지구본을 돌려<br />기억을 먼저 열어보세요.</h2><p>색칠된 나라를 누르면 실제 사진과 그날의 기억이 열립니다.</p></div>
        <div className="experience-stage">
          <article className="globe-panel" id="globe-demo"><header><span><GlobeHemisphereEast size={19} weight="duotone" />3D 기억 지도</span><small><i />민트색 = 저장된 나라</small></header><InteractiveGlobe selected={selectedMemory} onSelect={handleWorldSelect} onInteract={globeAnalytics.startExperience} theme={theme} countrySelector={<LocationSelector selected={selectedMemory} onSelect={handleWorldSelect} />} /></article>
          <MemoryCard memory={selectedMemory} />
        </div>
      </section>

      <KoreaDetailExperience theme={theme} />

      <section className="journey-section" id="journey">
        <p className="eyebrow">HOW IT FLOWS</p><h2>체험에서 내 지도로 자연스럽게</h2>
        <ol className="journey-list">
          <li><span className="journey-icon"><GlobeHemisphereEast size={30} weight="duotone" /></span><div><strong>01</strong><h3>나라를 골라요</h3><p>전세계 3D 지도에서 색칠된 기억을 열어요.</p></div></li>
          <li><span className="journey-icon"><MapTrifold size={30} weight="duotone" /></span><div><strong>02</strong><h3>지역까지 들어가요</h3><p>대한민국에서는 17개 시·도와 장소를 더 자세히 봐요.</p></div></li>
          <li><span className="journey-icon"><MapPin size={30} weight="duotone" /></span><div><strong>03</strong><h3>내 기억을 채워요</h3><p>앱에서 나만의 장소와 사진을 지도에 남겨요.</p></div></li>
        </ol>
      </section>

      <section className="download-section" id="download">
        <p className="eyebrow">YOUR MAP, YOUR MEMORY</p><h2>방금 본 장소처럼,<br />당신의 기억도 지도로.</h2>
        {GOOGLE_PLAY_URL ? (
          <><p>Google Play에서 Mapmory를 다운로드하고 나만의 기억 지도를 시작하세요.</p><DownloadButton placement="final" /></>
        ) : (
          <><p>정식 출시되면 입력한 이메일로 한 번만 알려드릴게요.</p><LaunchWaitlistForm /></>
        )}
      </section>

      <footer><div><Brand /><p>기억은 흩어져도, 지도는 남아요.</p></div><p>© 2026 Mapmory. All rights reserved.</p></footer>
    </main>
  );
}

export { App };
