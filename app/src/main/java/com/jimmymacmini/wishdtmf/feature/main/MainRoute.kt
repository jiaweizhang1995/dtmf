package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import com.jimmymacmini.wishdtmf.app.navigation.DELETED_PHOTO_IDS_KEY
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainRoute(
    session: LaunchSession,
    onOpenReview: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    backStackEntry: NavBackStackEntry? = null,
) {
    val sessionKey = session.photos.joinToString(separator = "-") { it.id.toString() }
    val viewModel: MainViewModel = viewModel(
        key = "main-route-$sessionKey",
        factory = MainViewModel.factory(session),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Consume confirmed-deleted IDs relayed from the review result via SavedStateHandle.
    // Clears the now-stale swipe session so the app does not resume with deleted media refs.
    LaunchedEffect(backStackEntry) {
        val deletedIds = backStackEntry
            ?.savedStateHandle
            ?.remove<LongArray>(DELETED_PHOTO_IDS_KEY)
            ?.toSet()
            .orEmpty()
        if (deletedIds.isNotEmpty()) {
            viewModel.onDeleteConfirmed(deletedIds)
        }
    }

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
