const MAX_CITY_DISTANCE_KM = 50;
const EARTH_RADIUS_KM = 6371;
const radians = degrees => degrees * Math.PI / 180;

export function distanceKm(lat1, lon1, lat2, lon2) {
  const deltaLat = radians(lat2 - lat1), deltaLon = radians(lon2 - lon1);
  const a = Math.sin(deltaLat / 2) ** 2 + Math.cos(radians(lat1)) * Math.cos(radians(lat2)) * Math.sin(deltaLon / 2) ** 2;
  return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(Math.min(1, a)));
}

export function createCityResolver(data = { cities: [] }) {
  const cities = [...(data.cities ?? [])].sort((a, b) => a[4] - b[4] || a[0] - b[0]);
  // Travel-area grouping keeps the Strip with Las Vegas and LAX with LA.
  // These are explicitly approximate metropolitan areas, not city boundaries.
  const metroRadii = new Map([[5368361, 35], [5506956, 25]]);
  const metros = cities.filter(city => metroRadii.has(city[0]));
  return (coords, countryCode = null) => {
    if (!coords) return null;
    const { latitude, longitude } = coords;
    let selected = null, nearest = MAX_CITY_DISTANCE_KM;
    for (const city of metros) {
      if (countryCode && city[1] !== countryCode) continue;
      const distance = distanceKm(latitude, longitude, city[4], city[5]);
      if (distance <= metroRadii.get(city[0])) return { id: city[0], countryCode: city[1], name: city[3], distanceKm: distance, kind: 'metro' };
    }
    const latDelta = MAX_CITY_DISTANCE_KM / EARTH_RADIUS_KM * 180 / Math.PI;
    let lo = 0, hi = cities.length;
    while (lo < hi) { const mid = (lo + hi) >>> 1; if (cities[mid][4] < latitude - latDelta) lo = mid + 1; else hi = mid; }
    for (let i = lo; i < cities.length && cities[i][4] <= latitude + latDelta; i++) {
      const city = cities[i];
      if (countryCode && city[1] !== countryCode) continue;
      const distance = distanceKm(latitude, longitude, city[4], city[5]);
      if (distance < nearest) { selected = city; nearest = distance; }
    }
    return selected ? { id: selected[0], countryCode: selected[1], name: selected[3], distanceKm: nearest, kind: 'nearby-city' } : null;
  };
}
