import test from "node:test";
import assert from "node:assert/strict";
import {
  ANALYTICS_EVENTS,
  buildEventParameters,
  isSupportedEvent,
} from "../src/analytics.js";

test("declares the agreed landing funnel events", () => {
  assert.deepEqual(
    new Set(Object.values(ANALYTICS_EVENTS)),
    new Set([
      "experience_view",
      "experience_start",
      "place_select",
      "experience_engagement",
      "waitlist_cta_click",
      "waitlist_form_view",
      "waitlist_form_start",
      "waitlist_submit",
      "waitlist_submit_error",
      "download_click",
    ]),
  );
});

test("adds the landing version and removes direct personal information", () => {
  assert.deepEqual(
    buildEventParameters({
      cta_placement: "hero",
      email: "person@example.com",
      phone_number: "010-0000-0000",
      unused: undefined,
    }),
    {
      landing_version: "v1",
      cta_placement: "hero",
    },
  );
});

test("rejects event names outside the agreed taxonomy", () => {
  assert.equal(isSupportedEvent("waitlist_submit"), true);
  assert.equal(isSupportedEvent("button_click"), false);
});
