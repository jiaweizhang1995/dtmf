package com.jimmymacmini.wishdtmf.feature.main

import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            val photoItems = session.photos.mapIndexed { index, photo ->
                MainPhotoUiModel(
                    id = photo.id,
                    contentUri = photo.contentUri,
                    thumbnailContentDescription = "Thumbnail ${index + 1}",
                    heroContentDescription = "Photo ${index + 1}",
                    isCurrent = index == session.currentIndex,
                )
            }
            val currentPhoto = photoItems[session.currentIndex]
            val sourcePhoto = session.photos[session.currentIndex]

            return MainUiState(
                title = buildTitle(sourcePhoto),
                currentPositionLabel = "${session.currentIndex + 1}/${session.photoCount}",
                fileSizeLabel = sourcePhoto.sizeBytes.toReadableFileSize(),
                mimeTypeLabel = sourcePhoto.mimeType
                    ?.substringAfterLast('/')
                    ?.uppercase(Locale.US)
                    ?: "JPG",
                photos = photoItems,
                currentPhoto = currentPhoto,
                visibleThumbnails = photoItems.thumbnailWindow(session.currentIndex),
            )
        }

        private fun buildTitle(photo: LocalPhoto): String {
            val timestamp = photo.dateTakenMillis ?: (photo.dateAddedSeconds.takeIf { it > 0 }?.times(1000))
            if (timestamp == null) {
                return "Recent photos"
            }

            return SimpleDateFormat("MMMM yyyy", Locale.US)
                .format(Date(timestamp))
                .replaceFirstChar { char -> char.titlecase(Locale.US) }
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

private fun List<MainPhotoUiModel>.thumbnailWindow(
    currentIndex: Int,
    radius: Int = 3,
): List<MainPhotoUiModel> {
    val start = (currentIndex - radius).coerceAtLeast(0)
    val end = (currentIndex + radius).coerceAtMost(lastIndex)
    return subList(start, end + 1)
}

private fun Long.toReadableFileSize(): String {
    if (this <= 0L) {
        return "Unknown size"
    }

    val megabytes = this / (1024f * 1024f)
    return "${DecimalFormat("0.#").format(megabytes)} MB"
}
