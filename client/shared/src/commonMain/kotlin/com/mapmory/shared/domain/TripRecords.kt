package com.mapmory.shared.domain

import kotlin.uuid.Uuid

class TripRecords(
    tripRecords: List<TripRecord> = emptyList(),
) {
    private val records: List<TripRecord> = tripRecords.toList()

    val _tripRecords: List<TripRecord>
        get() = records.toList()

    fun addTripRecord(
        imageUri: String,
        tripRecordTitle: String,
        tripRecordDescription: String?,
        tripLocation: String,
    ): TripRecords {
        val newRecord = TripRecord(
            imageUrl = imageUri,
            tripRecordTitle = tripRecordTitle,
            tripRecordDescription = tripRecordDescription,
            location = tripLocation
        )
        return TripRecords(records + newRecord)
    }

    fun removeTripRecord(
        deletingRecord: TripRecord
    ): TripRecords {
        val record = records.find { it.id == deletingRecord.id }
        if(record == null) return this

        return TripRecords(records.minus(record))
    }

    private fun findTripRecordId(id: Uuid): Uuid? = records.find { it.id == id }?.id

}