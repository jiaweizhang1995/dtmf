package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainRoute(
    session: LaunchSession,
    onOpenReview: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sessionKey = session.photos.joinToString(separator = "-") { it.id.toString() }
    val viewModel: MainViewModel = viewModel(
        key = "main-route-$sessionKey",
        factory = MainViewModel.factory(session),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is MainNavigationEvent.OpenReview -> onOpenReview(event.stagedPhotoIds)
            }
        }
    }

    MainScreen(
        uiState = uiState.value,
        onStageCurrentPhoto = viewModel::stageCurrentPhoto,
        onSkipCurrentPhoto = viewModel::skipCurrentPhoto,
        onUndoLastDecision = viewModel::undoLastDecision,
        onProceed = viewModel::onProceedToReview,
        modifier = modifier,
    )
}
