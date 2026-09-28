import { formatBytes, formatNumber } from './organize.js';

export function createPhotoViewer(getFile) {
  let dialog, stage, title, caption, counter, previous, next;
  let records = [], position = 0, objectUrl = null, generation = 0, opener = null, touchStart = null;
  let historyToken = null, previousHistoryState = null;

  function node(tag, className, text) {
    const value = document.createElement(tag);
    value.className = className;
    if (text !== undefined) value.textContent = text;
    return value;
  }
  function control(label, path, action) {
    const button = node('button', 'viewer-control'); button.type = 'button';
    button.setAttribute('aria-label', label); button.title = label;
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    for (const [key, value] of Object.entries({ viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', 'aria-hidden': 'true' })) svg.setAttribute(key, value);
    const line = document.createElementNS('http://www.w3.org/2000/svg', 'path'); line.setAttribute('d', path); svg.append(line);
    button.append(svg); button.addEventListener('click', action); return button;
  }
  function releaseImage() {
    generation++;
    stage?.replaceChildren();
    if (objectUrl) URL.revokeObjectURL(objectUrl);
    objectUrl = null;
  }
  function hide() {
    if (dialog?.open) dialog.close();
    releaseImage(); document.body.classList.remove('viewer-open');
    if (opener?.isConnected) opener.focus({ preventScroll: true });
  }
  function requestClose() {
    hide();
    if (historyToken && history.state?.photoViewer === historyToken) history.back();
  }
  function move(delta) {
    const target = position + delta;
    if (target < 0 || target >= records.length) return;
    position = target; paint();
  }
  function initialize() {
    if (dialog) return;
    dialog = node('dialog', 'photo-viewer'); dialog.setAttribute('aria-labelledby', 'viewer-title');
    const header = node('header', 'viewer-header');
    title = node('h2', 'viewer-title'); title.id = 'viewer-title';
    header.append(title, control('사진 뷰어 닫기', 'M6 6l12 12M18 6 6 18', requestClose));
    stage = node('div', 'viewer-stage'); stage.setAttribute('aria-live', 'polite');
    const footer = node('footer', 'viewer-footer');
    caption = node('p', 'viewer-caption');
    const navigation = node('div', 'viewer-navigation');
    previous = control('이전 사진', 'm14 5-7 7 7 7', () => move(-1));
    next = control('다음 사진', 'm10 5 7 7-7 7', () => move(1));
    counter = node('p', 'viewer-counter'); counter.setAttribute('role', 'status');
    navigation.append(previous, counter, next); footer.append(caption, navigation);
    dialog.append(header, stage, footer); document.body.append(dialog);
    dialog.addEventListener('cancel', event => { event.preventDefault(); requestClose(); });
    dialog.addEventListener('keydown', event => {
      if (event.altKey || event.metaKey || event.ctrlKey) return;
      if (event.key === 'ArrowLeft') { event.preventDefault(); move(-1); }
      if (event.key === 'ArrowRight') { event.preventDefault(); move(1); }
    });
    stage.addEventListener('touchstart', event => { touchStart = event.touches.length === 1 ? { x: event.touches[0].clientX, y: event.touches[0].clientY } : null; }, { passive: true });
    stage.addEventListener('touchmove', event => { if (event.touches.length > 1) touchStart = null; }, { passive: true });
    stage.addEventListener('touchcancel', () => { touchStart = null; });
    stage.addEventListener('touchend', event => {
      if (!touchStart || event.touches.length || event.changedTouches.length !== 1) return;
      const dx = event.changedTouches[0].clientX - touchStart.x, dy = event.changedTouches[0].clientY - touchStart.y;
      touchStart = null;
      // Keep pinch/zoom gestures native; only a clearly horizontal swipe changes photos.
      if ((window.visualViewport?.scale ?? 1) <= 1 && Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.5) move(dx < 0 ? 1 : -1);
    }, { passive: true });
    window.addEventListener('popstate', () => {
      if (historyToken && history.state?.photoViewer === historyToken && records.length) {
        if (!dialog.open) { dialog.showModal(); document.body.classList.add('viewer-open'); paint(); }
      } else if (dialog.open) hide();
    });
    window.addEventListener('pagehide', () => { if (dialog.open) hide(); });
  }
  function paint() {
    releaseImage();
    const revision = generation;
    const record = records[position];
    const file = getFile(record.index);
    title.textContent = record.name;
    caption.textContent = [record.country, record.city, record.date ? `${record.date.day} ${record.date.time}` : '촬영일 정보 없음', formatBytes(record.size)].filter(Boolean).join(' · ');
    counter.textContent = `${formatNumber(position + 1)} / ${formatNumber(records.length)}`;
    previous.disabled = position === 0; next.disabled = position === records.length - 1;
    const loading = node('p', 'viewer-message', '사진을 불러오는 중…');
    stage.append(loading); stage.setAttribute('aria-busy', 'true');
    if (!file) { loading.textContent = '사진을 다시 선택해 주세요.'; stage.setAttribute('aria-busy', 'false'); return; }
    objectUrl = URL.createObjectURL(file);
    const image = node('img', 'viewer-image'); image.alt = record.name; image.decoding = 'async'; image.hidden = true;
    image.addEventListener('load', () => {
      if (revision !== generation) return;
      loading.remove(); image.hidden = false; stage.setAttribute('aria-busy', 'false');
    }, { once: true });
    image.addEventListener('error', () => {
      if (revision !== generation) return;
      const error = node('div', 'viewer-fallback');
      error.append(node('p', '', '이 사진은 브라우저에서 미리 볼 수 없어요.'), node('p', 'viewer-fallback-detail', 'HEIC·RAW 등 일부 형식이나 손상된 파일일 수 있어요. 원본은 ZIP에 그대로 포함돼요.'));
      const download = node('a', 'button viewer-save-original', '원본 저장하기');
      download.href = objectUrl; download.download = record.name;
      error.append(download); stage.replaceChildren(error); stage.setAttribute('aria-busy', 'false');
    }, { once: true });
    image.src = objectUrl; stage.append(image);
  }
  return {
    open(photoRecords, fileIndex) {
      const selected = photoRecords.findIndex(record => record.index === fileIndex);
      if (selected < 0) return;
      initialize(); records = photoRecords; position = selected;
      if (!dialog.open) {
        opener = document.activeElement;
        previousHistoryState = history.state;
        historyToken = `photo-${Date.now()}-${fileIndex}`;
        history.pushState({ photoViewer: historyToken }, '', location.href);
        dialog.showModal(); document.body.classList.add('viewer-open');
      }
      paint();
    },
    reset() {
      if (historyToken && history.state?.photoViewer === historyToken) history.replaceState(previousHistoryState, '', location.href);
      if (dialog?.open) hide();
      records = []; historyToken = null;
    },
  };
}
