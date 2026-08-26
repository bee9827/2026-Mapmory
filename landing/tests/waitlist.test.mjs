import assert from "node:assert/strict";
import test from "node:test";

import { parseWaitlistStatus, WaitlistRequestError } from "../src/waitlist.js";

test("출시 알림 신규 신청 상태를 반환한다", () => {
  assert.equal(parseWaitlistStatus({ data: { status: "SUBSCRIBED" } }), "SUBSCRIBED");
});

test("이미 신청한 상태를 반환한다", () => {
  assert.equal(
    parseWaitlistStatus({ data: { status: "ALREADY_SUBSCRIBED" } }),
    "ALREADY_SUBSCRIBED",
  );
});

test("예상하지 못한 응답은 response 오류로 분류한다", () => {
  assert.throws(
    () => parseWaitlistStatus({ data: { status: "UNKNOWN" } }),
    (error) => error instanceof WaitlistRequestError && error.reason === "response",
  );
});
