export function publicAnalyticsConfig(env) {
  const measurementId = (env.VITE_GA_MEASUREMENT_ID ?? '').trim();
  if (measurementId && !/^G-[A-Z0-9]+$/.test(measurementId)) throw new Error('Invalid public GA measurement ID');
  return { measurementId, captureLocal: env.VITE_GA_CAPTURE_LOCAL === 'true', debug: env.VITE_GA_DEBUG === 'true' };
}

export function analyticsConfigSource(env) {
  return `export const analyticsConfig = Object.freeze(${JSON.stringify(publicAnalyticsConfig(env))});\n`;
}
