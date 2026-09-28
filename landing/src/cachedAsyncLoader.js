export function createCachedAsyncLoader(load) {
  let pending;

  return function loadCached() {
    if (!pending) {
      pending = Promise.resolve()
        .then(load)
        .catch((error) => {
          pending = undefined;
          throw error;
        });
    }
    return pending;
  };
}
