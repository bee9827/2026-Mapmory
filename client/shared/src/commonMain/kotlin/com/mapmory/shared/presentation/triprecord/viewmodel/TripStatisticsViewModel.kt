package com.mapmory.shared.presentation.triprecord.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapmory.shared.domain.repository.TripStatisticsRepository
import com.mapmory.shared.presentation.triprecord.state.TopLocationUiModel
import com.mapmory.shared.presentation.triprecord.state.TripStatisticsUiModel
import com.mapmory.shared.presentation.triprecord.state.TripStatisticsUiState

/** 전용 여행 통계 API 응답을 화면 상태로 변환한다. */
class TripStatisticsViewModel(
    private val tripStatisticsRepository: TripStatisticsRepository,
) : ViewModel() {
    private var loadGeneration = 0L
    private val cachedStatistics = tripStatisticsRepository.getCachedStatistics()

    var uiState by mutableStateOf(
        cachedStatistics
            ?.toUiState()
            ?: TripStatisticsUiState.Loading,
    )
        private set

    suspend fun refresh() {
        val generation = ++loadGeneration
        val hasVisibleStatistics = uiState is TripStatisticsUiState.Success
        if (!hasVisibleStatistics) {
            uiState = TripStatisticsUiState.Loading
        }

        val result = tripStatisticsRepository.getStatistics()
        if (generation != loadGeneration) return
        val statistics = result.getOrElse { error ->
            if (!hasVisibleStatistics) {
                uiState = error.toUiState("여행 통계를 불러오지 못했습니다.")
            }
            return
        }

        uiState = statistics.toUiState()
    }

    private fun Throwable.toUiState(fallbackMessage: String) = TripStatisticsUiState.Error(
        message = message?.takeIf(String::isNotBlank) ?: fallbackMessage,
    )
}

private fun com.mapmory.shared.domain.model.TripStatistics.toUiState() =
    TripStatisticsUiState.Success(
        TripStatisticsUiModel(
            travelerName = DefaultTravelerName,
            recordCount = recordCount.toUiCount(),
            photoCount = mediaCount.toUiCount(),
            worldVisitedCount = visitedCountryCount.toUiCount(),
            koreaVisitedCount = visitedKoreaDistrictCount.toUiCount(),
            visitedCountryCodes = visitedCountryCodes.toSet(),
            topLocations = topRegions.map { region ->
                TopLocationUiModel(
                    locationName = region.name,
                    visitCount = region.recordCount.toUiCount(),
                )
            },
        ),
    )

private fun Long.toUiCount(): Int = coerceIn(0, Int.MAX_VALUE.toLong()).toInt()

private const val DefaultTravelerName = "여행자"
