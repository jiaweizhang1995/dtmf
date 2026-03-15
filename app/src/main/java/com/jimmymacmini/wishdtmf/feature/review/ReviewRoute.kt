package com.jimmymacmini.wishdtmf.feature.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.jimmymacmini.wishdtmf.data.media.MediaStorePhotoRepository

/**
 * Review route entry point.
 *
 * Receives a deterministic ordered list of staged photo IDs from the navigation back-stack and
 * resolves them into [ReviewPhoto][com.jimmymacmini.wishdtmf.data.media.ReviewPhoto] display models
 * at the review boundary through [MediaStorePhotoRepository]. Once resolved, [ReviewViewModel]
 * is initialised with all photos selected for deletion.
 *
 * Selection toggles stay review-local; [com.jimmymacmini.wishdtmf.feature.main.MainViewModel]
 * staged state is not mutated until plan 04-03 wires destructive deletion.
 */
@Composable
fun ReviewRoute(
    stagedPhotoIds: List<Long>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModel.factory(),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(stagedPhotoIds) {
        val repository = MediaStorePhotoRepository(
            contentResolver = context.contentResolver,
        )
        val photos = repository.loadReviewPhotos(stagedPhotoIds)
        viewModel.onPhotosResolved(photos)
    }

    ReviewScreen(
        stagedPhotoIds = stagedPhotoIds,
        uiState = uiState.value,
        onBack = onBack,
        onTogglePhotoSelection = viewModel::togglePhotoSelection,
    )
}
