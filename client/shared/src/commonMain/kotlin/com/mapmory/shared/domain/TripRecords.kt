package com.mapmory.shared.domain

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class TripRecords(
    tripRecords: List<TripRecord> = emptyList(),
) {
    private val records: List<TripRecord> = tripRecords.toList()

    val tripRecords: List<TripRecord>
        get() = records.toList()

    fun addTripRecord(
        imageUri: String,
        tripRecordTitle: String,
        tripRecordDescription: String?,
        tripLocation: String,
        startTripDate: LocalDate,
        endTripDate: LocalDate,
    ): TripRecords {
        val newRecord = TripRecord(
            imageUrl = imageUri,
            tripRecordTitle = tripRecordTitle,
            tripRecordDescription = tripRecordDescription,
            location = tripLocation,
            startTripDate = startTripDate,
            endTripDate = endTripDate,
        )
        return TripRecords(records + newRecord)
    }

    fun removeTripRecord(
        deletingRecord: TripRecord
    ): TripRecords {
        val record = findTripRecordId(deletingRecord.id) ?: return this

        return TripRecords(records.minus(record))
    }

    fun editTripRecord(
        editingRecord: TripRecord,
        editingImage: String?,
        editingTitle: String?,
        editingDescription: String?,
        editingLocation: String?,
    ): TripRecords {
        val record = findTripRecordId(editingRecord.id)
            ?: throw IllegalArgumentException("해당 id를 찾을 수 없습니다")

        val editedRecord = record.copy(
            imageUrl = editingImage ?: record.imageUrl,
            tripRecordTitle = editingTitle ?: record.tripRecordTitle,
            tripRecordDescription = editingDescription ?: record.tripRecordDescription,
            location = editingLocation ?: record.location,
        )

        return TripRecords(
            records.map { currentRecord ->
                if (currentRecord.id == record.id) editedRecord else currentRecord
            },
        )
    }

    private fun findTripRecordId(id: Uuid): TripRecord? = records.find { it.id == id }

}
