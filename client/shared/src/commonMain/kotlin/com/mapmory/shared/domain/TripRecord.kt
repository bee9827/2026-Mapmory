package com.mapmory.shared.domain

import kotlin.uuid.Uuid

data class TripRecord(
    val id: Uuid = Uuid.random(),
    val imageUrl: String,
    val tripRecordTitle: String,
    val tripRecordDescription: String?,
    val location: String,
)
