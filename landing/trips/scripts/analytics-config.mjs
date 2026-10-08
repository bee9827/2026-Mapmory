// Keep Trips tracking off until the shared GA stream's automatic events are reviewed.
// Enable in a reviewed follow-up change after console setup and receipt verification.
export const TRIPS_GA_RELEASE_APPROVED = false;

export function publicAnalyticsConfig(env) {
  const measurementId = (env.VITE_GA_MEASUREMENT_ID ?? '').trim();
  if (measurementId && !/^G-[A-Z0-9]+$/.test(measurementId)) throw new Error('Invalid public GA measurement ID');
  const captureLocal = env.VITE_GA_CAPTURE_LOCAL === 'true';
  return { measurementId: TRIPS_GA_RELEASE_APPROVED || captureLocal ? measurementId : '', captureLocal, debug: env.VITE_GA_DEBUG === 'true' };
}

export function analyticsConfigSource(env) {
  return `export const analyticsConfig = Object.freeze(${JSON.stringify(publicAnalyticsConfig(env))});\n`;
}
