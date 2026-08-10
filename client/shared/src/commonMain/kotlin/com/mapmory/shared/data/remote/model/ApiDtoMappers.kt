package com.mapmory.shared.data.remote.model

import com.mapmory.shared.domain.model.Country
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.TripRecordMedia
import com.mapmory.shared.domain.model.TripRecordData
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.model.TripRecordPage

fun CountryDto.toDomain(): Country = Country(
    id = id,
    code = code,
    name = name,
)

fun LocationDto.toDomain(): Location = Location(
    id = id,
    countryId = countryId,
    parentId = parentId,
    regionCode = regionCode,
    name = name,
    type = LocationType.valueOf(locationType),
)

fun TripRecordMediaDto.toDomain(): TripRecordMedia = TripRecordMedia(
    id = id,
    objectKey = objectKey,
    sortOrder = sortOrder,
    url = viewUrl,
)

fun TripRecordListItemDto.toDomain(): TripRecordData = TripRecordData(
    id = id,
    memberId = member.id,
    locationId = location.id,
    title = title,
    content = "",
    startDate = startDate,
    endDate = endDate,
    media = emptyList(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    thumbnailUrl = thumbnailUrl,
)

fun TripRecordDetailDto.toDomain(): TripRecordData = TripRecordData(
    id = id,
    memberId = member.id,
    locationId = location.id,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    media = media.map(TripRecordMediaDto::toDomain),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TripRecordRequestDto.toDraft(): TripRecordDraft = TripRecordDraft(
    locationId = locationId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    mediaObjectKeys = objectKeys,
)

fun TripRecordDraft.toRequestDto(): TripRecordRequestDto = TripRecordRequestDto(
    locationId = locationId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    objectKeys = mediaObjectKeys,
)

fun PageDto<TripRecordListItemDto>.toDomain(): TripRecordPage = TripRecordPage(
    records = items.map(TripRecordListItemDto::toDomain),
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages,
)
