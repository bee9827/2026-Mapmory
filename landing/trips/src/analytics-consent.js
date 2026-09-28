// A non-modal choice, outside #app, survives photo/result screen transitions.
export function mountAnalyticsConsent(analytics, doc = document) {
  if (!analytics.enabled) return;
  const section = doc.createElement('section');
  section.className = 'analytics-consent';
  section.setAttribute('aria-label', '사용 통계 설정');
  doc.body.prepend(section);
  function render(editing = !analytics.consent) {
    section.replaceChildren();
    const text = doc.createElement('p');
    text.textContent = editing
      ? '실험 개선을 위한 사용 통계를 허용할까요? Google Analytics가 쿠키를 사용해 사진 수, 처리 시간, 정보 누락 여부와 클릭 단계를 측정해요. 사진·파일명·촬영일·GPS는 보내지 않아요. 거절해도 모든 기능을 이용할 수 있어요.'
      : `사용 통계 ${analytics.consent === 'granted' ? '허용 중' : '수집 안 함'} · 사진은 기기 안에서만 처리돼요.`;
    section.append(text);
    const actions = doc.createElement('div'); actions.className = 'analytics-consent-actions';
    const addButton = (label, action) => {
      const button = doc.createElement('button'); button.type = 'button';
      button.className = 'button button-secondary'; button.textContent = label;
      button.addEventListener('click', action); actions.append(button);
    };
    if (editing) {
      addButton('허용 안 함', () => { analytics.setConsent('denied'); render(false); focus(); });
      addButton('통계 허용', () => { analytics.setConsent('granted'); render(false); focus(); });
    } else addButton('통계 설정 변경', () => { render(true); focus(); });
    section.append(actions);
  }
  function focus() { section.querySelector('button')?.focus({ preventScroll: true }); }
  render();
}
