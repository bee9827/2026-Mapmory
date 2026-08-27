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
      "experience_cta_click",
      "experience_view",
      "experience_start",
      "memory_open",
      "korea_memory_add",
      "experience_end",
      "waitlist_cta_click",
      "waitlist_form_view",
      "waitlist_form_start",
      "waitlist_submit_attempt",
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
      arbitrary_payload: "must-not-pass",
      unused: undefined,
    }),
    {
      landing_version: "v2",
      cta_placement: "hero",
    },
  );
});

test("rejects event names outside the agreed taxonomy", () => {
  assert.equal(isSupportedEvent("waitlist_submit"), true);
  assert.equal(isSupportedEvent("button_click"), false);
});

test("keeps exact experience duration and distinct-memory parameters", () => {
  assert.deepEqual(
    buildEventParameters({
      experience_type: "globe",
      active_duration_ms: 23740,
      unique_memories_opened: 3,
      last_completed_step: "memory_open",
    }),
    {
      landing_version: "v2",
      experience_type: "globe",
      active_duration_ms: 23740,
      unique_memories_opened: 3,
      last_completed_step: "memory_open",
    },
  );
});
