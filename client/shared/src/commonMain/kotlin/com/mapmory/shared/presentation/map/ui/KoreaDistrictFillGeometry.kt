package com.mapmory.shared.presentation.map.ui

import com.mapmory.shared.presentation.map.domain.GeoPoint

/** Reuse the displayed province outline only on exposed fill arcs, keeping city seams fixed. */
internal fun alignDistrictFillWithProvince(
    rings: List<List<GeoPoint>>,
    provinceRings: List<List<GeoPoint>>,
    sharedEdges: Set<Set<GeoPoint>>,
): List<List<GeoPoint>> = rings.map { ring ->
    val points = ring.dropLastWhile { it == ring.first() }
    val firstShared = points.indices.firstOrNull { index ->
        setOf(points[index], points[(index + 1) % points.size]) in sharedEdges
    }
    // Independent islands have no city seam to anchor a replacement arc.
    if (firstShared == null) return@map ring
    val cycle = (points.drop(firstShared) + points.take(firstShared)).let { it + it.first() }
    val result = mutableListOf(cycle.first())
    var index = 0
    while (index < cycle.lastIndex) {
        if (setOf(cycle[index], cycle[index + 1]) in sharedEdges) {
            result.add(cycle[++index])
        } else {
            val start = index
            while (index < cycle.lastIndex && setOf(cycle[index], cycle[index + 1]) !in sharedEdges) index++
            val original = cycle.subList(start, index + 1)
            result.addAll(matchingProvinceArc(original, provinceRings).drop(1))
        }
    }
    result
}

private fun matchingProvinceArc(original: List<GeoPoint>, provinceRings: List<List<GeoPoint>>): List<GeoPoint> {
    val candidates = provinceRings.flatMap { ring ->
        val points = ring.dropLastWhile { it == ring.first() }
        val first = points.indexOf(original.first())
        val last = points.indexOf(original.last())
        if (first < 0 || last < 0 || first == last) return@flatMap emptyList()
        fun arc(from: Int, to: Int): List<GeoPoint> =
            if (from <= to) points.subList(from, to + 1) else points.drop(from) + points.take(to + 1)
        listOf(arc(first, last), arc(last, first).reversed())
    }
    // Both directions must stay close: matching endpoints alone could select a remote coastline.
    val toleranceSquared = 0.008f * 0.008f
    return candidates.mapNotNull { candidate ->
        fun within(source: List<GeoPoint>, target: List<GeoPoint>): Boolean = source.all { point ->
            target.zipWithNext().any { (start, end) -> point.segmentDistanceSquared(start, end) <= toleranceSquared }
        }
        candidate.takeIf { within(it, original) && within(original, it) }
    }.minByOrNull { it.size } ?: original
}

private fun GeoPoint.segmentDistanceSquared(start: GeoPoint, end: GeoPoint): Float {
    val dx = end.longitude - start.longitude
    val dy = end.latitude - start.latitude
    val lengthSquared = dx * dx + dy * dy
    val fraction = if (lengthSquared == 0f) 0f else (
        ((longitude - start.longitude) * dx + (latitude - start.latitude) * dy) / lengthSquared
    ).coerceIn(0f, 1f)
    val x = longitude - start.longitude - fraction * dx
    val y = latitude - start.latitude - fraction * dy
    return x * x + y * y
}
