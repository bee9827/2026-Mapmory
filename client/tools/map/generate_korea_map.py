#!/usr/bin/env python3
"""Generate static Kotlin province and district boundary data.

The source GeoJSON is read only during development. Runtime code consumes the
generated Kotlin files and never contacts the source repository.

Examples:
    python3 tools/map/generate_korea_map.py /tmp/geoBoundaries-KOR-ADM1.geojson
    python3 tools/map/generate_korea_map.py \
        --district-source /tmp/skorea-municipalities.json \
        --locations-source shared/src/commonMain/kotlin/com/mapmory/shared/domain/model/KoreanDistrictCode.kt
"""

from __future__ import annotations

import argparse
import json
import math
import re
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Any

PART_COUNT = 4
DISTRICT_SOURCE_PROVINCE_CODES = {
    "11": "KR-11", "21": "KR-26", "22": "KR-27", "23": "KR-28",
    "24": "KR-29", "25": "KR-30", "26": "KR-31", "29": "KR-50",
    "31": "KR-41", "32": "KR-42", "33": "KR-43", "34": "KR-44",
    "35": "KR-45", "36": "KR-46", "37": "KR-47", "38": "KR-48",
    "39": "KR-49",
}
# 군위군은 원본 2018 데이터에서 경북에 있었지만 현재 앱의 canonical
# 행정구역에서는 대구광역시 소속이다. 이 예외는 생성 단계에서만 적용한다.
DISTRICT_FEATURE_PROVINCE_OVERRIDES = {"37310": "KR-27"}
PROVINCE_NAMES = {
    "KR-11": "서울특별시", "KR-26": "부산광역시", "KR-27": "대구광역시",
    "KR-28": "인천광역시", "KR-29": "광주광역시", "KR-30": "대전광역시",
    "KR-31": "울산광역시", "KR-41": "경기도", "KR-42": "강원특별자치도",
    "KR-43": "충청북도", "KR-44": "충청남도", "KR-45": "전북특별자치도",
    "KR-46": "전라남도", "KR-47": "경상북도", "KR-48": "경상남도",
    "KR-49": "제주특별자치도", "KR-50": "세종특별자치시",
}
METROPOLITAN_PROVINCES = {
    "KR-11", "KR-26", "KR-27", "KR-28", "KR-29", "KR-30", "KR-31", "KR-50",
}
PROVINCE_PREFIXES = tuple(PROVINCE_NAMES.values()) + ("강원도", "전라북도")
CITY_DISTRICT_PATTERN = re.compile(r"^(.+시).+구$")
LOCATION_PATTERN = re.compile(
    r'KoreanDistrictCode\("([^"]+)",\s*"([^"]+)",\s*(?:"([^"]+)"|null)\)',
)


@dataclass(frozen=True)
class Location:
    code: str
    name: str
    province_code: str | None


@dataclass(frozen=True)
class Boundary:
    province_code: str
    code: str
    name: str
    rings: list[list[list[float]]]


def number(value: float) -> str:
    value = round(float(value), 5)
    if abs(value) < 0.000005:
        value = 0.0
    result = f"{value:.5f}".rstrip("0").rstrip(".")
    return (result if result not in ("", "-0") else "0") + "f"


def ring_expression(ring: list[list[float]]) -> str:
    points = ", ".join(f"p({number(lon)}, {number(lat)})" for lon, lat in ring)
    return f"listOf({points})"


def outer_rings(geometry: dict[str, Any]) -> list[list[list[float]]]:
    """Return only exterior rings; interior GeoJSON rings are holes."""
    geometry_type = geometry.get("type")
    coordinates = geometry.get("coordinates", [])
    if geometry_type == "Polygon":
        candidates = [coordinates[0]] if coordinates else []
    elif geometry_type == "MultiPolygon":
        candidates = [polygon[0] for polygon in coordinates if polygon]
    else:
        candidates = []
    return [ring for ring in candidates if len(ring) >= 3]


def read_province_features(source: Path) -> list[tuple[str, str, list[list[list[float]]]]]:
    features = []
    for feature in json.loads(source.read_text())["features"]:
        properties = feature.get("properties", {})
        raw_code = properties.get("shapeISO") or properties.get("code")
        if not raw_code:
            continue
        if raw_code in PROVINCE_NAMES:
            code = raw_code
        else:
            code = DISTRICT_SOURCE_PROVINCE_CODES.get(str(raw_code))
        if code is None:
            continue
        rings = outer_rings(feature.get("geometry", {}))
        if rings:
            features.append((code, PROVINCE_NAMES.get(code, properties.get("name", code)), rings))
    return features


def read_locations(source: Path) -> list[Location]:
    return [Location(code, name, province) for code, name, province in LOCATION_PATTERN.findall(source.read_text())]


def normalize_name(name: str) -> str:
    compact_name = name.replace(" ", "")
    for prefix in PROVINCE_PREFIXES:
        if compact_name.startswith(prefix):
            return compact_name[len(prefix):]
    return compact_name


def selectable_locations(locations: list[Location]) -> list[Location]:
    selectable = []
    for location in locations:
        if location.province_code is None:
            continue
        city = CITY_DISTRICT_PATTERN.fullmatch(normalize_name(location.name))
        if location.province_code in METROPOLITAN_PROVINCES or city is None:
            selectable.append(Location(location.code, normalize_name(location.name), location.province_code))
        else:
            selectable.append(Location(location.code[:-1] + "0", city.group(1), location.province_code))
    unique = {}
    for location in selectable:
        unique.setdefault(location.code, location)
    return list(unique.values())


def canonicalize_districts(source: Path, locations_source: Path) -> tuple[list[Boundary], list[str]]:
    targets = selectable_locations(read_locations(locations_source))
    by_name = {(location.province_code, location.name): location for location in targets}
    grouped: dict[tuple[str, str], Boundary] = {}
    skipped: list[str] = []

    for feature in json.loads(source.read_text())["features"]:
        properties = feature.get("properties", {})
        raw_code = str(properties.get("code", ""))
        province_code = DISTRICT_FEATURE_PROVINCE_OVERRIDES.get(raw_code)
        province_code = province_code or DISTRICT_SOURCE_PROVINCE_CODES.get(raw_code[:2])
        raw_name = properties.get("name")
        if not province_code or not raw_name:
            continue
        display_name = raw_name
        if province_code not in METROPOLITAN_PROVINCES:
            city = CITY_DISTRICT_PATTERN.fullmatch(raw_name)
            if city:
                display_name = city.group(1)
        target = by_name.get((province_code, display_name))
        rings = outer_rings(feature.get("geometry", {}))
        if target is None:
            skipped.append(f"{province_code} {raw_code} {raw_name}")
            continue
        key = (province_code, target.code)
        if key in grouped:
            grouped[key].rings.extend(rings)
        else:
            grouped[key] = Boundary(province_code, target.code, target.name, list(rings))

    result = list(grouped.values())
    result.sort(key=lambda boundary: (boundary.province_code, boundary.code))
    return result, skipped


def write_province_part(output: Path, index: int, features) -> str:
    object_name = f"GeneratedKoreaMapDataPart{index:02d}"
    lines = [
        "package com.mapmory.shared.presentation.map.data", "",
        "import com.mapmory.shared.presentation.map.domain.GeoPoint",
        "import com.mapmory.shared.presentation.map.domain.ProvincePolygon", "",
        f"internal object {object_name} {{",
        "    val provinces: List<ProvincePolygon> = listOf(",
    ]
    for code, name, rings in features:
        lines.append(f"        province({json.dumps(code)}, {json.dumps(name, ensure_ascii=False)}, listOf(")
        for ring_index, ring in enumerate(rings):
            comma = "," if ring_index < len(rings) - 1 else ""
            lines.append(f"            {ring_expression(ring)}{comma}")
        lines.append("        )),")
    lines += [
        "    )", "", "    private fun province(", "        code: String,", "        name: String,",
        "        rings: List<List<GeoPoint>>,",
        "    ): ProvincePolygon = ProvincePolygon(code = code, name = name, rings = rings)", "",
        "    private fun p(longitude: Float, latitude: Float): GeoPoint =",
        "        GeoPoint(longitude = longitude, latitude = latitude)", "}",
    ]
    (output / f"{object_name}.kt").write_text("\n".join(lines) + "\n")
    return object_name


def write_provinces(output: Path, source: Path) -> None:
    for old in output.glob("GeneratedKoreaMapDataPart*.kt"):
        old.unlink()
    features = read_province_features(source)
    chunk_size = math.ceil(len(features) / PART_COUNT)
    names = [
        write_province_part(output, index, features[index * chunk_size:(index + 1) * chunk_size])
        for index in range(PART_COUNT)
        if features[index * chunk_size:(index + 1) * chunk_size]
    ]
    parts = ",\n".join(f"        {name}.provinces" for name in names)
    aggregator = f"""package com.mapmory.shared.presentation.map.data

import com.mapmory.shared.presentation.map.domain.ProvincePolygon

/** Generated from a development-time GeoJSON source. */
internal object GeneratedKoreaMapData {{
    val provinces: List<ProvincePolygon> = listOf(
{parts},
    ).flatten()
}}
"""
    (output / "GeneratedKoreaMapData.kt").write_text(aggregator)
    print(f"generated {len(features)} provinces in {len(names)} parts")


def write_district_resource(output: Path, province_code: str, boundaries: list[Boundary]) -> None:
    suffix = province_code.removeprefix("KR-")
    payload = {
        "provinceCode": province_code,
        "districts": [
            {
                "code": boundary.code,
                "name": boundary.name,
                "rings": [
                    [[round(longitude, 5), round(latitude, 5)] for longitude, latitude in ring]
                    for ring in boundary.rings
                ],
            }
            for boundary in boundaries
        ],
    }
    (output / f"korea-districts-{suffix}.json").write_text(
        json.dumps(payload, ensure_ascii=False, separators=(",", ":")) + "\n",
    )


def write_districts(resource_output: Path, source: Path, locations_source: Path) -> None:
    resource_output.mkdir(parents=True, exist_ok=True)
    for old in resource_output.glob("korea-districts-*.json"):
        old.unlink()
    boundaries, skipped = canonicalize_districts(source, locations_source)
    by_province: dict[str, list[Boundary]] = defaultdict(list)
    for boundary in boundaries:
        by_province[boundary.province_code].append(boundary)
    for province, values in sorted(by_province.items()):
        write_district_resource(resource_output, province, values)
    print(f"generated {len(boundaries)} display boundaries for {len(by_province)} provinces")
    if skipped:
        print("skipped source features without a canonical app location:")
        print("\n".join(f"  - {entry}" for entry in skipped))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("province_source", nargs="?", type=Path)
    parser.add_argument("--district-source", type=Path)
    parser.add_argument("--locations-source", type=Path)
    parser.add_argument("--resource-output", type=Path, default=Path("shared/src/commonMain/composeResources/files"))
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("shared/src/commonMain/kotlin/com/mapmory/shared/presentation/map/data"),
    )
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    if args.province_source:
        write_provinces(args.output, args.province_source)
    if args.district_source:
        if not args.locations_source:
            parser.error("--locations-source is required with --district-source")
        write_districts(args.resource_output, args.district_source, args.locations_source)
    if not args.province_source and not args.district_source:
        parser.error("province source or --district-source is required")


if __name__ == "__main__":
    main()
