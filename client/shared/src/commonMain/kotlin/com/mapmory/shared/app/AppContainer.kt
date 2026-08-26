package com.mapmory.shared.app

import com.mapmory.shared.data.local.StaticRegionCatalog
import com.mapmory.shared.data.repository.FakeTripRecordRepository
import com.mapmory.shared.domain.region.RegionCatalog
import com.mapmory.shared.domain.repository.TripRecordRepository
import com.mapmory.shared.domain.usecase.CreateTripRecordUseCase
import com.mapmory.shared.domain.usecase.DeleteTripRecordUseCase
import com.mapmory.shared.domain.usecase.GetTripRecordUseCase
import com.mapmory.shared.domain.usecase.GetTripRecordsUseCase
import com.mapmory.shared.domain.usecase.UpdateTripRecordUseCase
import com.mapmory.shared.presentation.map.viewmodel.MapViewModel
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordDetailViewModel
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordEditorViewModel
import com.mapmory.shared.presentation.triprecord.viewmodel.TripRecordListViewModel

interface AppContainer {
    val regionCatalog: RegionCatalog
    val tripRecordRepository: TripRecordRepository
    val viewModelFactory: MapmoryViewModelFactory

    fun close() = Unit
}

interface MapmoryViewModelFactory {
    fun createMapViewModel(): MapViewModel

    fun createTripRecordListViewModel(): TripRecordListViewModel

    fun createTripRecordDetailViewModel(): TripRecordDetailViewModel

    fun createTripRecordEditorViewModel(): TripRecordEditorViewModel
}

private class DefaultMapmoryViewModelFactory(
    private val repository: TripRecordRepository,
    private val regionCatalog: RegionCatalog,
) : MapmoryViewModelFactory {
    override fun createMapViewModel(): MapViewModel = MapViewModel(
        getTripRecords = GetTripRecordsUseCase(repository),
        regionCatalog = regionCatalog,
    )

    override fun createTripRecordListViewModel(): TripRecordListViewModel =
        TripRecordListViewModel(
            getTripRecords = GetTripRecordsUseCase(repository),
            regionCatalog = regionCatalog,
        )

    override fun createTripRecordDetailViewModel(): TripRecordDetailViewModel =
        TripRecordDetailViewModel(
            getTripRecord = GetTripRecordUseCase(repository),
            deleteTripRecord = DeleteTripRecordUseCase(repository),
            regionCatalog = regionCatalog,
        )

    override fun createTripRecordEditorViewModel(): TripRecordEditorViewModel =
        TripRecordEditorViewModel(
            createTripRecord = CreateTripRecordUseCase(repository),
            updateTripRecord = UpdateTripRecordUseCase(repository),
            getTripRecord = GetTripRecordUseCase(repository),
            regionCatalog = regionCatalog,
        )
}

private class DefaultAppContainer(
    override val regionCatalog: RegionCatalog,
    override val tripRecordRepository: TripRecordRepository,
    private val onClose: () -> Unit,
) : AppContainer {
    override val viewModelFactory: MapmoryViewModelFactory = DefaultMapmoryViewModelFactory(
        repository = tripRecordRepository,
        regionCatalog = regionCatalog,
    )

    override fun close() = onClose()
}

fun createAppContainer(
    tripRecordRepository: TripRecordRepository,
    regionCatalog: RegionCatalog = StaticRegionCatalog(),
    onClose: () -> Unit = {},
): AppContainer = DefaultAppContainer(
    regionCatalog = regionCatalog,
    tripRecordRepository = tripRecordRepository,
    onClose = onClose,
)

fun createInMemoryAppContainer(
    now: () -> String = { "2026-08-24T00:00:00" },
): AppContainer = createAppContainer(
    tripRecordRepository = FakeTripRecordRepository(now = now),
)
