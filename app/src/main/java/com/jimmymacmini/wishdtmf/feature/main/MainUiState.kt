package com.jimmymacmini.wishdtmf.feature.main

import com.jimmymacmini.wishdtmf.domain.LaunchSession

data class MainUiState(
    val title: String,
    val currentPositionLabel: String,
    val fileSizeLabel: String,
    val mimeTypeLabel: String,
    val photos: List<MainPhotoUiModel>,
    val currentPhoto: MainPhotoUiModel,
    val visibleThumbnails: List<MainPhotoUiModel>,
) {
    companion object {
        fun fromSession(session: LaunchSession): MainUiState {
            return fromPresentation(PhotoPresentationMapper.map(session))
        }

        fun fromPresentation(presentation: MainPresentationState): MainUiState {
            return MainUiState(
                title = presentation.title,
                currentPositionLabel = presentation.currentPositionLabel,
                fileSizeLabel = presentation.fileSizeLabel,
                mimeTypeLabel = presentation.mimeTypeLabel,
                photos = presentation.photos.map { it.toUiModel() },
                currentPhoto = presentation.currentPhoto.toUiModel(),
                visibleThumbnails = presentation.visibleThumbnails.map { it.toUiModel() },
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
