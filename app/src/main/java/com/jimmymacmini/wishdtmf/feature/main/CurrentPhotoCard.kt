package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun CurrentPhotoCard(
    photo: MainPhotoUiModel,
    heroAspectRatio: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .aspectRatio(heroAspectRatio)
            .clip(RoundedCornerShape(MainScreenTokens.heroCornerRadius))
            .background(Color(0xFF2A261F))
            .testTag(MainScreenTags.HeroPhoto)
            .semantics { contentDescription = photo.heroContentDescription },
    ) {
        key(photo.id) {
            AsyncImage(
                model = photo.contentUri,
                contentDescription = photo.heroContentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainScreenTokens.heroGradient),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(MainScreenTokens.heroOverlaySize)
                .clip(RoundedCornerShape(14.dp))
                .background(MainScreenTokens.chromeSurface.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "◪",
                color = MainScreenTokens.primaryText,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            text = photo.heroContentDescription,
            color = MainScreenTokens.primaryText,
        )
    }
}
