package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.jimmymacmini.wishdtmf.domain.LaunchSession

@Composable
fun MainRoute(
    session: LaunchSession,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MainViewModel = viewModel(
        key = session.photos.joinToString(separator = "-") { it.id.toString() },
        factory = MainViewModel.factory(session),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    MainScreen(
        uiState = uiState.value,
        onStageCurrentPhoto = viewModel::stageCurrentPhoto,
        onSkipCurrentPhoto = viewModel::skipCurrentPhoto,
        onAdvance = onAdvance,
        modifier = modifier,
    )
}
