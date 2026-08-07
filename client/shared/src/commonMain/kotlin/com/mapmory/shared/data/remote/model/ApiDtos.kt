package com.mapmory.shared.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseDto<T>(
    val data: T,
)

@Serializable
data class PageDto<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

@Serializable
data class CountryDto(
    val id: Long,
    val code: String,
    val name: String,
)

@Serializable
data class LocationDto(
    val id: Long,
    val countryId: Long,
    val parentId: Long?,
    val regionCode: String,
    val name: String,
    val locationType: String,
)

@Serializable
data class MemberSummaryDto(
    val id: Long,
    val name: String,
)

@Serializable
data class LocationSummaryDto(
    val id: Long,
    val countryCode: String,
    val regionCode: String,
    val name: String,
)

@Serializable
data class RecordMediaDto(
    val id: Long,
    val objectKey: String,
    val viewUrl: String? = null,
    val viewUrlExpiresIn: Long? = null,
    val sortOrder: Int,
)

@Serializable
data class TravelRecordListItemDto(
    val id: Long,
    val member: MemberSummaryDto,
    val location: LocationSummaryDto,
    val title: String,
    val startDate: String?,
    val endDate: String?,
    val thumbnailUrl: String? = null,
    val thumbnailUrlExpiresIn: Long? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class TravelRecordDetailDto(
    val id: Long,
    val member: MemberSummaryDto,
    val location: LocationSummaryDto,
    val title: String,
    val content: String,
    val startDate: String?,
    val endDate: String?,
    val media: List<RecordMediaDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class TravelRecordRequestDto(
    val locationId: Long,
    val title: String,
    val content: String,
    val startDate: String?,
    val endDate: String?,
    val objectKeys: List<String> = emptyList(),
)

@Serializable
data class IdResponseDto(
    val id: Long,
)
