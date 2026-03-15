package com.jimmymacmini.wishdtmf.feature.main

import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import java.text.DecimalFormat
import java.util.Locale

data class MainPresentationState(
    val currentPositionLabel: String,
    val fileSizeLabel: String,
    val mimeTypeLabel: String,
    val activePhotoIndex: Int,
    val photos: List<MainPhotoPresentation>,
    val activePhoto: MainPhotoPresentation,
    val visibleThumbnails: List<MainPhotoPresentation>,
) {
    val currentPhoto: MainPhotoPresentation
        get() = activePhoto
}

data class MainPhotoPresentation(
    val id: Long,
    val contentUri: String,
    val thumbnailContentDescription: String,
    val heroContentDescription: String,
    val isCurrent: Boolean,
)

object PhotoPresentationMapper {
    fun map(session: LaunchSession): MainPresentationState {
        val activePhotoIndex = session.currentIndex
        val photoItems = session.photos.mapIndexed { index, photo ->
            MainPhotoPresentation(
                id = photo.id,
                contentUri = photo.contentUri,
                thumbnailContentDescription = "Thumbnail ${index + 1}",
                heroContentDescription = "Photo ${index + 1}",
                isCurrent = index == activePhotoIndex,
            )
        }
        val activePhoto = photoItems[activePhotoIndex]
        val sourcePhoto = session.photos[activePhotoIndex]

        return MainPresentationState(
            currentPositionLabel = "${activePhotoIndex + 1}/${session.photoCount}",
            fileSizeLabel = sourcePhoto.sizeBytes.toReadableFileSize(),
            mimeTypeLabel = sourcePhoto.mimeType
                ?.substringAfterLast('/')
                ?.uppercase(Locale.US)
                ?: "JPG",
            activePhotoIndex = activePhotoIndex,
            photos = photoItems,
            activePhoto = activePhoto,
            visibleThumbnails = photoItems,
        )
    }

}

internal fun deriveThumbnailWindow(
    photos: List<MainPhotoPresentation>,
    currentIndex: Int,
    radius: Int = 3,
): List<MainPhotoPresentation> {
    val start = (currentIndex - radius).coerceAtLeast(0)
    val end = (currentIndex + radius).coerceAtMost(photos.lastIndex)
    return photos.subList(start, end + 1)
}

private fun Long.toReadableFileSize(): String {
    if (this <= 0L) {
        return "Unknown size"
    }

    val megabytes = this / (1024f * 1024f)
    return "${DecimalFormat("0.#").format(megabytes)} MB"
}
