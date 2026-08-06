package com.mapmory.shared.domain

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
        return TripRecords(_tripRecords + newRecord)
    }
}