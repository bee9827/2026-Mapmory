package com.mapmory.shared.domain

import kotlinx.datetime.LocalDate
import kotlin.random.Random

data class TripRecord(
    val id: Long = Random.nextLong(from = 1L, until = Long.MAX_VALUE),
    val imageUrl: String,
    val tripRecordTitle: String,
    val tripRecordDescription: String?,
    val startTripDate: LocalDate,
    val endTripDate: LocalDate,
    val location: String,
)
