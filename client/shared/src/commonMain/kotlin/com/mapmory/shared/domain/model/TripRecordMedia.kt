package com.mapmory.shared.domain.model

// 여행 기록에 연결된 미디어 메타데이터다.
data class TripRecordMedia(
    val id: Long,
    val objectKey: String,
    val sortOrder: Int,
    val url: String?,
)
