package com.mapmory.shared.app

import com.mapmory.shared.data.local.StaticRegionCatalog
import com.mapmory.shared.data.remote.AccessTokenProvider
import com.mapmory.shared.data.remote.MapSummaryRemoteRepository
import com.mapmory.shared.data.remote.TripRecordRemoteRepository
import com.mapmory.shared.data.remote.createHttpClient
import com.mapmory.shared.data.repository.FakeTripRecordRepository
import com.mapmory.shared.domain.region.RegionCatalog
import com.mapmory.shared.domain.repository.MapSummaryRepository
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
    val mapSummaryRepository: MapSummaryRepository
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
    private val mapSummaryRepository: MapSummaryRepository,
    private val regionCatalog: RegionCatalog,
) : MapmoryViewModelFactory {
    override fun createMapViewModel(): MapViewModel = MapViewModel(
        mapSummaryRepository = mapSummaryRepository,
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
    override val mapSummaryRepository: MapSummaryRepository,
    private val onClose: () -> Unit,
) : AppContainer {
    override val viewModelFactory: MapmoryViewModelFactory = DefaultMapmoryViewModelFactory(
        repository = tripRecordRepository,
        mapSummaryRepository = mapSummaryRepository,
        regionCatalog = regionCatalog,
    )

    override fun close() = onClose()
}

fun createAppContainer(
    tripRecordRepository: TripRecordRepository,
    mapSummaryRepository: MapSummaryRepository = requireNotNull(
        tripRecordRepository as? MapSummaryRepository,
    ) { "지도 요약 Repository를 함께 전달해 주세요." },
    regionCatalog: RegionCatalog = StaticRegionCatalog(),
    onClose: () -> Unit = {},
): AppContainer = DefaultAppContainer(
    regionCatalog = regionCatalog,
    tripRecordRepository = tripRecordRepository,
    mapSummaryRepository = mapSummaryRepository,
    onClose = onClose,
)

fun createInMemoryAppContainer(
    now: () -> String = { "2026-08-24T00:00:00" },
): AppContainer {
    val regionCatalog = StaticRegionCatalog()
    return createAppContainer(
        tripRecordRepository = FakeTripRecordRepository(
            regionCatalog = regionCatalog,
            now = now,
        ),
        regionCatalog = regionCatalog,
    )
}

/** 게스트 로그인이 완료돼 토큰을 제공할 수 있다는 가정 아래 원격 구현을 조립한다. */
fun createRemoteAppContainer(
    apiBaseUrl: String,
    accessTokenProvider: AccessTokenProvider,
    regionCatalog: RegionCatalog = StaticRegionCatalog(),
): AppContainer {
    val client = createHttpClient()
    return createAppContainer(
        tripRecordRepository = TripRecordRemoteRepository(
            client = client,
            apiBaseUrl = apiBaseUrl,
            accessTokenProvider = accessTokenProvider,
            regionCatalog = regionCatalog,
        ),
        mapSummaryRepository = MapSummaryRemoteRepository(
            client = client,
            apiBaseUrl = apiBaseUrl,
            accessTokenProvider = accessTokenProvider,
        ),
        regionCatalog = regionCatalog,
        onClose = client::close,
    )
}
