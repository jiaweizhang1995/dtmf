package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ThumbnailStrip(
    photos: List<MainPhotoUiModel>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MainScreenTags.ThumbnailRail),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        photos.forEach { photo ->
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
            ) {
                AsyncImage(
                    model = photo.contentUri,
                    contentDescription = photo.thumbnailContentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
