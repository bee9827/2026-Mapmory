import { useMemo, useState } from "react";

type Page = "dashboard" | "members" | "feedback";
type FeedbackStatus = "미확인" | "반영 중" | "반영 완료" | "보류" | "미반영";
type Feedback = {
  id: string;
  category: string;
  content: string;
  platform: "iOS" | "Android";
  createdAt: string;
  status: FeedbackStatus;
};
type Member = {
  id: string;
  name: string;
  provider: "KAKAO" | "GUEST";
  joinedAt: string;
  records: number;
  refresh: "정상" | "갱신 실패";
  lastActive: string;
};

const feedbackStatuses: FeedbackStatus[] = ["미확인", "반영 중", "반영 완료", "보류", "미반영"];

const initialFeedback: Feedback[] = [
  { id: "FB-0248", category: "기능 제안", content: "여행 기록을 여러 장 한 번에 등록할 수 있으면 좋겠어요.", platform: "iOS", createdAt: "10.08 14:32", status: "미확인" },
  { id: "FB-0247", category: "사용성·불편", content: "지도에서 지역을 확대할 때 어디까지 확대됐는지 알기 어려워요.", platform: "Android", createdAt: "10.08 12:18", status: "반영 중" },
  { id: "FB-0246", category: "오류 제보", content: "사진 추천에서 날짜가 하루 전으로 표시되는 경우가 있어요.", platform: "iOS", createdAt: "10.07 18:51", status: "미확인" },
  { id: "FB-0245", category: "기능 제안", content: "기록에 같이 간 사람을 메모할 수 있으면 좋겠습니다.", platform: "Android", createdAt: "10.07 16:07", status: "보류" },
  { id: "FB-0244", category: "사용성·불편", content: "기록 목록에서 최신순과 오래된 순을 바꿀 수 있으면 좋겠어요.", platform: "iOS", createdAt: "10.07 09:26", status: "반영 완료" },
  { id: "FB-0243", category: "기타", content: "앱을 잘 사용하고 있어요. 지도 색감이 정말 예뻐요!", platform: "Android", createdAt: "10.06 21:43", status: "미확인" },
];

const members: Member[] = [
  { id: "M-10842", name: "김지우", provider: "KAKAO", joinedAt: "2026.10.08", records: 12, refresh: "정상", lastActive: "방금 전" },
  { id: "M-10841", name: "박서연", provider: "KAKAO", joinedAt: "2026.10.08", records: 4, refresh: "정상", lastActive: "12분 전" },
  { id: "M-10840", name: "게스트 사용자", provider: "GUEST", joinedAt: "2026.10.07", records: 2, refresh: "갱신 실패", lastActive: "어제" },
  { id: "M-10839", name: "이도윤", provider: "KAKAO", joinedAt: "2026.10.07", records: 28, refresh: "정상", lastActive: "어제" },
  { id: "M-10838", name: "게스트 사용자", provider: "GUEST", joinedAt: "2026.10.06", records: 0, refresh: "정상", lastActive: "10.06" },
  { id: "M-10837", name: "최유진", provider: "KAKAO", joinedAt: "2026.10.06", records: 7, refresh: "정상", lastActive: "10.06" },
  { id: "M-10836", name: "정민준", provider: "KAKAO", joinedAt: "2026.10.05", records: 19, refresh: "정상", lastActive: "10.05" },
];

function Icon({ name, size = 18 }: { name: string; size?: number }) {
  const common = { width: size, height: size, viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: 1.7, strokeLinecap: "round" as const, strokeLinejoin: "round" as const, "aria-hidden": true as const };
  const paths: Record<string, React.ReactNode> = {
    grid: <><rect x="3.5" y="3.5" width="7" height="7" rx="1.5"/><rect x="13.5" y="3.5" width="7" height="7" rx="1.5"/><rect x="3.5" y="13.5" width="7" height="7" rx="1.5"/><rect x="13.5" y="13.5" width="7" height="7" rx="1.5"/></>,
    users: <><path d="M16 20v-1.8a3.2 3.2 0 0 0-3.2-3.2H6.2A3.2 3.2 0 0 0 3 18.2V20"/><circle cx="9.5" cy="7" r="3.5"/><path d="M17 11a3.5 3.5 0 1 0-.2-7M17 15h.8a3.2 3.2 0 0 1 3.2 3.2V20"/></>,
    chat: <><path d="M20 11.5a7.5 7.5 0 0 1-7.5 7.5H5l-2 2v-8.5a7.5 7.5 0 1 1 15 0Z"/><path d="M8 11.5h.01M12 11.5h.01M16 11.5h.01"/></>,
    search: <><circle cx="10.8" cy="10.8" r="6.8"/><path d="m16 16 4.5 4.5"/></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"/></>,
    chevron: <path d="m9 18 6-6-6-6"/>,
    download: <><path d="M12 3v12m0 0 4-4m-4 4-4-4"/><path d="M4 17v3h16v-3"/></>,
    arrow: <><path d="M7 17 17 7M7 7h10v10"/></>,
    calendar: <><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/></>,
    more: <><circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/></>,
    logout: <><path d="M10 17l5-5-5-5M15 12H3"/><path d="M12 3h6a3 3 0 0 1 3 3v12a3 3 0 0 1-3 3h-6"/></>,
    close: <path d="m6 6 12 12M18 6 6 18"/>,
    filter: <><path d="M4 6h16M7 12h10m-7 6h4"/><circle cx="8" cy="6" r="1" fill="currentColor"/><circle cx="15" cy="12" r="1" fill="currentColor"/></>,
  };
  return <svg {...common}>{paths[name] ?? paths.grid}</svg>;
}

function App() {
  const [page, setPage] = useState<Page>("dashboard");
  const [period, setPeriod] = useState("최근 7일");
  const [feedback, setFeedback] = useState(initialFeedback);
  const [feedbackFilter, setFeedbackFilter] = useState("전체");
  const [feedbackQuery, setFeedbackQuery] = useState("");
  const [memberQuery, setMemberQuery] = useState("");
  const [selectedMember, setSelectedMember] = useState<Member | null>(null);
  const [notice, setNotice] = useState("");

  const filteredMembers = useMemo(() => members.filter((member) =>
    `${member.id} ${member.name} ${member.provider}`.toLowerCase().includes(memberQuery.toLowerCase()),
  ), [memberQuery]);
  const filteredFeedback = feedback.filter((item) => {
    const matchesStatus = feedbackFilter === "전체" || item.status === feedbackFilter;
    const matchesQuery = `${item.id} ${item.category} ${item.content} ${item.platform}`.toLowerCase().includes(feedbackQuery.toLowerCase());
    return matchesStatus && matchesQuery;
  });

  const showNotice = (message: string) => {
    setNotice(message);
    window.setTimeout(() => setNotice(""), 2600);
  };

  const pageTitle = page === "dashboard" ? "대시보드" : page === "members" ? "회원 관리" : "서비스 의견";

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-lockup">
          <div className="brand-mark"><span /><span /><span /></div>
          <div><strong>mapmory</strong><small>ADMIN CONSOLE</small></div>
        </div>
        <div className="workspace-label">WORKSPACE</div>
        <nav className="side-nav" aria-label="주 메뉴">
          <button className={page === "dashboard" ? "nav-item active" : "nav-item"} onClick={() => setPage("dashboard")}><Icon name="grid"/><span>대시보드</span></button>
          <button className={page === "members" ? "nav-item active" : "nav-item"} onClick={() => setPage("members")}><Icon name="users"/><span>회원 관리</span></button>
          <button className={page === "feedback" ? "nav-item active" : "nav-item"} onClick={() => setPage("feedback")}><Icon name="chat"/><span>서비스 의견</span><span className="nav-count">8</span></button>
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-help"><div className="help-icon">?</div><div><strong>운영 가이드</strong><span>어드민 사용 방법</span></div><Icon name="arrow" size={15}/></div>
          <div className="profile-button">
          <div className="avatar admin-avatar">M</div><div className="profile-copy"><strong>Mapmory 운영자</strong><span>운영자</span></div>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb"><span>Mapmory</span><Icon name="chevron" size={14}/><strong>{pageTitle}</strong></div>
          <div className="topbar-actions">
            <span className="environment-pill"><i /> 운영 환경</span>
            <button className="icon-button notification-button" aria-label="알림" onClick={() => showNotice("새로운 알림이 없습니다.")}><Icon name="bell"/><i /></button>
            <div className="avatar admin-avatar small-avatar">M</div>
          </div>
        </header>
        <div className="content-area">
          {page === "dashboard" && <Dashboard period={period} setPeriod={setPeriod} feedback={feedback} onNavigate={setPage} onNotice={showNotice} />}
          {page === "members" && <MembersPage query={memberQuery} setQuery={setMemberQuery} list={filteredMembers} onSelect={setSelectedMember} onNotice={showNotice} />}
          {page === "feedback" && <FeedbackPage feedback={filteredFeedback} allFeedback={feedback} filter={feedbackFilter} setFilter={setFeedbackFilter} query={feedbackQuery} setQuery={setFeedbackQuery} onStatusChange={(id, status) => setFeedback((current) => current.map((item) => item.id === id ? { ...item, status } : item))} />}
        </div>
      </main>
      {selectedMember && <MemberDrawer member={selectedMember} onClose={() => setSelectedMember(null)} />}
      {notice && <div className="toast"><span className="toast-check">✓</span>{notice}</div>}
      <div className="demo-ribbon">시연용 데이터</div>
    </div>
  );
}

function Dashboard({ period, setPeriod, feedback, onNavigate, onNotice }: { period: string; setPeriod: (period: string) => void; feedback: Feedback[]; onNavigate: (page: Page) => void; onNotice: (message: string) => void }) {
  const newFeedback = feedback.filter((item) => item.status === "미확인").length;
  const periods = ["최근 7일", "최근 30일", "전체"];
  return (
    <div className="page-content dashboard-page">
      <div className="page-heading-row"><div><div className="date-eyebrow"><span className="live-dot"/> 2026년 10월 8일 목요일</div><h1>좋은 오후예요, 운영자님 <span className="wave">✳</span></h1><p>Mapmory의 최근 현황을 확인해 보세요.</p></div><button className="secondary-button" onClick={() => onNotice("리포트 다운로드는 API 연결 후 제공됩니다.")}><Icon name="download" size={16}/> 리포트 내보내기</button></div>
      <div className="period-row"><div className="period-tabs">{periods.map((item) => <button key={item} className={period === item ? "period-tab active" : "period-tab"} onClick={() => setPeriod(item)}>{item}</button>)}</div><button className="date-range"><Icon name="calendar" size={15}/>{period === "최근 7일" ? "10.02 – 10.08" : period === "최근 30일" ? "09.09 – 10.08" : "전체 기간"}<Icon name="chevron" size={13}/></button></div>
      <section className="metric-grid" aria-label="주요 지표">
        <MetricCard label="가입 회원" value="1,284" change="12.8%" detail="지난 기간 대비" icon="users" tint="mint" trend="up" foot="게스트 812 · OAuth 472" />
        <MetricCard label="여행 기록" value="5,492" change="18.6%" detail="지난 기간 대비" icon="grid" tint="peach" trend="up" foot="최근 7일 286개 생성" />
        <MetricCard label="서비스 의견" value="248" change="12건" detail="최근 7일 접수" icon="chat" tint="lavender" trend="neutral" foot={`${newFeedback}건 확인이 필요해요`} />
      </section>
      <section className="dashboard-main-grid">
        <div className="panel chart-panel">
          <div className="panel-heading"><div><h2>서비스 성장</h2><p>가입 회원과 여행 기록 생성 추이</p></div><button className="more-button" aria-label="차트 옵션" onClick={() => onNotice("차트 옵션은 준비 중입니다.")}><Icon name="more"/></button></div>
          <div className="chart-legend"><span><i className="legend-dot mint-dot"/> 가입 회원</span><span><i className="legend-dot blue-dot"/> 여행 기록</span><div className="chart-total"><strong>+24.8%</strong><span>지난 기간 대비</span></div></div>
          <div className="chart-wrap"><div className="chart-y-labels"><span>300</span><span>225</span><span>150</span><span>75</span><span>0</span></div><div className="chart-area"><div className="chart-grid-lines"><i/><i/><i/><i/><i/></div><svg className="chart-svg" viewBox="0 0 700 230" preserveAspectRatio="none" role="img" aria-label="날짜별 회원 가입 및 여행 기록 추이 그래프"><defs><linearGradient id="mintArea" x1="0" x2="0" y1="0" y2="1"><stop offset="0%" stopColor="#79cbb0" stopOpacity=".2"/><stop offset="100%" stopColor="#79cbb0" stopOpacity="0"/></linearGradient></defs><path d="M0 175 C34 174 42 153 78 160 S120 158 156 138 S198 148 234 126 S277 133 312 115 S356 126 390 96 S434 110 468 89 S512 101 546 70 S589 82 624 55 S665 60 700 35 L700 230 L0 230Z" fill="url(#mintArea)"/><path d="M0 175 C34 174 42 153 78 160 S120 158 156 138 S198 148 234 126 S277 133 312 115 S356 126 390 96 S434 110 468 89 S512 101 546 70 S589 82 624 55 S665 60 700 35" fill="none" stroke="#55b99b" strokeWidth="3" vectorEffect="non-scaling-stroke"/><path d="M0 200 C34 195 44 188 78 191 S121 176 156 184 S200 167 234 175 S276 156 312 166 S357 151 390 155 S434 143 468 151 S511 130 546 141 S590 119 624 132 S666 108 700 115" fill="none" stroke="#94a9dd" strokeWidth="2.5" strokeDasharray="5 6" vectorEffect="non-scaling-stroke"/><circle cx="546" cy="70" r="5" fill="#fff" stroke="#55b99b" strokeWidth="3" vectorEffect="non-scaling-stroke"/></svg><div className="chart-x-labels"><span>10.02</span><span>10.03</span><span>10.04</span><span>10.05</span><span>10.06</span><span>10.07</span><span>10.08</span></div></div></div>
        </div>
        <div className="panel distribution-panel">
          <div className="panel-heading"><div><h2>회원 구성</h2><p>로그인 방식별 회원 현황</p></div><button className="more-button" aria-label="회원 페이지로 이동" onClick={() => onNavigate("members")}><Icon name="arrow" size={16}/></button></div>
          <div className="donut-area"><div className="donut"><div className="donut-center"><strong>1,284</strong><span>전체 회원</span></div></div><div className="donut-caption"><span><i className="legend-dot mint-dot"/>게스트<strong>812</strong><small>63.2%</small></span><span><i className="legend-dot peach-dot"/>OAuth<strong>472</strong><small>36.8%</small></span></div></div>
          <div className="distribution-footer"><span><i className="sparkle-icon">✦</i> 게스트 회원의 <strong>18%</strong>가 계정을 연결했어요</span><Icon name="chevron" size={14}/></div>
        </div>
      </section>
      <section className="panel feedback-preview">
        <div className="panel-heading"><div><div className="title-with-count"><h2>최근 서비스 의견</h2><span className="count-chip">{newFeedback} 신규</span></div><p>사용자가 남긴 소중한 의견이에요.</p></div><button className="text-link" onClick={() => onNavigate("feedback")}>전체 보기 <Icon name="chevron" size={14}/></button></div>
        <div className="feedback-preview-list">{feedback.slice(0, 3).map((item) => <div className="feedback-preview-item" key={item.id}><div className="category-dot"/><div className="feedback-preview-copy"><div className="feedback-preview-meta"><span className="category-label">{item.category}</span><span>{item.platform}</span><span>{item.createdAt}</span></div><p>{item.content}</p></div><StatusBadge status={item.status}/><button className="row-arrow" onClick={() => onNavigate("feedback")} aria-label="의견 관리로 이동"><Icon name="chevron" size={15}/></button></div>)}</div>
      </section>
      <footer className="page-footer"><span>Mapmory Admin <b>·</b> 내부 운영 도구</span><span>마지막 데이터 업데이트 <strong>방금 전</strong></span></footer>
    </div>
  );
}

function MetricCard({ label, value, change, detail, icon, tint, trend, foot }: { label: string; value: string; change: string; detail: string; icon: string; tint: string; trend: "up" | "neutral"; foot: string }) {
  return <article className="metric-card"><div className="metric-top"><span>{label}</span><div className={`metric-icon ${tint}`}><Icon name={icon} size={18}/></div></div><div className="metric-value">{value}</div><div className="metric-change"><span className={trend === "up" ? "change-up" : "change-neutral"}>{trend === "up" && <Icon name="arrow" size={12}/>} {change}</span><span>{detail}</span></div><div className="metric-divider"/><div className="metric-foot"><span className="foot-dot"/>{foot}</div></article>;
}

function MembersPage({ query, setQuery, list, onSelect, onNotice }: { query: string; setQuery: (query: string) => void; list: Member[]; onSelect: (member: Member) => void; onNotice: (message: string) => void }) {
  const [provider, setProvider] = useState("전체 유형");
  const visible = provider === "전체 유형" ? list : list.filter((item) => item.provider === provider);
  return <div className="page-content list-page"><div className="page-heading-row"><div><div className="date-eyebrow">MEMBERS</div><h1>회원 관리</h1><p>회원 가입과 계정 상태를 확인할 수 있어요.</p></div><button className="secondary-button" onClick={() => onNotice("회원 내보내기는 API 연결 후 제공됩니다.")}><Icon name="download" size={16}/> 내보내기</button></div>
    <div className="member-summary-row"><div className="member-summary-card"><span>전체 회원</span><strong>1,284</strong><small>지난 기간보다 <b>+84명</b></small></div><div className="member-summary-card"><span>게스트</span><strong>812</strong><small>전체 회원의 63.2%</small></div><div className="member-summary-card"><span>OAuth</span><strong>472</strong><small>전체 회원의 36.8%</small></div><div className="member-summary-card"><span>최근 갱신 실패</span><strong className="alert-value">7</strong><small>최근 7일 기준</small></div></div>
    <div className="panel table-panel"><div className="table-toolbar"><div><h2>회원 목록 <span className="muted-count">1,284</span></h2><p>계정을 선택하면 상세 상태를 볼 수 있어요.</p></div><div className="table-actions"><div className="search-field"><Icon name="search" size={17}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="이름 또는 회원 ID 검색" aria-label="회원 검색"/><kbd>⌘ K</kbd></div><select className="select-control" value={provider} onChange={(event) => setProvider(event.target.value)} aria-label="로그인 유형"><option>전체 유형</option><option value="GUEST">게스트</option><option value="KAKAO">OAuth</option></select><button className="filter-button" onClick={() => onNotice("가입일 필터는 API 연결 후 제공됩니다.")}><Icon name="filter" size={16}/> 필터</button></div></div>
      <div className="table-scroll"><table><thead><tr><th>회원</th><th>가입일</th><th>로그인 방식</th><th>여행 기록</th><th>최근 토큰 갱신</th><th>최근 활동</th><th /></tr></thead><tbody>{visible.map((member) => <tr key={member.id} onClick={() => onSelect(member)}><td><div className="member-cell"><div className={`avatar ${member.provider === "GUEST" ? "guest-avatar" : "member-avatar"}`}>{member.provider === "GUEST" ? "G" : member.name.slice(0, 1)}</div><div><strong>{member.name}</strong><span>{member.id}</span></div></div></td><td>{member.joinedAt}</td><td><ProviderBadge provider={member.provider}/></td><td><strong className="record-number">{member.records}</strong><span className="table-suffix">개</span></td><td><span className={member.refresh === "정상" ? "refresh-state" : "refresh-state failed"}><i/>{member.refresh}</span></td><td className="muted-cell">{member.lastActive}</td><td><button className="row-arrow" aria-label={`${member.id} 상세 보기`}><Icon name="chevron" size={15}/></button></td></tr>)}</tbody></table>{visible.length === 0 && <div className="empty-state">검색 결과가 없습니다.</div>}</div>
      <div className="table-footer"><span>총 <strong>1,284</strong>명 중 <strong>{visible.length}</strong>명 표시</span><div className="pagination"><button disabled>이전</button><button className="current-page">1</button><button onClick={() => onNotice("시연용 데이터는 1페이지입니다.")}>2</button><button onClick={() => onNotice("시연용 데이터는 1페이지입니다.")}>3</button><span>…</span><button onClick={() => onNotice("시연용 데이터는 1페이지입니다.")}>129</button><button onClick={() => onNotice("시연용 데이터는 1페이지입니다.")}>다음</button></div></div>
    </div><footer className="page-footer"><span>회원 정보는 운영 지원 목적으로만 확인해 주세요.</span><span>최근 토큰 갱신 정보는 시연용입니다.</span></footer></div>;
}

function FeedbackPage({ feedback, allFeedback, filter, setFilter, query, setQuery, onStatusChange }: { feedback: Feedback[]; allFeedback: Feedback[]; filter: string; setFilter: (filter: string) => void; query: string; setQuery: (query: string) => void; onStatusChange: (id: string, status: FeedbackStatus) => void }) {
  const counts = { "전체": allFeedback.length, "미확인": allFeedback.filter((item) => item.status === "미확인").length, "반영 중": allFeedback.filter((item) => item.status === "반영 중").length, "반영 완료": allFeedback.filter((item) => item.status === "반영 완료").length };
  return <div className="page-content list-page"><div className="page-heading-row"><div><div className="date-eyebrow">VOICE OF MAPMORY</div><h1>서비스 의견</h1><p>사용자의 목소리를 모아 더 나은 경험을 만들어 보세요.</p></div><button className="secondary-button" onClick={() => window.print()}><Icon name="download" size={16}/> 목록 인쇄</button></div>
    <div className="feedback-overview"><div className="feedback-overview-copy"><span className="overview-icon"><Icon name="chat" size={19}/></span><div><strong>이번 주 새로운 의견 <b>12건</b></strong><p>의견은 서비스 개선을 위해 내부에서만 관리됩니다.</p></div></div><span className="overview-date"><Icon name="calendar" size={15}/> 10.02 – 10.08</span></div>
    <div className="panel table-panel feedback-table-panel"><div className="feedback-toolbar"><div className="feedback-tabs">{["전체", "미확인", "반영 중", "반영 완료"].map((item) => <button key={item} className={filter === item ? "feedback-tab active" : "feedback-tab"} onClick={() => setFilter(item)}>{item}<span>{counts[item as keyof typeof counts]}</span></button>)}</div><div className="table-actions"><div className="search-field"><Icon name="search" size={17}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="의견 내용 검색" aria-label="의견 내용 검색"/></div><button className="filter-button"><Icon name="filter" size={16}/> 상세 필터</button></div></div>
      <div className="table-scroll"><table className="feedback-table"><thead><tr><th>접수일</th><th>유형</th><th className="content-column">의견 내용</th><th>OS</th><th>처리 상태</th><th /></tr></thead><tbody>{feedback.map((item) => <tr key={item.id}><td><span className="feedback-date">2026.{item.createdAt}</span><small>{item.id}</small></td><td><span className={`category-tag ${item.category === "오류 제보" ? "bug-category" : item.category === "기능 제안" ? "feature-category" : "other-category"}`}>{item.category}</span></td><td className="feedback-content-cell">{item.content}</td><td><span className="os-label"><span className="os-symbol">{item.platform === "iOS" ? "●" : "◉"}</span>{item.platform}</span></td><td><select className={`status-select status-${item.status.replaceAll(" ", "")}`} value={item.status} onChange={(event) => onStatusChange(item.id, event.target.value as FeedbackStatus)} aria-label={`${item.id} 처리 상태`}>{feedbackStatuses.map((status) => <option key={status}>{status}</option>)}</select></td><td><button className="row-arrow" aria-label={`${item.id} 의견 상세`}><Icon name="chevron" size={15}/></button></td></tr>)}</tbody></table>{feedback.length === 0 && <div className="empty-state">해당 상태의 의견이 없습니다.</div>}</div>
      <div className="table-footer"><span>총 <strong>{feedback.length}</strong>건</span><div className="pagination"><button disabled>이전</button><button className="current-page">1</button><button>다음</button></div></div>
    </div><div className="feedback-privacy-note"><span>✳</span><p><strong>의견은 내부 개선 목적으로만 사용됩니다.</strong> 제출한 사용자에게 개별 답변은 제공하지 않습니다.</p></div><footer className="page-footer"><span>Mapmory Admin <b>·</b> 의견 관리</span><span>접수일은 서버 기준 시각으로 저장됩니다.</span></footer></div>;
}

function MemberDrawer({ member, onClose }: { member: Member; onClose: () => void }) {
  return <><button className="drawer-backdrop" aria-label="상세 닫기" onClick={onClose}/><aside className="member-drawer"><div className="drawer-header"><div><span className="date-eyebrow">MEMBER DETAIL</span><h2>회원 상세</h2></div><button className="icon-button" onClick={onClose} aria-label="닫기"><Icon name="close"/></button></div><div className="drawer-member"><div className={`avatar drawer-avatar ${member.provider === "GUEST" ? "guest-avatar" : "member-avatar"}`}>{member.provider === "GUEST" ? "G" : member.name.slice(0, 1)}</div><div><h3>{member.name}</h3><span>{member.id}</span></div></div><div className="detail-section"><h3>계정 정보</h3><DetailRow label="가입일" value={member.joinedAt}/><DetailRow label="로그인 방식" value={member.provider === "GUEST" ? "게스트" : "OAuth · Kakao"}/><DetailRow label="여행 기록" value={`${member.records}개`}/></div><div className="detail-section"><h3>로그인 상태</h3><div className="session-card"><div className="session-heading"><span className={member.refresh === "정상" ? "refresh-state" : "refresh-state failed"}><i/>{member.refresh}</span><span className="session-pill">최근 확인</span></div><p>{member.refresh === "정상" ? "최근 토큰 갱신이 정상적으로 완료됐어요." : "토큰 갱신에 실패했어요. 사용자가 다시 로그인해야 할 수 있습니다."}</p><small>최근 활동 · {member.lastActive}</small></div><DetailRow label="활성 세션" value={member.refresh === "정상" ? "1개" : "확인 필요"}/></div><div className="drawer-note"><span>ⓘ</span> 로그인 상태는 시연용 데이터입니다. 실제 화면에서는 서버의 최근 리프레시 결과를 표시합니다.</div><button className="secondary-button drawer-close" onClick={onClose}>닫기</button></aside></>;
}

function DetailRow({ label, value }: { label: string; value: string }) { return <div className="detail-row"><span>{label}</span><strong>{value}</strong></div>; }
function ProviderBadge({ provider }: { provider: Member["provider"] }) { return <span className={`provider-badge ${provider === "GUEST" ? "provider-guest" : "provider-kakao"}`}><i/>{provider === "GUEST" ? "게스트" : "Kakao"}</span>; }
function StatusBadge({ status }: { status: FeedbackStatus }) { return <span className={`status-badge badge-${status.replaceAll(" ", "")}`}><i/>{status}</span>; }

export default App;
