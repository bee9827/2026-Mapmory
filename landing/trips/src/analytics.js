// Explicit Trips events contain no files, EXIF values, labels or raw errors/UAs.
// Separately verify the shared GA stream's automatic events before release.
export const CONSENT_KEY = 'mapmory_trips_analytics_consent_v1';
const INTERNAL_KEY = 'mapmory_internal_traffic_v1';
const count = value => Number.isInteger(value) && value >= 0 && value <= 100000;
const seconds = value => Number.isFinite(value) && value >= 0 && value <= 86400;
const oneOf = (...values) => value => values.includes(value);
const coverage = oneOf('none', 'some', 'all', 'unknown');
const picker = { picker_type: oneOf('photos', 'files') };
const grouping = { grouping_mode: oneOf('location', 'mixed', 'holding'), candidate_count: count, candidate_photo_count: count, other_photo_count: count };
const selection = { ...picker, selected_count: count, photo_count: count, oversized_count: count, non_photo_count: count };
const schemas = {
  trips_picker_open: picker,
  trips_selection_received: selection,
  trips_selection_rejected: { ...selection, reason: oneOf('no_photos', 'all_oversized', 'too_many', 'invalid_size') },
  trips_processing_start: selection,
  trips_processing_complete: { processing_seconds: seconds, gps_count: count, dated_count: count, usable_count: count, read_error_count: count, geo_data_available: value => typeof value === 'boolean' },
  trips_processing_failed: { processing_seconds: seconds },
  trips_processing_cancelled: { processing_seconds: seconds },
  trips_grouping_start: grouping,
  trips_results_view: grouping,
  trips_album_open: { album_kind: oneOf('trip', 'other'), album_photo_count: count, classification_method: oneOf('location', 'time', 'time_assisted', 'pattern', 'date', 'combined') },
  trips_review_edit: { ...grouping, edit_action: oneOf('merge', 'split', 'dismiss', 'undo', 'regenerate'), affected_count: count },
  trips_review_confirm: { ...grouping, edit_count: count, review_seconds: seconds },
  trips_archive_start: { archive_photo_count: count },
  trips_archive_ready: { archive_photo_count: count },
  trips_archive_failed: {},
  trips_archive_cancelled: {},
  trips_download_request: { archive_photo_count: count },
  trips_survey_click: {},
};
const contextSchema = {
  ...picker, photo_count: count,
  photo_bucket: oneOf('under_500', '500_plus'),
  gps_coverage: coverage, date_coverage: coverage,
  evaluation_group: oneOf('pending', 'android_non_kakao', 'no_usable_metadata', 'date_only', 'eligible'),
};
function selectSafe(schema, values) {
  return Object.fromEntries(Object.entries(schema).filter(([key, valid]) => valid(values[key])).map(([key]) => [key, values[key]]));
}
export function clientEnvironment(userAgent = '', touchPoints = 0) {
  const platform = /android/i.test(userAgent) ? 'android' : /iphone|ipad|ipod/i.test(userAgent) || (/macintosh/i.test(userAgent) && touchPoints > 1) ? 'ios' : 'other';
  const browserContext = /kakaotalk/i.test(userAgent) ? 'kakao' : /slack/i.test(userAgent) ? 'slack' : /chrome|crios/i.test(userAgent) ? 'chrome' : /safari/i.test(userAgent) ? 'safari' : 'other';
  return { platform, browser_context: browserContext, environment_eligible: platform !== 'android' || browserContext === 'kakao' };
}
function coverageOf(amount, total) { return !amount ? 'none' : amount === total ? 'all' : 'some'; }
export function metadataSummary(records, environment) {
  const total = records.length;
  const gps = records.filter(record => record.gps).length;
  const dated = records.filter(record => record.date).length;
  const usable = records.filter(record => record.gps && record.date).length;
  return {
    gps_count: gps, dated_count: dated, usable_count: usable,
    read_error_count: records.filter(record => record.readError).length,
    gps_coverage: coverageOf(gps, total), date_coverage: coverageOf(dated, total),
    evaluation_group: !environment.environment_eligible ? 'android_non_kakao' : usable ? 'eligible' : dated ? 'date_only' : 'no_usable_metadata',
  };
}
// Only campaign tokens chosen for this experiment are forwarded, never arbitrary URL text.
export function safePageLocation(href) {
  const incoming = new URL(href);
  const page = new URL('/trips/', incoming.origin);
  const allowed = { utm_source: ['wooteco', 'everytime'], utm_medium: ['community'], utm_campaign: ['trips_test'], utm_content: ['post_1', 'post_2'] };
  for (const [key, values] of Object.entries(allowed)) {
    const value = incoming.searchParams.get(key);
    if (values.includes(value)) page.searchParams.set(key, value);
  }
  return page.href;
}
function safeReferrer(value) {
  try { return new URL(value).origin; } catch { return ''; }
}
const denied = { analytics_storage: 'denied', ad_storage: 'denied', ad_user_data: 'denied', ad_personalization: 'denied' };

export function createAnalytics({ config, win = window, doc = document }) {
  const environment = clientEnvironment(win.navigator.userAgent, win.navigator.maxTouchPoints);
  const enabled = /^G-[A-Z0-9]+$/.test(config.measurementId) && (['map-mory.com', 'www.map-mory.com'].includes(win.location.hostname) || config.captureLocal === true);
  const read = key => { try { return win.localStorage.getItem(key); } catch { return null; } };
  const write = (key, value) => { try { win.localStorage.setItem(key, value); } catch {} };
  let consent = read(CONSENT_KEY), initialized = false, context = {}, seen = new Set();
  const internalQuery = new URL(win.location.href).searchParams.get('internal');
  if (internalQuery === '1' || internalQuery === '0') write(INTERNAL_KEY, internalQuery === '1' ? '1' : '0');
  const internal = config.captureLocal || (internalQuery === '1' || (internalQuery !== '0' && read(INTERNAL_KEY) === '1'));
  const base = { surface: 'trips', analytics_schema_version: '4', experiment_version: 'trips_v3', traffic_type: internal ? 'internal' : 'external', platform: environment.platform, browser_context: environment.browser_context, environment_group: environment.environment_eligible ? 'standard' : 'android_non_kakao' };
  const commonPage = { page_location: safePageLocation(win.location.href), page_referrer: safeReferrer(doc.referrer), page_title: '사진 정리하기 · Mapmory' };
  function gtag() { (win.dataLayer ??= []).push(arguments); }
  function initialize() {
    if (!enabled || consent !== 'granted' || initialized) return;
    try {
      win[`ga-disable-${config.measurementId}`] = false;
      gtag('consent', 'default', denied);
      gtag('consent', 'update', { ...denied, analytics_storage: 'granted' });
      gtag('js', new Date());
      gtag('config', config.measurementId, {
        ...base, ...commonPage, send_page_view: false,
        allow_google_signals: false, allow_ad_personalization_signals: false,
        ...(config.debug ? { debug_mode: true } : {}),
      });
      gtag('event', 'page_view', { ...base, ...commonPage, send_to: config.measurementId });
      const script = doc.createElement('script');
      script.async = true;
      script.src = `https://www.googletagmanager.com/gtag/js?id=${config.measurementId}`;
      doc.head.append(script);
      initialized = true;
    } catch { /* Analytics is optional and must never break photo processing. */ }
  }
  function track(event, values = {}, onceKey) {
    if (!enabled || consent !== 'granted' || !initialized || !Object.hasOwn(schemas, event) || (onceKey && seen.has(onceKey))) return false;
    try {
      // A new picker/selection may follow an earlier successful run. Never attach stale metadata.
      const flowContext = ['trips_picker_open', 'trips_selection_received', 'trips_selection_rejected'].includes(event) ? {} : context;
      gtag('event', event, { ...base, ...commonPage, ...flowContext, ...selectSafe(schemas[event], values), send_to: config.measurementId });
      if (onceKey) seen.add(onceKey);
      return true;
    } catch { return false; }
  }
  initialize();
  return {
    enabled, environment, track,
    get consent() { return consent; },
    setConsent(value) {
      if (!['granted', 'denied'].includes(value)) return;
      consent = value; write(CONSENT_KEY, value);
      if (value === 'granted') {
        if (initialized) {
          win[`ga-disable-${config.measurementId}`] = false;
          gtag('consent', 'update', { ...denied, analytics_storage: 'granted' });
        } else initialize();
      } else if (initialized) {
        win[`ga-disable-${config.measurementId}`] = true;
        gtag('consent', 'update', denied);
      }
    },
    setContext(values) { context = selectSafe(contextSchema, { ...context, ...values }); },
    resetFlow(values) { seen = new Set(); context = selectSafe(contextSchema, values); },
  };
}
