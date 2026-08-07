package com.mapmory.shared.domain

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class TripRecord(
    val id: Uuid = Uuid.random(),
    val imageUrl: String,
    val tripRecordTitle: String,
    val tripRecordDescription: String?,
    val startTripDate: LocalDate,
    val endTripDate: LocalDate,
    val location: String,
)
