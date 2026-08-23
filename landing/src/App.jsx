import { lazy, Suspense, useEffect, useMemo, useRef, useState } from "react";
import { feature } from "topojson-client";
import countriesTopology from "world-atlas/countries-110m.json";
import koreaProvinces from "./data/korea-provinces.json";
import {
  ArrowDown,
  ArrowRight,
  CheckCircle,
  GlobeHemisphereEast,
  MapPin,
  MapTrifold,
  Moon,
  NavigationArrow,
  Sun,
} from "@phosphor-icons/react";

const DOWNLOAD_URL = "#download";
const Globe = lazy(() => import("react-globe.gl"));

const memories = [
  {
    id: "410",
    country: "대한민국",
    location: "대전",
    title: "빵 봉투를 들고 나오던 대전의 밤",
    shortDescription: "성심당 앞의 사람들과 빵 냄새가 다시 그날을 불러와요.",
    image: "/assets/daejeon-sungsimdang-real.jpg",
    photoCredit: "Trainholic · Wikimedia Commons (CC BY-SA 4.0)",
    photoCreditUrl: "https://commons.wikimedia.org/wiki/File:Sungsimdang_2019.jpg",
    lat: 36.35,
    lng: 127.38,
  },
  {
    id: "158",
    country: "대만",
    location: "타이베이",
    title: "사람들 사이로 걷던 야시장",
    shortDescription: "불빛과 목소리가 가득했던 스린의 밤.",
    image: "/assets/taiwan-night-market-real.webp",
    photoCredit: "Hannah Tu · Unsplash",
    photoCreditUrl: "https://unsplash.com/photos/zOjeDplBZxs",
    lat: 25.03,
    lng: 121.56,
  },
  {
    id: "496",
    country: "몽골",
    location: "헨티",
    title: "말을 쉬게 하며 함께 본 노을",
    shortDescription: "강가에 멈춘 사람들과 말, 그리고 오래 남은 저녁빛.",
    image: "/assets/mongolia-horse-riders-real.jpg",
    photoCredit: "Khosbayar Surenkhorloo · Unsplash",
    photoCreditUrl: "https://unsplash.com/photos/DkpGGLtUBt0",
    lat: 48.08,
    lng: 110.52,
  },
];

const koreaMemories = [
  {
    key: "daejeon",
    provinceCode: "KR-30",
    province: "대전광역시",
    location: "대전 · 성심당",
    category: "빵집",
    title: "튀김소보로를 나눠 들던 밤",
    description: "대전역에 내리면 자연스럽게 떠오르는 빵 봉투. 장소 하나가 도시 전체의 기억이 돼요.",
    image: "/assets/daejeon-sungsimdang-real.jpg",
    photoCredit: "Trainholic · Wikimedia Commons (CC BY-SA 4.0)",
    photoCreditUrl: "https://commons.wikimedia.org/wiki/File:Sungsimdang_2019.jpg",
    lat: 36.327,
    lng: 127.427,
  },
  {
    key: "hapjeong",
    provinceCode: "KR-11",
    province: "서울특별시",
    location: "합정 · 희옥",
    category: "라멘",
    title: "줄을 서서 기다린 맑은 한 그릇",
    description: "같이 기다린 사람과 첫 국물의 온도까지, 합정의 점심을 한 장소로 다시 꺼내요.",
    image: "/assets/hapjeong-ramen-real.jpg",
    photoCredit: "Brittany Salatino · Pexels",
    photoCreditUrl: "https://www.pexels.com/photo/a-bowl-of-ramen-served-in-a-restaurant-19133971/",
    lat: 37.549,
    lng: 126.914,
  },
  {
    key: "yeosu",
    provinceCode: "KR-46",
    province: "전라남도",
    location: "여수 · 딸기모찌",
    category: "디저트",
    title: "반으로 갈라 나눠 먹던 딸기모찌",
    description: "손에 쥔 간식과 함께 걷던 바닷길처럼, 작은 트리거가 여행 전체를 기억하게 해요.",
    image: "/assets/yeosu-strawberry-mochi-real.jpg",
    photoCredit: "Jeremy Li · Pexels",
    photoCreditUrl: "https://www.pexels.com/photo/delicious-strawberry-mochi-held-in-hands-30215498/",
    lat: 34.76,
    lng: 127.662,
  },
];

const memoryByCountry = new Map(memories.map((memory) => [memory.id, memory]));
const countries = feature(countriesTopology, countriesTopology.objects.countries).features;
const koreaBounds = { minLng: 124.5, maxLng: 130.05, minLat: 33, maxLat: 38.75 };

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

function DownloadButton({ className = "" }) {
  return <a className={`button button-primary ${className}`} href={DOWNLOAD_URL}><ArrowDown size={19} weight="bold" />Mapmory 다운로드</a>;
}

function InteractiveGlobe({ selected, onSelect, theme }) {
  const globeRef = useRef(null);
  const containerRef = useRef(null);
  const [size, setSize] = useState({ width: 540, height: 540 });
  const [hoveredId, setHoveredId] = useState(null);
  const [globeMaterial, setGlobeMaterial] = useState(null);

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
    <div className="globe-shell" ref={containerRef} aria-label="회전 가능한 Mapmory 세계 지구본">
      <Suspense fallback={<div className="globe-loading"><GlobeHemisphereEast size={28} weight="duotone" /><span>지구본을 준비하고 있어요</span></div>}>
        {globeMaterial && <Globe ref={globeRef} width={size.width} height={size.height} backgroundColor="rgba(0,0,0,0)" globeMaterial={globeMaterial} showAtmosphere atmosphereColor="#93a6b8" atmosphereAltitude={0.12} polygonsData={countries}
          polygonCapColor={(polygon) => { const id = String(polygon.id); if (id === selected.id) return "#21e69a"; if (id === hoveredId && isVisited(polygon)) return "#72efbd"; return isVisited(polygon) ? "#3fd09a" : "#303b4d"; }}
          polygonSideColor={(polygon) => (isVisited(polygon) ? "#189a6d" : "#1b2532")}
          polygonStrokeColor={(polygon) => (isVisited(polygon) ? "#a3f4d3" : "#778497")}
          polygonAltitude={(polygon) => (String(polygon.id) === selected.id ? 0.025 : isVisited(polygon) ? 0.012 : 0.003)}
          polygonsTransitionDuration={250}
          onPolygonHover={(polygon) => { const visited = polygon && isVisited(polygon); setHoveredId(visited ? String(polygon.id) : null); if (containerRef.current) containerRef.current.style.cursor = visited ? "pointer" : "grab"; }}
          onPolygonClick={(polygon) => { const memory = memoryByCountry.get(String(polygon.id)); if (memory) onSelect(memory); }} />}
      </Suspense>
      <p className="globe-instruction"><GlobeHemisphereEast size={18} weight="duotone" />드래그해 돌리고, 민트색 장소를 눌러보세요</p>
    </div>
  );
}

function LocationSelector({ selected, onSelect }) {
  return (
    <div className="location-selector">
      <div className="selector-copy"><span className="selector-step">1단계</span><strong>기억이 있는 나라를 선택하세요</strong></div>
      <div className="location-shortcuts" role="group" aria-label="여행 기록 바로 선택">
        {memories.map((memory) => <button type="button" key={memory.id} className={selected.id === memory.id ? "is-active" : ""} aria-pressed={selected.id === memory.id} onClick={() => onSelect(memory)}><MapPin size={16} weight={selected.id === memory.id ? "fill" : "regular"} />{memory.location}</button>)}
      </div>
    </div>
  );
}

function PhotoCredit({ label, url }) {
  return <a className="photo-credit" href={url} target="_blank" rel="noreferrer">Photo: {label}</a>;
}

function MemoryCard({ memory }) {
  return (
    <article className="memory-card" aria-live="polite">
      <header><MapPin size={18} weight="fill" /><span>{memory.location}</span><small>{memory.country}</small></header>
      <div className="memory-image-wrap"><img key={memory.image} src={memory.image} alt={`${memory.location}에서 남긴 실제 여행 장면`} /></div>
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
      setDimensions({ width, height: Math.round(width * 1.04) });
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
      const active = selected.provinceCode === province.code;
      context.fillStyle = active ? "#21e69a" : visited ? "#6ce1b2" : theme === "dark" ? "#25343c" : "#dfe8e2";
      context.strokeStyle = active ? "#08784f" : theme === "dark" ? "#53626a" : "#aebbb3";
      context.lineWidth = active ? 2.5 : 1;
      context.fill("evenodd");
      context.stroke();
    });
  }, [dimensions, selected, theme, visitedCodes]);

  return (
    <div className="korea-map-shell" ref={shellRef}>
      <canvas ref={canvasRef} aria-label="대한민국 17개 시도 상세 지도" />
      {koreaMemories.map((memory) => {
        const [x, y] = projectPoint(memory.lng, memory.lat, dimensions.width, dimensions.height);
        return <button key={memory.key} type="button" className={`map-hotspot ${selected.key === memory.key ? "is-active" : ""}`} style={{ left: x, top: y }} onClick={() => onSelect(memory)} aria-label={`${memory.location} 기억 보기`}><span className="hotspot-dot"><MapPin size={15} weight="fill" /></span><span className="hotspot-label">{memory.location.split(" · ")[0]}<small>{memory.category}</small></span></button>;
      })}
      <div className="map-legend"><i />기억이 있는 지역 <span>{koreaMemories.length}곳</span></div>
    </div>
  );
}

function KoreaDetailExperience({ theme }) {
  const [selected, setSelected] = useState(koreaMemories[0]);

  return (
    <section className="detail-section" id="korea-detail">
      <div className="detail-heading">
        <div><p className="eyebrow">FROM GLOBE TO REGION</p><h2>대한민국에서는<br /><em>지역의 기억까지</em> 들어가요.</h2></div>
        <p>지구본에서 나라를 고른 다음, 실제 앱처럼 17개 시·도 지도로 자연스럽게 이어집니다. 대전·합정·여수를 눌러보세요.</p>
      </div>

      <div className="detail-demo">
        <header className="map-app-header">
          <div><span className="app-kicker">MY TRIP MAP</span><h3>나의 대한민국 지도</h3></div>
          <div className="map-progress"><strong>3</strong><span>/ 17</span><small>18% 채움</small></div>
        </header>
        <div className="scope-toggle" role="group" aria-label="지도 범위"><button type="button" className="is-active"><MapTrifold size={17} weight="fill" />대한민국</button><a href="#experience"><GlobeHemisphereEast size={17} weight="duotone" />전세계</a></div>
        <div className="region-shortcuts" role="group" aria-label="상세 지역 기억 선택">
          <span><b>2단계</b> 지역을 눌러 기억을 여세요</span>
          <div>{koreaMemories.map((memory) => <button key={memory.key} type="button" className={selected.key === memory.key ? "is-active" : ""} onClick={() => setSelected(memory)} aria-pressed={selected.key === memory.key}>{memory.location}</button>)}</div>
        </div>
        <div className="detail-stage">
          <KoreaMap selected={selected} onSelect={setSelected} theme={theme} />
          <article className="region-memory-card" aria-live="polite">
            <div className="region-photo"><img key={selected.image} src={selected.image} alt={`${selected.location}의 실제 사진`} /><span>{selected.category}</span></div>
            <div className="region-memory-body">
              <p className="region-location"><NavigationArrow size={17} weight="fill" />{selected.location}<small>{selected.province}</small></p>
              <h3>{selected.title}</h3>
              <p>{selected.description}</p>
              <PhotoCredit label={selected.photoCredit} url={selected.photoCreditUrl} />
              <a className="button button-primary region-cta" href="#download">내 기억 지도도 만들기<ArrowRight size={18} weight="bold" /></a>
            </div>
          </article>
        </div>
      </div>
    </section>
  );
}

function App() {
  const [theme, setTheme] = useState(() => localStorage.getItem("mapmory-theme") || "light");
  const [selectedMemory, setSelectedMemory] = useState(memories[0]);

  useEffect(() => { document.documentElement.dataset.theme = theme; localStorage.setItem("mapmory-theme", theme); }, [theme]);

  return (
    <main id="top">
      <header className="site-header">
        <Brand />
        <nav aria-label="주요 메뉴"><a href="#experience">전세계 체험</a><a href="#korea-detail">대한민국 상세지도</a><a href="#journey">사용 흐름</a></nav>
        <div className="header-actions"><ThemeToggle theme={theme} onChange={setTheme} /><DownloadButton className="header-download" /></div>
      </header>

      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow">PLACE-BASED MEMORY ARCHIVE</p>
          <h1>다녀온 곳이 쌓일수록,<br />나만의 <em>기억 지도</em>가 완성돼요.</h1>
          <p className="hero-description">사진은 앨범에 흩어져도, 장소는 기억을 다시 불러와요.<br />여행부터 데이트, 카페, 라멘까지 원하는 방식으로 모아보세요.</p>
          <div className="hero-actions"><DownloadButton /><a className="button button-secondary" href="#experience"><GlobeHemisphereEast size={19} weight="duotone" />설치 전에 먼저 체험하기</a></div>
          <p className="release-note"><CheckCircle size={17} weight="fill" />비공개 테스트 진행 중 · Google Play 출시 준비</p>
        </div>
      </section>

      <section className="experience-section" id="experience">
        <div className="section-heading"><p className="eyebrow">STEP 1 · TRY THE GLOBE</p><h2>지구본을 돌려<br />기억을 먼저 열어보세요.</h2><p>색칠된 나라를 누르면 실제 사진과 그날의 기억이 열립니다.</p></div>
        <LocationSelector selected={selectedMemory} onSelect={setSelectedMemory} />
        <div className="experience-stage">
          <article className="globe-panel" id="globe-demo"><header><span><GlobeHemisphereEast size={19} weight="duotone" />3D 기억 지도</span><small><i />민트색 = 저장된 장소</small></header><InteractiveGlobe selected={selectedMemory} onSelect={setSelectedMemory} theme={theme} /></article>
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
        <p>공개 출시 후 Google Play에서 바로 다운로드할 수 있어요.</p>
        <button className="button button-primary" type="button" disabled><ArrowDown size={19} weight="bold" />Google Play 출시 준비 중</button>
      </section>

      <footer><div><Brand /><p>기억은 흩어져도, 지도는 남아요.</p></div><p>© 2026 Mapmory. All rights reserved.</p></footer>
    </main>
  );
}

export { App };
