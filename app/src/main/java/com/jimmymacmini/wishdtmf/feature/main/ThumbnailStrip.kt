package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ThumbnailStrip(
    photos: List<MainPhotoUiModel>,
    modifier: Modifier = Modifier,
) {
    val thumbnailWidth = MainScreenTokens.thumbnailWidth
    val spacing = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MainScreenTags.ThumbnailRail),
    ) {
        // Number of items (thumbnails + ellipsis) that fit in the available width.
        // N items: N*thumbnailWidth + (N-1)*spacing <= maxWidth
        // → N <= (maxWidth + spacing) / (thumbnailWidth + spacing)
        val maxCount = ((maxWidth + spacing) / (thumbnailWidth + spacing)).toInt()
        val slotsForPhotos = (maxCount - 1).coerceAtLeast(1) // one slot reserved for ellipsis

        val showEllipsis = photos.size > maxCount
        val activeIndex = photos.indexOfFirst { it.isCurrent }.coerceAtLeast(0)

        val visiblePhotos: List<MainPhotoUiModel> = when {
            !showEllipsis -> photos
            activeIndex < slotsForPhotos -> {
                // Active photo is within the natural visible range — show first N slots + ellipsis.
                photos.take(slotsForPhotos)
            }
            else -> {
                // Active photo is in the overflow region — pin it as the last visible slot
                // so the yellow border is always visible.
                photos.take(slotsForPhotos - 1) + photos[activeIndex]
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            visiblePhotos.forEach { photo ->
                ThumbnailBox(photo = photo)
            }
            if (showEllipsis) {
                Box(
                    modifier = Modifier
                        .size(width = thumbnailWidth, height = MainScreenTokens.thumbnailHeight)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MainScreenTokens.mutedSurface)
                        .border(1.dp, MainScreenTokens.thumbnailInactiveBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "•••",
                        color = MainScreenTokens.secondaryText,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailBox(photo: MainPhotoUiModel) {
    Box(
        modifier = Modifier
            .size(
                width = MainScreenTokens.thumbnailWidth,
                height = MainScreenTokens.thumbnailHeight,
            )
            .clip(RoundedCornerShape(10.dp))
            .background(MainScreenTokens.mutedSurface)
            .border(
                width = if (photo.isCurrent) 2.dp else 1.dp,
                color = if (photo.isCurrent) {
                    MainScreenTokens.thumbnailBorder
                } else {
                    MainScreenTokens.thumbnailInactiveBorder
                },
                shape = RoundedCornerShape(10.dp),
            )
            .semantics { contentDescription = photo.thumbnailContentDescription }
            .alpha(if (photo.isCurrent) 1f else 0.7f)
            .testTag(thumbnailTag(photo.id)),
    ) {
        AsyncImage(
            model = photo.contentUri,
            contentDescription = photo.thumbnailContentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
