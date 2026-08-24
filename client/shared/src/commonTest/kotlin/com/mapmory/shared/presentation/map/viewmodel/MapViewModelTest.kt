package com.mapmory.shared.presentation.map.viewmodel

import com.mapmory.shared.data.local.StaticRegionCatalog
import com.mapmory.shared.data.repository.FakeTripRecordRepository
import com.mapmory.shared.domain.model.TripRecordDraft
import com.mapmory.shared.domain.usecase.GetTripRecordsUseCase
import com.mapmory.shared.runSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapViewModelTest {
    @Test
    fun refreshBuildsVisitedCountriesProvincesAndDistrictsFromRepositoryRecords() = runSuspend {
        val catalog = StaticRegionCatalog()
        val repository = FakeTripRecordRepository(1) { "2026-08-24T00:00:00" }
        val gangnam = catalog.requireByCode("11680")
        val seoul = catalog.requireByCode("KR-11")
        val japan = catalog.requireByCode("JP")
        repository.createTripRecord(draft(gangnam.id, "서울 여행")).getOrThrow()
        repository.createTripRecord(draft(japan.id, "일본 여행")).getOrThrow()
        val viewModel = MapViewModel(GetTripRecordsUseCase(repository), catalog)

        viewModel.refresh()

        assertEquals(setOf("KR", "JP"), viewModel.visitedCountryCodes)
        assertEquals(setOf("KR-11"), viewModel.visitedProvinceCodes)
        assertEquals(setOf("11680"), viewModel.visitedDistrictCodes("KR-11"))
        assertTrue(viewModel.hasRecords(gangnam))
        assertTrue(viewModel.hasRecords(seoul))
    }

    private fun draft(locationId: Long, title: String) = TripRecordDraft(
        locationId = locationId,
        title = title,
        content = "",
        startDate = null,
        endDate = null,
        mediaObjectKeys = emptyList(),
    )
}
