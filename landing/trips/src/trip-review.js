import { proposeTrips, candidateMethod, TRIP_RULES } from './trip-candidates.js';

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
  function render() {
    clearAlbums(); root.replaceChildren(); root.className = 'result-screen trip-review trip-results';
    const title = el('h1', '', groups.length ? `${groups.length}개 여행 후보를 찾았어요` : '분류 보류 사진을 한곳에 모았어요');
    title.tabIndex = -1;
    root.append(el('p', 'flow-label', '여행 묶어서 보기'), title,
      el('p', 'result-description', `${records.length}장의 사진 · 앨범을 누르면 사진을 펼쳐볼 수 있어요.`));
    if (review.pattern) {
      const messages = {
        ready: '선택한 사진에서 촬영량이 늘어난 기간을 추정했어요. 실제 여행이 아닐 수도 있어요.',
        insufficient: '비교할 기록이 부족한 사진은 분류 보류 앨범에 모았어요.',
        no_burst: '뚜렷한 촬영량 증가가 없어 분류를 보류했어요. 여행이 없었다는 뜻은 아니에요.',
        no_dates: '촬영일이 없는 사진은 아래에 모았어요. 원본 파일의 날짜 정보를 확인해주세요.',
      };
      root.append(el('p', 'result-caveat', messages[review.pattern.status]));
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
      el('p', '', '위치·날짜가 확인된 사진과 촬영 시간·촬영량으로 추정한 사진을 구분해요. 여행 확정이 아닌 자동 제안이며 일상·출장도 포함될 수 있어요.'),
      el('p', 'notice-detail', '위치 기준: 첫 사진에서 30km 이내, 연속된 촬영 날짜, 사진 간 36시간 이내. 위치 없는 사진은 같은 방문의 앞뒤 GPS 사진 사이이며 가까운 기준 사진과 6시간 이내일 때만 연결해요. 해외 시간대는 보정하지 않아요.'));
    if (review.pattern) {
      const p = review.pattern;
      details.append(el('p', 'notice-detail', `촬영량 실험값: ${TRIP_RULES.baselineSpanDays}일 이상 범위 · ${TRIP_RULES.baselineActiveDays}개 이상 촬영일 · 비급증일 ${TRIP_RULES.baselineOrdinaryDays}개 이상. 촬영일별 장수 중앙값의 ${TRIP_RULES.burstRatio}배 이상이며 하루 ${TRIP_RULES.burstMinPhotos}장 이상. 현재 ${p.spanDays}일 범위 · 촬영일 ${p.activeDays}일.`),
        el('p', 'notice-detail', '선택한 사진만 비교하므로 실제 평소 촬영량을 알 수는 없어요. 선택하지 않은 날을 0장으로 보지 않으며, 여행 사진만 고르면 추정이 달라질 수 있어요.'));
    }
    root.append(details, el('p', 'notice-detail', '사진은 기기 안에서 처리해요. 새로고침하면 결과가 사라지며, 원본과 위치·날짜별 ZIP은 변경되지 않아요.'),
      button('원본 위치·날짜별 보기 / ZIP', onBack));
    if (onChoose) root.append(button('다른 사진 정리하기', onChoose, 'button button-text'));
    title.focus({ preventScroll: true });
    root.scrollIntoView?.({ block: 'start' });
  }
  return { open() {
    analytics.track('trips_grouping_start', { grouping_mode: review.mode }, 'grouping_start');
    render(); analytics.track('trips_results_view', totals(), 'initial_candidates');
  } };
}
