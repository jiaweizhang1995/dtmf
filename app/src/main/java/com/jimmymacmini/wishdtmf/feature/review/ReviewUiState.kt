package com.jimmymacmini.wishdtmf.feature.review

import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto

/**
 * Stable UI model for the review screen.
 *
 * [selectedPhotoIds] is the review-local subset the user has chosen to delete.
 * It starts equal to all staged IDs and can be narrowed by deselecting individual tiles.
 * [stagedPhotos] is the resolved display list; it may be empty while MediaStore lookup completes.
 * [isLoading] is true while the initial MediaStore resolution is in flight.
 */
data class ReviewUiState(
    val stagedPhotos: List<ReviewPhoto> = emptyList(),
    val selectedPhotoIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
) {
    /** Number of photos currently selected for deletion. */
    val selectedCount: Int get() = selectedPhotoIds.size

    /** True when at least one photo is selected so the delete CTA should be enabled. */
    val isDeleteEnabled: Boolean get() = selectedPhotoIds.isNotEmpty()

    /** Copy-driven destructive question reflecting the current selected subset. */
    val destructivePromptText: String
        get() = if (selectedCount == 1) "永久删除 1 项？" else "永久删除 ${selectedCount} 项？"

    /** Whether the given photo ID is currently selected for deletion. */
    fun isSelected(photoId: Long): Boolean = photoId in selectedPhotoIds
}
