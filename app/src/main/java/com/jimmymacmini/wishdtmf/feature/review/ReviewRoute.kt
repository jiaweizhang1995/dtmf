package com.jimmymacmini.wishdtmf.feature.review

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun ReviewRoute(
    stagedPhotoIds: List<Long>,
    onBack: () -> Unit,
) {
    val uiState = remember(stagedPhotoIds) {
        ReviewUiState(
            title = "Review staged photos",
            subtitle = if (stagedPhotoIds.size == 1) {
                "1 photo is ready for the delete staging area."
            } else {
                "${stagedPhotoIds.size} photos are ready for the delete staging area."
            },
            stagedPhotoIds = stagedPhotoIds,
        )
    }

    ReviewScreen(
        uiState = uiState,
        onBack = onBack,
    )
}

data class ReviewUiState(
    val title: String,
    val subtitle: String,
    val stagedPhotoIds: List<Long>,
)
