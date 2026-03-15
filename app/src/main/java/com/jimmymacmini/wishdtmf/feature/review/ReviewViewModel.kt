package com.jimmymacmini.wishdtmf.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Review-local state owner for selection and delete-action readiness.
 *
 * Receives the initial ordered staged-photo payload and, once MediaStore resolution
 * completes, initialises [selectedPhotoIds] to include every staged item. The user
 * can then deselect items they want to keep; those changes stay isolated here and do
 * NOT mutate [com.jimmymacmini.wishdtmf.feature.main.MainViewModel] staged state until
 * plan 04-03 wires destructive deletion.
 *
 * Selected IDs are persisted through [SavedStateHandle] so rotation does not reset
 * the user's keep/delete choices.
 */
class ReviewViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    /**
     * Called by [ReviewRoute] once MediaStore resolution completes.
     *
     * If a prior selection set was persisted through [SavedStateHandle] AND the resolved
     * photos match the persisted set exactly, the prior selection is restored. Otherwise
     * all photos are initialised as selected.
     */
    fun onPhotosResolved(photos: List<ReviewPhoto>) {
        val resolvedIds = photos.map { it.id }.toSet()

        val restoredIds: Set<Long>? = savedStateHandle
            .get<LongArray>(KEY_SELECTED_IDS)
            ?.toSet()
            ?.intersect(resolvedIds)
            // Only restore if the persisted anchored set is a subset of resolved IDs (handles
            // scenarios where MediaStore deletes photos between sessions — such IDs are dropped).

        val selectedIds = if (restoredIds != null) {
            restoredIds
        } else {
            // No prior persisted selection — all staged photos start selected.
            resolvedIds
        }

        savedStateHandle[KEY_SELECTED_IDS] = selectedIds.toLongArray()

        _uiState.update {
            ReviewUiState(
                stagedPhotos = photos,
                selectedPhotoIds = selectedIds,
                isLoading = false,
            )
        }
    }

    /**
     * Toggle the selection state of a single photo tile.
     *
     * If the photo is currently selected, it moves to "keep" (deselected).
     * If it is currently deselected, it is moved back to "delete" (reselected).
     * The underlying staged set in [MainViewModel] is NOT mutated here.
     */
    fun togglePhotoSelection(photoId: Long) {
        _uiState.update { current ->
            val newIds = if (photoId in current.selectedPhotoIds) {
                current.selectedPhotoIds - photoId
            } else {
                current.selectedPhotoIds + photoId
            }
            savedStateHandle[KEY_SELECTED_IDS] = newIds.toLongArray()
            current.copy(selectedPhotoIds = newIds)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return ReviewViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                    ) as T
                }
            }
        }
    }
}

private const val KEY_SELECTED_IDS = "review_selected_ids"
