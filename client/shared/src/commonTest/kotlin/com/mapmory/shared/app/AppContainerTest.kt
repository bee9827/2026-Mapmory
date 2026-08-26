package com.mapmory.shared.app

import com.mapmory.shared.data.local.StaticRegionCatalog
import com.mapmory.shared.data.remote.AccessTokenProvider
import com.mapmory.shared.data.remote.MapSummaryRemoteRepository
import com.mapmory.shared.data.remote.TripRecordRemoteRepository
import com.mapmory.shared.data.repository.FakeTripRecordRepository
import com.mapmory.shared.presentation.photo.SelectedPhoto
import com.mapmory.shared.presentation.triprecord.state.TripRecordDetailUiState
import com.mapmory.shared.presentation.triprecord.state.TripRecordListUiState
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppContainerTest {
    @Test
    fun `remote container can be assembled from base url and a token provider`() {
        val container = createRemoteAppContainer(
            apiBaseUrl = "https://api.example.com/api/v1",
            accessTokenProvider = AccessTokenProvider { "guest-token" },
        )

        assertIs<TripRecordRemoteRepository>(container.tripRecordRepository)
        assertIs<MapSummaryRemoteRepository>(container.mapSummaryRepository)
        container.close()
    }

    @Test
    fun `container can be assembled with a replaceable repository`() {
        val repository = FakeTripRecordRepository { "2026-08-24T00:00:00" }

        val container = createAppContainer(
            tripRecordRepository = repository,
            regionCatalog = StaticRegionCatalog(),
        )

        assertSame(repository, container.tripRecordRepository)
        assertSame(repository, container.mapSummaryRepository)
    }

    @Test
    fun `screen view models share one repository without sharing ui state`() = runSuspend {
        val container = createInMemoryAppContainer()
        val gangnam = container.regionCatalog.requireByCode("11680")
        val editor = container.viewModelFactory.createTripRecordEditorViewModel()

        editor.selectLocation(gangnam)
        editor.updateTitle("컨테이너 여행")
        editor.updateContent("화면별 ViewModel이 같은 저장소를 바라본다.")
        editor.updateStartDate("2026-08-24")
        editor.addPhotos(
            listOf(
                SelectedPhoto(
                    id = "local/photo.jpg",
                    displayName = "photo.jpg",
                    previewBytes = byteArrayOf(1, 2, 3),
                    originalBytes = byteArrayOf(4, 5, 6),
                ),
            ),
        )

        assertTrue(editor.save())

        val list = container.viewModelFactory.createTripRecordListViewModel()
        list.load()
        val listState = assertIs<TripRecordListUiState.Success>(list.uiState)
        val savedRecord = listState.records.single()
        assertEquals("컨테이너 여행", savedRecord.title)
        assertEquals("강남구", savedRecord.locationName)
        assertContentEquals(
            byteArrayOf(1, 2, 3),
            savedRecord.photos.single().previewBytes?.bytesForDecoding(),
        )
        assertContentEquals(
            byteArrayOf(4, 5, 6),
            savedRecord.photos.single().originalBytes?.bytesForDecoding(),
        )

        val detail = container.viewModelFactory.createTripRecordDetailViewModel()
        detail.load(savedRecord.id)
        val detailState = assertIs<TripRecordDetailUiState.Success>(detail.uiState)
        assertEquals(savedRecord.id, detailState.record.id)
        assertEquals("강남구", detailState.record.locationName)
        assertContentEquals(
            byteArrayOf(1, 2, 3),
            detailState.record.photos.single().previewBytes?.bytesForDecoding(),
        )

        assertNotSame(list, container.viewModelFactory.createTripRecordListViewModel())
        assertNotSame(detail, container.viewModelFactory.createTripRecordDetailViewModel())
    }
}
