package com.mapmory.shared.presentation.photo

import androidx.compose.runtime.Composable
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType

const val MaxPhotosPerRecord = 10

data class SelectedPhoto(
    val id: String,
    val displayName: String,
    val previewBytes: ByteArray?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val capturedAt: String? = null,
    val originalBytes: ByteArray? = null,
)

data class PhotoLibraryActions(
    val pickFromGallery: () -> Unit,
    val recommendForLocation: (Location, String?) -> Unit,
    val recommendationsAvailable: Boolean = true,
)

internal data class PhotoAdministrativeArea(
    val countryCode: String?,
    val administrativeArea: String?,
    val subAdministrativeArea: String?,
    val locality: String?,
    val subLocality: String?,
)

@Composable
expect fun rememberPhotoLibraryActions(
    onPhotosPicked: (List<SelectedPhoto>) -> Unit,
    onPhotosRecommended: (List<SelectedPhoto>) -> Unit,
    onMessage: (String) -> Unit,
): PhotoLibraryActions

internal fun Location.recommendationSearchText(parentName: String?): String = buildString {
    append(name)
    if (
        !parentName.isNullOrBlank() &&
        parentName != name &&
        !normalizeAreaName(name).startsWith(normalizeAreaName(parentName))
    ) {
        append(" ").append(parentName)
    }
    if (countryId == 1L) append(" 대한민국")
}

internal fun Location.recommendationRadiusMeters(): Double = when {
    countryId != 1L -> 3_000_000.0
    type == LocationType.PROVINCE -> 350_000.0
    else -> 100_000.0
}

internal fun PhotoAdministrativeArea.matches(
    target: Location,
    parentName: String?,
): Boolean {
    if (target.countryId != 1L) {
        return countryCode.equals(target.regionCode, ignoreCase = true)
    }
    if (!countryCode.isNullOrBlank() && !countryCode.equals("KR", ignoreCase = true)) return false

    // Geocoder providers do not always put the same administrative level in
    // the same field. Search all returned address levels instead of relying
    // on one fixed Android/iOS field mapping.
    val parentMatches = parentName == null || areaText(
        administrativeArea,
        locality,
        subAdministrativeArea,
        subLocality,
    ).contains(normalizeAreaName(parentName))
    if (!parentMatches) return false

    return when (target.type) {
        LocationType.PROVINCE -> geocodedArea(target.name)
        LocationType.DISTRICT -> {
            val normalizedTarget = normalizeAreaName(target.name)
            val normalizedParent = parentName?.let(::normalizeAreaName).orEmpty()
            val districtName = normalizedTarget
                .removePrefix(normalizedParent)
                .ifBlank { normalizedTarget }
            areaNameCandidates(districtName).any { candidate -> geocodedArea(candidate) }
        }
    }
}

private fun PhotoAdministrativeArea.geocodedArea(targetName: String): Boolean = areaText(
    administrativeArea,
    subAdministrativeArea,
    locality,
    subLocality,
).contains(normalizeAreaName(targetName))

private fun areaNameCandidates(value: String): Set<String> {
    val normalized = normalizeAreaName(value)
    return buildSet {
        add(normalized)
        val withoutDistrictSuffix = normalized.removeSuffix("구")
        if (withoutDistrictSuffix.length >= 2) add(withoutDistrictSuffix)
    }
}

private fun areaText(vararg values: String?): String = values
    .filterNotNull()
    .joinToString(separator = "")
    .let(::normalizeAreaName)

private fun normalizeAreaName(value: String): String = value
    .lowercase()
    .filter { it.isLetterOrDigit() }

internal fun mergeSelectedPhotos(
    existing: List<SelectedPhoto>,
    incoming: List<SelectedPhoto>,
): List<SelectedPhoto> = (existing + incoming)
    .distinctBy(SelectedPhoto::id)
    .take(MaxPhotosPerRecord)
