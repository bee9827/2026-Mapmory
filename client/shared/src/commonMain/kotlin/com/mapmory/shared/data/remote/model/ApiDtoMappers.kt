package com.mapmory.shared.data.remote.model

import com.mapmory.shared.domain.model.Country
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.model.LocationType
import com.mapmory.shared.domain.model.RecordMedia
import com.mapmory.shared.domain.model.TravelRecord
import com.mapmory.shared.domain.model.TravelRecordDraft
import com.mapmory.shared.domain.model.TravelRecordPage

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

fun RecordMediaDto.toDomain(): RecordMedia = RecordMedia(
    id = id,
    objectKey = objectKey,
    sortOrder = sortOrder,
    url = viewUrl,
)

fun TravelRecordListItemDto.toDomain(): TravelRecord = TravelRecord(
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

fun TravelRecordDetailDto.toDomain(): TravelRecord = TravelRecord(
    id = id,
    memberId = member.id,
    locationId = location.id,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    media = media.map(RecordMediaDto::toDomain),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TravelRecordRequestDto.toDraft(): TravelRecordDraft = TravelRecordDraft(
    locationId = locationId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    mediaObjectKeys = objectKeys,
)

fun TravelRecordDraft.toRequestDto(): TravelRecordRequestDto = TravelRecordRequestDto(
    locationId = locationId,
    title = title,
    content = content,
    startDate = startDate,
    endDate = endDate,
    objectKeys = mediaObjectKeys,
)

fun PageDto<TravelRecordListItemDto>.toDomain(): TravelRecordPage = TravelRecordPage(
    records = items.map(TravelRecordListItemDto::toDomain),
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages,
)
