package com.mapmory.shared.presentation.map.state

import com.mapmory.shared.presentation.map.domain.MapBoundaryData

sealed interface KoreaMapUiState {
    data object Idle : KoreaMapUiState

    data object Loading : KoreaMapUiState

    data class Success(val data: MapBoundaryData) : KoreaMapUiState

    data class Error(val message: String) : KoreaMapUiState
}
