package com.jimmymacmini.wishdtmf.feature.entry

sealed interface LaunchUiState {
    data class NeedsPermission(
        val showSettingsHint: Boolean = false,
    ) : LaunchUiState

    data object LoadingBatch : LaunchUiState

    data class Ready(
        val photoCount: Int,
    ) : LaunchUiState

    data object Empty : LaunchUiState

    data class Error(
        val message: String,
    ) : LaunchUiState
}
