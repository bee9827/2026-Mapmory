const environment = import.meta.env ?? {};
const measurementId = environment.VITE_GA_MEASUREMENT_ID?.trim()
  || (environment.PROD ? "G-MC93CZWLZF" : "");
const landingVersion = environment.VITE_LANDING_VERSION?.trim() || "v2";

export const ANALYTICS_EVENTS = Object.freeze({
  EXPERIENCE_CTA_CLICK: "experience_cta_click",
  EXPERIENCE_VIEW: "experience_view",
  EXPERIENCE_START: "experience_start",
  MEMORY_OPEN: "memory_open",
  KOREA_MEMORY_ADD: "korea_memory_add",
  EXPERIENCE_END: "experience_end",
  WAITLIST_CTA_CLICK: "waitlist_cta_click",
  WAITLIST_FORM_VIEW: "waitlist_form_view",
  WAITLIST_FORM_START: "waitlist_form_start",
  WAITLIST_SUBMIT_ATTEMPT: "waitlist_submit_attempt",
  WAITLIST_SUBMIT: "waitlist_submit",
  WAITLIST_SUBMIT_ERROR: "waitlist_submit_error",
  DOWNLOAD_CLICK: "download_click",
});

const supportedEvents = new Set(Object.values(ANALYTICS_EVENTS));
const forbiddenParameterPattern = /(email|phone|name|address|message|free.?text)/i;
const supportedParameters = new Set([
  "experience_type",
  "interaction_type",
  "memory_id",
  "selection_source",
  "cta_placement",
  "open_index",
  "add_index",
  "time_since_start_ms",
  "active_duration_ms",
  "unique_memories_opened",
  "last_completed_step",
  "exit_reason",
  "attempt_number",
  "result",
  "error_type",
  "validation_field",
  "transport_type",
]);

let initialized = false;

export function initializeAnalytics() {
  if (!measurementId || initialized || typeof window === "undefined") return;

  initialized = true;
  window.dataLayer = window.dataLayer || [];
  window.gtag = function gtag() {
    window.dataLayer.push(arguments);
  };

  window.gtag("js", new Date());
  window.gtag("config", measurementId, {
    anonymize_ip: true,
    send_page_view: true,
    landing_version: landingVersion,
  });

  const script = document.createElement("script");
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(measurementId)}`;
  document.head.appendChild(script);
}

export function buildEventParameters(parameters = {}) {
  const safeParameters = Object.fromEntries(
    Object.entries(parameters).filter(([key, value]) => (
      supportedParameters.has(key)
      && !forbiddenParameterPattern.test(key)
      && value !== undefined
      && value !== null
      && value !== ""
    )),
  );

  return {
    landing_version: landingVersion,
    ...safeParameters,
  };
}

export function isSupportedEvent(name) {
  return supportedEvents.has(name);
}

export function trackEvent(name, parameters = {}) {
  if (!isSupportedEvent(name)) return false;
  if (!measurementId || typeof window === "undefined" || typeof window.gtag !== "function") return false;
  window.gtag("event", name, buildEventParameters(parameters));
  return true;
}
