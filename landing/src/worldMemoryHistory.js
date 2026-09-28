const WORLD_MEMORY_HISTORY_KEY = "__mapmoryWorldMemory";

function createWorldMemoryHistoryState(currentState, memoryKey) {
  const preservedState = currentState && typeof currentState === "object" && !Array.isArray(currentState)
    ? currentState
    : {};

  return {
    ...preservedState,
    [WORLD_MEMORY_HISTORY_KEY]: memoryKey,
  };
}

function isWorldMemoryHistoryEntry(state) {
  return Boolean(
    state
      && typeof state === "object"
      && typeof state[WORLD_MEMORY_HISTORY_KEY] === "string"
      && state[WORLD_MEMORY_HISTORY_KEY].length > 0,
  );
}

export {
  WORLD_MEMORY_HISTORY_KEY,
  createWorldMemoryHistoryState,
  isWorldMemoryHistoryEntry,
};
