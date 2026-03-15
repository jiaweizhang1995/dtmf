package com.jimmymacmini.wishdtmf.feature.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.jimmymacmini.wishdtmf.data.media.MediaStorePhotoRepository
import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto

/**
 * Display model for the review screen. Holds the ordered list of resolved review cards.
 * Selection toggles and delete-progress state will be attached here in later plans.
 */
data class ReviewUiState(
    val stagedPhotos: List<ReviewPhoto> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Review route entry point.
 *
 * Receives a deterministic ordered list of staged photo IDs from the navigation back-stack and
 * resolves them into [ReviewPhoto] display models at the review boundary through
 * [MediaStorePhotoRepository]. This keeps the navigation handoff minimal (IDs only) while
 * ensuring the review grid shows real images rather than placeholder data.
 */
@Composable
fun ReviewRoute(
    stagedPhotoIds: List<Long>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(ReviewUiState()) }

    LaunchedEffect(stagedPhotoIds) {
        val repository = MediaStorePhotoRepository(
            contentResolver = context.contentResolver,
        )
        val photos = repository.loadReviewPhotos(stagedPhotoIds)
        uiState = ReviewUiState(
            stagedPhotos = photos,
            isLoading = false,
        )
    }

    ReviewScreen(
        stagedPhotoIds = stagedPhotoIds,
        stagedPhotos = uiState.stagedPhotos,
        onBack = onBack,
    )
}
