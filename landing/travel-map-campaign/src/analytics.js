import { CAMPAIGN_ID } from "./campaignConfig.js";

const environment = import.meta.env ?? {};
export function resolveMeasurementId(config = {}) {
  return config.VITE_GA_MEASUREMENT_ID?.trim() || "";
}

const measurementId = resolveMeasurementId(environment);
const campaignVersion = environment.VITE_CAMPAIGN_VERSION?.trim() || "travel-map-v1";
const forbiddenParameterPattern = /(email|phone|name|address|message|free.?text)/i;

export const INTERNAL_TRAFFIC_STORAGE_KEY = "mapmory_internal_traffic_v1";

export function resolveTrafficType({ search = "", storage = null } = {}) {
  const internalMode = new URLSearchParams(search).get("internal");

  if (internalMode === "1") {
    try { storage?.setItem(INTERNAL_TRAFFIC_STORAGE_KEY, "1"); } catch { /* Current visit still counts as internal. */ }
    return "internal";
  }

  if (internalMode === "0") {
    try { storage?.removeItem(INTERNAL_TRAFFIC_STORAGE_KEY); } catch { /* Explicit reset still applies. */ }
    return "external";
  }

  try { return storage?.getItem(INTERNAL_TRAFFIC_STORAGE_KEY) === "1" ? "internal" : "external"; }
  catch { return "external"; }
}

function resolveBrowserTrafficType() {
  if (typeof window === "undefined") return "external";
  let storage = null;
  try { storage = window.localStorage; } catch { /* Storage may be unavailable. */ }
  return resolveTrafficType({ search: window.location.search, storage });
}

const trafficType = resolveBrowserTrafficType();
let gaInitialized = false;

export function sanitizeEventProperties(properties = {}) {
  return Object.fromEntries(Object.entries(properties).filter(([key, value]) => (
    !forbiddenParameterPattern.test(key)
    && value !== undefined
    && value !== null
    && value !== ""
    && ["string", "number", "boolean"].includes(typeof value)
  )));
}

export function initializeCampaignAnalytics() {
  if (!measurementId || gaInitialized || typeof window === "undefined") return false;

  gaInitialized = true;
  window.dataLayer = window.dataLayer || [];
  window.gtag = window.gtag || function gtag() { window.dataLayer.push(arguments); };
  window.gtag("js", new Date());
  window.gtag("config", measurementId, {
    anonymize_ip: true,
    send_page_view: true,
    campaign_name: CAMPAIGN_ID,
    campaign_version: campaignVersion,
    traffic_type: trafficType,
  });

  const script = document.createElement("script");
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(measurementId)}`;
  document.head.appendChild(script);
  return true;
}

export function trackCampaignEvent(eventName, properties = {}) {
  const eventProperties = {
    campaign_name: CAMPAIGN_ID,
    campaign_version: campaignVersion,
    traffic_type: trafficType,
    ...sanitizeEventProperties(properties),
  };

  let tracked = false;
  if (measurementId && gaInitialized && typeof window !== "undefined" && typeof window.gtag === "function") {
    window.gtag("event", eventName, eventProperties);
    tracked = true;
  }
  if (typeof window !== "undefined" && window.posthog?.capture) {
    window.posthog.capture(eventName, { ...eventProperties, $geoip_disable: true });
    tracked = true;
  }
  return tracked;
}
