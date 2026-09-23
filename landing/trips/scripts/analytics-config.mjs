// Public ID of the user-approved Trips-only stream, created 2026-09-23 with
// enhanced measurement OFF. Never inherit the landing/Recap stream's ID.
export const TRIPS_GA_MEASUREMENT_ID = 'G-P0TDZHRQ6P';
export const TRIPS_GA_RELEASE_APPROVED = true;

export function publicAnalyticsConfig(env) {
  const captureLocal = env.VITE_GA_CAPTURE_LOCAL === 'true';
  const measurementId = captureLocal ? (env.VITE_GA_MEASUREMENT_ID ?? '').trim() : TRIPS_GA_MEASUREMENT_ID;
  if (measurementId && !/^G-[A-Z0-9]+$/.test(measurementId)) throw new Error('Invalid public GA measurement ID');
  return { measurementId: TRIPS_GA_RELEASE_APPROVED || captureLocal ? measurementId : '', captureLocal, debug: env.VITE_GA_DEBUG === 'true' };
}

export function analyticsConfigSource(env) {
  return `export const analyticsConfig = Object.freeze(${JSON.stringify(publicAnalyticsConfig(env))});\n`;
}
