package com.jimmymacmini.wishdtmf.feature.review

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.jimmymacmini.wishdtmf.data.media.DeleteRequestCoordinator
import com.jimmymacmini.wishdtmf.data.media.MediaStorePhotoRepository
import kotlinx.coroutines.flow.collectLatest

/**
 * Review route entry point.
 *
 * Receives a deterministic ordered list of staged photo IDs from the navigation back-stack and
 * resolves them into [ReviewPhoto][com.jimmymacmini.wishdtmf.data.media.ReviewPhoto] display models
 * at the review boundary through [MediaStorePhotoRepository]. Once resolved, [ReviewViewModel]
 * is initialised with all photos selected for deletion.
 *
 * The route owns the activity-result launcher for the Android system delete confirmation flow.
 * On success, [onDeleteConfirmed] is called with the IDs that were deleted so the nav layer
 * can clear the stale swipe session and trigger a post-delete refresh.
 * On cancel, review state is left unchanged.
 */
@Composable
fun ReviewRoute(
    stagedPhotoIds: List<Long>,
    onBack: () -> Unit,
    onDeleteConfirmed: (Set<Long>) -> Unit = {},
) {
    val context = LocalContext.current

    val repository = remember(context) {
        MediaStorePhotoRepository(contentResolver = context.contentResolver)
    }

    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModel.factory(),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // Activity-result launcher for the platform delete confirmation dialog.
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (DeleteRequestCoordinator.isDeleteConfirmed(result.resultCode)) {
            // Platform confirmed deletion — relay to ViewModel so it emits DeleteConfirmed.
            viewModel.onDeleteConfirmed(viewModel.uiState.value.selectedPhotoIds)
        }
        // Cancel: do nothing — review state is unchanged.
    }

    // Consume one-shot events from the ViewModel.
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ReviewEvent.RequestDelete -> {
                    // Resolve the selected IDs to URIs, then launch the system delete dialog.
                    val uris = repository.resolveUrisForDelete(event.selectedPhotoIds)
                    val request = DeleteRequestCoordinator.buildDeleteRequest(context, uris)
                    if (request != null) {
                        deleteLauncher.launch(request)
                    }
                    // If request is null (empty selection slipped through), no-op.
                }

                is ReviewEvent.DeleteConfirmed -> {
                    onDeleteConfirmed(event.deletedPhotoIds)
                }
            }
        }
    }

    LaunchedEffect(stagedPhotoIds) {
        val photos = repository.loadReviewPhotos(stagedPhotoIds)
        viewModel.onPhotosResolved(photos)
    }

    ReviewScreen(
        stagedPhotoIds = stagedPhotoIds,
        uiState = uiState.value,
        onBack = onBack,
        onTogglePhotoSelection = viewModel::togglePhotoSelection,
        onDeleteForever = viewModel::onDeleteForever,
    )
}
