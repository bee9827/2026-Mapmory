package com.mapmory.shared.presentation.triprecord.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapmory.shared.domain.repository.TripStatisticsRepository
import com.mapmory.shared.logging.mapmoryDebugLog
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

    init {
        cachedStatistics?.let { statistics ->
            mapmoryDebugLog(
                StatisticsLogTag,
                "cache " +
                    "visitedCountryCount=${statistics.visitedCountryCount}, " +
                    "visitedKoreaDistrictCount=${statistics.visitedKoreaDistrictCount}",
            )
        }
    }

    suspend fun refresh() {
        val generation = ++loadGeneration
        val hasVisibleStatistics = uiState is TripStatisticsUiState.Success
        mapmoryDebugLog(
            StatisticsLogTag,
            "refresh start generation=$generation, hasCachedState=$hasVisibleStatistics",
        )
        if (!hasVisibleStatistics) {
            uiState = TripStatisticsUiState.Loading
        }

        val result = tripStatisticsRepository.getStatistics()
        if (generation != loadGeneration) {
            mapmoryDebugLog(StatisticsLogTag, "refresh ignored generation=$generation")
            return
        }
        val statistics = result.getOrElse { error ->
            mapmoryDebugLog(
                StatisticsLogTag,
                "refresh failed, retainedCache=$hasVisibleStatistics: " +
                    "${error::class.simpleName}: ${error.message}",
            )
            if (!hasVisibleStatistics) {
                uiState = error.toUiState("여행 통계를 불러오지 못했습니다.")
            }
            return
        }

        val nextState = statistics.toUiState()
        uiState = nextState
        mapmoryDebugLog(
            StatisticsLogTag,
            "ui state " +
                "worldVisitedCount=${nextState.statistics.worldVisitedCount}, " +
                "koreaVisitedCount=${nextState.statistics.koreaVisitedCount}, " +
                "recordCount=${nextState.statistics.recordCount}",
        )
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
private const val StatisticsLogTag = "MapmoryStatistics"
