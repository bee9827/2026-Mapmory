import { formatNumber as n } from './organize.js';

function el(tag, className, text) {
  const node = document.createElement(tag);
  node.className = className;
  if (text !== undefined) node.textContent = text;
  return node;
}

// Object URLs live only as long as the result screen. No upload or conversion.
export function createAlbumResources(getFile) {
  const urls = new Map();
  return {
    url(record) {
      if (!urls.has(record.index)) urls.set(record.index, URL.createObjectURL(getFile(record.index)));
      return urls.get(record.index);
    },
    clear() { for (const url of urls.values()) URL.revokeObjectURL(url); urls.clear(); },
  };
}

export function albumDateRange(photos) {
  const days = photos.map(p => p.date?.day).filter(Boolean).sort();
  return days.length ? (days[0] === days.at(-1) ? days[0] : `${days[0]} – ${days.at(-1)}`) : '촬영 날짜 정보 없음';
}

export function coverPhoto(photos) {
  // Prefer a broadly previewable original, without analyzing photo content.
  return photos.find(p => /\.(jpe?g|png|webp|avif)$/i.test(p.name)) ?? photos[0];
}

export function createPhotoAlbum({ title, photos, label, onSelect }, { resources, onOpen }) {
  const album = el('details', 'photo-album');
  const summary = el('summary', 'album-summary');
  const stack = el('span', 'album-stack'); stack.setAttribute('aria-hidden', 'true');
  const representative = coverPhoto(photos);
  const image = (record, className) => {
    const img = el('img', className); img.alt = ''; img.loading = 'lazy'; img.decoding = 'async';
    img.src = resources.url(record);
    return img;
  };
  if (representative) {
    // The same cover on each layer: only one representative is revealed while closed.
    for (let i = 0; i < 3; i++) {
      const img = image(representative, `album-cover album-cover-${i}`);
      img.addEventListener('error', () => {
        img.hidden = true;
        if (i === 2) stack.append(el('span', 'album-preview-unavailable', '미리보기 불가'));
      }, { once: true });
      stack.append(img);
    }
  }
  const copy = el('span', 'album-copy');
  if (label) copy.append(el('span', 'album-label', label));
  copy.append(el('span', 'album-title', title), el('span', 'album-meta', albumDateRange(photos)), el('span', 'album-meta', `${n(photos.length)}장의 사진`));
  const hint = el('span', 'album-toggle', '사진 펼쳐보기');
  summary.append(stack, copy, hint); album.append(summary);
  const body = el('div', 'album-body'); body.hidden = true; album.append(body);
  let shown = 0;
  const grid = el('div', 'album-photo-grid');
  const more = el('button', 'button button-secondary album-more'); more.type = 'button';
  function appendPhotos() {
    for (const record of photos.slice(shown, shown + 24)) {
      const tile = el('button', 'album-photo'); tile.type = 'button';
      tile.setAttribute('aria-label', `${record.name} 크게 보기`); tile.setAttribute('aria-haspopup', 'dialog');
      const img = image(record, 'album-thumbnail');
      img.addEventListener('error', () => {
        img.remove(); tile.classList.add('album-photo-unavailable');
        tile.append(el('span', '', '미리보기 불가'), el('span', 'album-file-name', record.name));
      }, { once: true });
      tile.append(img); tile.addEventListener('click', () => onOpen(photos, record.index)); grid.append(tile);
    }
    shown = Math.min(photos.length, shown + 24);
    more.hidden = shown === photos.length;
    more.textContent = `사진 더 보기 · ${n(photos.length - shown)}장 남음`;
  }
  more.addEventListener('click', appendPhotos);
  body.append(grid, more);
  if (onSelect) {
    const select = el('button', 'button button-trip album-select', '이곳을 생활 지역으로 선택'); select.type = 'button';
    select.addEventListener('click', onSelect); body.append(select);
  }
  album.addEventListener('toggle', () => {
    hint.textContent = album.open ? '사진 접기' : '사진 펼쳐보기';
    body.hidden = !album.open;
    if (album.open && !shown) appendPhotos();
  });
  return album;
}
