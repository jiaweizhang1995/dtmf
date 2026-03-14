package com.jimmymacmini.wishdtmf.feature.main

import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.SwipeDecision
import com.jimmymacmini.wishdtmf.domain.SwipeSessionState

data class MainUiState(
    val title: String,
    val currentPositionLabel: String,
    val fileSizeLabel: String,
    val mimeTypeLabel: String,
    val activePhotoIndex: Int,
    val photos: List<MainPhotoUiModel>,
    val activePhoto: MainPhotoUiModel,
    val visibleThumbnails: List<MainPhotoUiModel>,
    val stagedPhotoIds: Set<Long>,
    val lastDecision: SwipeDecision?,
    val isSessionComplete: Boolean,
) {
    val currentPhoto: MainPhotoUiModel
        get() = activePhoto

    companion object {
        fun fromSession(
            session: LaunchSession,
            swipeState: SwipeSessionState = SwipeSessionState(currentIndex = session.currentIndex),
        ): MainUiState {
            return fromPresentation(
                presentation = PhotoPresentationMapper.map(session.withCurrentIndex(swipeState.currentIndex)),
                swipeState = swipeState,
            )
        }

        fun fromPresentation(
            presentation: MainPresentationState,
            swipeState: SwipeSessionState = SwipeSessionState(),
        ): MainUiState {
            val photos = presentation.photos.map { it.toUiModel() }
            return MainUiState(
                title = presentation.title,
                currentPositionLabel = presentation.currentPositionLabel,
                fileSizeLabel = presentation.fileSizeLabel,
                mimeTypeLabel = presentation.mimeTypeLabel,
                activePhotoIndex = presentation.activePhotoIndex,
                photos = photos,
                activePhoto = photos[presentation.activePhotoIndex],
                visibleThumbnails = presentation.visibleThumbnails.map { visiblePhoto ->
                    photos.first { it.id == visiblePhoto.id }
                },
                stagedPhotoIds = swipeState.stagedPhotoIds,
                lastDecision = swipeState.lastDecision,
                isSessionComplete = swipeState.isSessionComplete,
            )
        }
    }
}

data class MainPhotoUiModel(
    val id: Long,
    val contentUri: String,
    val thumbnailContentDescription: String,
    val heroContentDescription: String,
    val isCurrent: Boolean,
)

private fun MainPhotoPresentation.toUiModel(): MainPhotoUiModel = MainPhotoUiModel(
    id = id,
    contentUri = contentUri,
    thumbnailContentDescription = thumbnailContentDescription,
    heroContentDescription = heroContentDescription,
    isCurrent = isCurrent,
)
