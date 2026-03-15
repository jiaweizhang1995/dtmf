package com.jimmymacmini.wishdtmf.feature.main

import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.SwipeDecision
import com.jimmymacmini.wishdtmf.domain.SwipeSessionState

data class MainUiState(
    val currentPositionLabel: String,
    val fileSizeLabel: String,
    val mimeTypeLabel: String,
    val activePhotoIndex: Int,
    val photos: List<MainPhotoUiModel>,
    val activePhoto: MainPhotoUiModel,
    val visibleThumbnails: List<MainPhotoUiModel>,
    val stagedPhotoIds: Set<Long>,
    val stagedPhotoCount: Int,
    val lastDecision: SwipeDecision?,
    val isSessionComplete: Boolean,
    val canUndo: Boolean,
    val canProceed: Boolean,
    val proceedMessage: String,
    val completedMessage: String?,
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
                stagedPhotoCount = swipeState.stagedPhotoIds.size,
                lastDecision = swipeState.lastDecision,
                isSessionComplete = swipeState.isSessionComplete,
                canUndo = swipeState.lastDecision != null,
                canProceed = swipeState.stagedPhotoIds.isNotEmpty(),
                proceedMessage = when {
                    swipeState.stagedPhotoIds.isNotEmpty() -> {
                        "审核已选 ${swipeState.stagedPhotoIds.size} 张"
                    }

                    swipeState.isSessionComplete -> "未选择照片进行审核"
                    else -> "向左滑动照片以加入审核"
                },
                completedMessage = when {
                    !swipeState.isSessionComplete -> null
                    swipeState.stagedPhotoIds.isNotEmpty() -> {
                        "全部已审阅。可审核已选 ${swipeState.stagedPhotoIds.size} 张。"
                    }

                    else -> "全部已审阅。未选择照片进行审核。"
                },
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
