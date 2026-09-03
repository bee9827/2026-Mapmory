import assert from "node:assert/strict";
import test from "node:test";

import {
  WORLD_MEMORY_HISTORY_KEY,
  createWorldMemoryHistoryState,
  isWorldMemoryHistoryEntry,
} from "../src/worldMemoryHistory.js";

test("preserves existing browser history state when marking an open world memory", () => {
  const currentState = {
    index: 4,
    scroll: { x: 0, y: 1280 },
  };

  const nextState = createWorldMemoryHistoryState(currentState, "usa-west");

  assert.deepEqual(nextState, {
    index: 4,
    scroll: { x: 0, y: 1280 },
    [WORLD_MEMORY_HISTORY_KEY]: "usa-west",
  });
  assert.notEqual(nextState, currentState);
  assert.deepEqual(currentState, {
    index: 4,
    scroll: { x: 0, y: 1280 },
  });
});

test("creates a clean marker state when the existing history state is absent or not an object", () => {
  assert.deepEqual(createWorldMemoryHistoryState(null, "usa-west"), {
    [WORLD_MEMORY_HISTORY_KEY]: "usa-west",
  });
  assert.deepEqual(createWorldMemoryHistoryState("legacy-state", "jeju-coast"), {
    [WORLD_MEMORY_HISTORY_KEY]: "jeju-coast",
  });
});

test("recognizes only non-empty Mapmory world-memory history markers", () => {
  assert.equal(isWorldMemoryHistoryEntry(null), false);
  assert.equal(isWorldMemoryHistoryEntry({}), false);
  assert.equal(isWorldMemoryHistoryEntry({ [WORLD_MEMORY_HISTORY_KEY]: "" }), false);
  assert.equal(isWorldMemoryHistoryEntry({ [WORLD_MEMORY_HISTORY_KEY]: true }), false);
  assert.equal(isWorldMemoryHistoryEntry({ [WORLD_MEMORY_HISTORY_KEY]: "usa-west" }), true);
});
