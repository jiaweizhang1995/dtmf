package com.jimmymacmini.wishdtmf.feature.entry

import com.jimmymacmini.wishdtmf.domain.LaunchSession

sealed interface LaunchUiState {
    data class NeedsPermission(
        val showSettingsHint: Boolean = false,
    ) : LaunchUiState

    data object LoadingBatch : LaunchUiState

    data class Ready(
        val session: LaunchSession,
    ) : LaunchUiState

    data object Empty : LaunchUiState

    data class Error(
        val message: String,
    ) : LaunchUiState
}
