import assert from "node:assert/strict";
import test from "node:test";
import { APP_ACQUISITION_URL, CAMPAIGN_LANDING_URL, GOOGLE_PLAY_PACKAGE_ID, MAPMORY_DOMAIN_LABEL, MAPMORY_SITE_ORIGIN } from "../src/campaignConfig.js";
import { resolveTrafficType, sanitizeEventProperties } from "../src/analytics.js";

test("uses the official Google Play listing for app acquisition", () => {
  const destination = new URL(APP_ACQUISITION_URL);
  assert.equal(MAPMORY_SITE_ORIGIN, "https://map-mory.com");
  assert.equal(MAPMORY_DOMAIN_LABEL, "map-mory.com");
  assert.equal(GOOGLE_PLAY_PACKAGE_ID, "com.mapmory.android");
  assert.equal(destination.hostname, "play.google.com");
  assert.equal(destination.searchParams.get("id"), GOOGLE_PLAY_PACKAGE_ID);
  const referrer = new URLSearchParams(destination.searchParams.get("referrer"));
  assert.equal(referrer.get("utm_source"), "travel_map_campaign");
  assert.equal(referrer.get("utm_medium"), "web_campaign");
  assert.equal(referrer.get("utm_campaign"), "2026_travel_map");
  assert.equal(referrer.get("utm_content"), "demand_primary");
});

test("attributes the lower-commitment Mapmory landing path separately", () => {
  const destination = new URL(CAMPAIGN_LANDING_URL);
  assert.equal(destination.origin, MAPMORY_SITE_ORIGIN);
  assert.equal(destination.pathname, "/");
  assert.equal(destination.searchParams.get("utm_source"), "travel_map_campaign");
  assert.equal(destination.searchParams.get("utm_medium"), "web_campaign");
  assert.equal(destination.searchParams.get("utm_campaign"), "2026_travel_map");
  assert.equal(destination.searchParams.get("utm_content"), "demand_secondary");
});

test("marks internal campaign traffic without collecting personal fields", () => {
  const values = new Map();
  const storage = {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
    removeItem: (key) => values.delete(key),
  };
  assert.equal(resolveTrafficType({ search: "?internal=1", storage }), "internal");
  assert.equal(resolveTrafficType({ storage }), "internal");
  assert.equal(resolveTrafficType({ search: "?internal=0", storage }), "external");
  assert.deepEqual(sanitizeEventProperties({ source: "demo", selected_photos: 5, email: "private@example.com" }), { source: "demo", selected_photos: 5 });
});
