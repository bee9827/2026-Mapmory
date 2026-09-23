import { proposeTrips, candidateMethod } from './trip-candidates.js';
import { estimatePatterns, lacksLocation } from './pattern-experiment.js';

const labels = { location: '관측 · 위치와 날짜', time_assisted: '관측 + 추정 · 촬영 시간으로 연결',
  pattern: '추정 · 촬영량 증가', date: '판단 보류 · 날짜별 묶음', combined: '여러 근거가 포함된 묶음', time: '추정 · 촬영 시간으로 연결' };
export function candidateTitle(group) {
  const places = [...new Set(group.photos.filter(p => group.basis[p.index] === 'location').map(p => p.city || p.location).filter(Boolean))];
  if (places.length) return `${places.slice(0, 2).join(' · ')}${places.length > 2 ? ' 외' : ''} 방문 묶음`;
  return candidateMethod(group) === 'pattern' ? '사진이 몰린 기간' : candidateMethod(group) === 'date' ? '날짜별 사진 묶음' : '같은 지역의 사진 묶음';
}

// Read-only experiment: results first, survey below. Originals and ZIP stay unchanged.
export function createTripReview({ records, root, photoAlbum, clearAlbums, onBack, onChoose, createNotice, createSurvey,
  analytics, doc = document }) {
  const review = proposeTrips(records);
  const groups = review.groups.filter(group => candidateMethod(group) !== 'date');
  // Uncertain photos are one holding album, not many apparent trips split by date.
  const pending = [...review.groups.filter(group => candidateMethod(group) === 'date').flatMap(group => group.photos), ...review.other];
  let shown = 12;
  let experiment = null;
  function el(tag, className = '', text) {
    const node = doc.createElement(tag); node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }
  function button(text, action, className = 'button button-secondary') {
    const node = el('button', className, text); node.type = 'button'; node.addEventListener('click', action); return node;
  }
  function totals() {
    return { candidate_count: groups.length, candidate_photo_count: groups.reduce((n, g) => n + g.photos.length, 0),
      other_photo_count: pending.length, grouping_mode: review.mode };
  }
  function patternSection() {
    const section = el('section', 'pattern-note'); section.setAttribute('aria-label', '선택형 촬영 패턴 실험');
    section.append(el('h2', '', '🧪 위치 없는 사진에서도 여행 찾아보기'),
      el('p', '', '일상 사진이 함께 있으면 촬영량 변화를 비교하는 데 도움이 돼요. 여행 사진만 선택했다면 추정이 어려울 수 있어요.'),
      el('p', 'notice-detail', '다시 선택할 필요 없이, 이미 고른 사진만 사용해요. 기본 분류와 보류 앨범은 그대로 유지돼요.'));
    const output = el('div', 'pattern-output');
    const renderOutput = () => {
      const messages = { ready: '촬영량이 늘어난 기간을 찾았어요. 실제 여행과 다를 수 있어요.',
        insufficient: '비교 근거가 부족해요. 사진은 그대로 보류했어요.',
        no_dates: '촬영일을 확인할 수 없어 사진을 그대로 보류했어요.',
        no_burst: '기준을 넘는 촬영량 증가가 없어 사진을 그대로 보류했어요. 여행이 없었다는 뜻은 아니에요.' };
      const status = el('h3', 'pattern-status', messages[experiment.status]); status.tabIndex = -1;
      output.replaceChildren(status, el('p', 'notice-detail', '아래는 보류 앨범 중 일부를 다시 묶어본 실험 결과예요. 기존 앨범이나 ZIP을 변경하지 않아요.'));
      for (const group of experiment.groups) output.append(photoAlbum({ title: '사진이 몰린 기간', photos: group.photos,
        label: '실험 · 촬영 패턴으로 추정', onExpand: () => analytics.track('trips_pattern_album_open',
          { album_photo_count: group.photos.length }, 'pattern_album_open') }));
      output.append(el('p', '', `추정 ${experiment.groups.length}묶음 · 계속 보류 ${experiment.remaining.length}장`));
      const stats = experiment.stats, details = el('details', 'review-legend');
      details.append(el('summary', '', '어떤 기준으로 추정했나요?'), el('p', '',
        `선택한 사진의 촬영일 ${stats.active_days}일 · 기간 ${stats.span_days}일 · 선택한 사진이 없는 날짜 ${stats.empty_selected_days}일. 촬영일별 평균 ${stats.daily_mean}장, 중앙값 ${stats.daily_median}장, 최대 ${stats.daily_max}장.`),
        el('p', '', `급증 기준: 중앙값의 3배 이상이면서 하루 10장 이상(현재 ${stats.burst_threshold}장). 보류된 위치 없는 사진 중 기준을 넘은 날짜 ${stats.burst_day_count}일.`),
        el('p', 'notice-detail', '14일 이상 범위와 촬영일 7일 이상, 기준 미만 촬영일 5일 이상일 때만 추정해요. 빈 날짜 수는 첫 촬영일~마지막 촬영일 사이의 선택 공백이며 실제로 촬영하지 않은 날이라는 뜻은 아니에요. 공백을 0장으로 평균에 넣지 않아요. 실제 일상 포함 여부나 평소 촬영량을 확인한 값은 아니에요.'));
      output.append(details);
      return status;
    };
    if (experiment) renderOutput();
    else {
      const run = button('촬영 패턴으로 찾아보기', () => {
        if (experiment) return;
        run.disabled = true;
        analytics.track('trips_pattern_start', { target_photo_count: pending.filter(lacksLocation).length }, 'pattern_start');
        experiment = estimatePatterns(records, pending);
        const status = renderOutput(); run.hidden = true;
        analytics.track('trips_pattern_stats', { ...experiment.stats, pattern_status: experiment.status }, 'pattern_stats');
        analytics.track('trips_pattern_result', { pattern_status: experiment.status, candidate_count: experiment.groups.length,
          candidate_photo_count: experiment.groups.reduce((n, g) => n + g.photos.length, 0), other_photo_count: experiment.remaining.length }, 'pattern_result');
        status.focus({ preventScroll: true });
      });
      section.append(run);
    }
    section.append(output); return section;
  }
  function render() {
    clearAlbums(); root.replaceChildren(); root.className = 'result-screen trip-review trip-results';
    const title = el('h1', '', groups.length ? `${groups.length}개 여행 후보를 찾았어요` : '분류 보류 사진을 한곳에 모았어요');
    title.tabIndex = -1;
    root.append(el('p', 'flow-label', '여행 묶어서 보기'), title,
      el('p', 'result-description', `${records.length}장의 사진 · 앨범을 누르면 사진을 펼쳐볼 수 있어요.`));
    if (review.mode === 'holding') {
      root.append(el('p', 'result-caveat', '위치와 촬영일을 함께 확인할 수 없어 분류를 보류했어요. 촬영량 추정은 맨 아래 실험 기능에서 선택할 수 있어요.'));
    }
    const list = el('div', 'album-list classified-albums'); list.setAttribute('aria-label', '분류된 사진 묶음');
    const addGroup = group => photoAlbum({ title: candidateTitle(group), photos: group.photos, label: labels[candidateMethod(group)],
      onExpand: () => analytics.track('trips_album_open', { album_kind: 'trip', album_photo_count: group.photos.length,
        classification_method: candidateMethod(group) }, 'trip_album_open') });
    for (const group of groups.slice(0, shown)) list.append(addGroup(group));
    const more = button('묶음 더 보기', () => {
      const next = Math.min(shown + 12, groups.length);
      for (const group of groups.slice(shown, next)) list.append(addGroup(group));
      shown = next; more.hidden = shown >= groups.length;
    }, 'button button-secondary album-more'); more.hidden = shown >= groups.length;
    root.append(list, more);
    if (pending.length) root.append(photoAlbum({ title: '분류 보류', photos: pending,
      label: '여행 판단 근거 부족 · 한곳에 모음', onExpand: () => analytics.track('trips_album_open', { album_kind: 'other', album_photo_count: pending.length }, 'other_album_open') }));
    if (!records.length) root.append(el('p', 'notice', '선택한 사진이 없어요. 사진을 선택해주세요.'));
    // Keep exclusions visible, but never put controls or long explanations before albums.
    if (createNotice) root.append(createNotice());
    root.append(createSurvey());
    const details = el('details', 'review-legend');
    details.append(el('summary', '', '어떻게 분류했나요?'),
      el('p', '', '위치·날짜가 확인된 사진과 그 사이의 촬영 시간으로 연결한 사진을 구분해요. 여행 확정이 아닌 자동 제안이며 일상·출장도 포함될 수 있어요. 촬영량 추정은 맨 아래 실험 버튼을 눌렀을 때만 실행해요.'),
      el('p', 'notice-detail', '위치 기준: 첫 사진에서 30km 이내, 연속된 촬영 날짜, 사진 간 36시간 이내. 위치 없는 사진은 같은 방문의 앞뒤 GPS 사진 사이이며 가까운 기준 사진과 6시간 이내일 때만 연결해요. 해외 시간대는 보정하지 않아요.'));
    root.append(details, el('p', 'notice-detail', '사진은 기기 안에서 처리해요. 새로고침하면 결과가 사라지며, 원본과 위치·날짜별 ZIP은 변경되지 않아요.'),
      button('원본 위치·날짜별 보기 / ZIP', onBack));
    if (onChoose) root.append(button('다른 사진 정리하기', onChoose, 'button button-text'));
    if (pending.some(lacksLocation)) root.append(patternSection());
    title.focus({ preventScroll: true });
    root.scrollIntoView?.({ block: 'start' });
  }
  return { open() {
    analytics.track('trips_grouping_start', { grouping_mode: review.mode }, 'grouping_start');
    render(); analytics.track('trips_results_view', totals(), 'initial_candidates');
  } };
}
