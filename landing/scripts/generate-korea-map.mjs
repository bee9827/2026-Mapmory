import { readFileSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";

const sourceDir = resolve(
  "../client/shared/src/commonMain/kotlin/com/mapmory/shared/presentation/map/data",
);

const sources = [0, 1, 2, 3]
  .map((index) => readFileSync(resolve(sourceDir, `GeneratedKoreaMapDataPart0${index}.kt`), "utf8"))
  .join("\n");

const provinces = [];
const pointPattern = /p\((-?\d+(?:\.\d+)?)f,\s*(-?\d+(?:\.\d+)?)f\)/g;
let current = null;

for (const line of sources.split(/\r?\n/)) {
  const provinceMatch = line.match(/province\("([^"]+)",\s*"([^"]+)"/);
  if (provinceMatch) {
    if (current?.rings.length) provinces.push(current);
    current = { code: provinceMatch[1], name: provinceMatch[2], rings: [] };
  }

  if (!current || !line.includes("listOf(p(")) continue;

  const points = [...line.matchAll(pointPattern)].map((match) => [
    Number(match[1]),
    Number(match[2]),
  ]);
  if (points.length > 2) current.rings.push(points);
}

if (current?.rings.length) provinces.push(current);

if (provinces.length !== 17) {
  throw new Error(`Expected 17 provinces, parsed ${provinces.length}.`);
}

writeFileSync(
  resolve("src/data/korea-provinces.json"),
  `${JSON.stringify(provinces)}\n`,
  "utf8",
);

console.log(`Generated ${provinces.length} provinces.`);
