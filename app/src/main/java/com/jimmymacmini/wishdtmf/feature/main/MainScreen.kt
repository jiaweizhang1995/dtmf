package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object MainScreenTags {
    const val TopBar = "main_top_bar"
    const val ThumbnailRail = "main_thumbnail_rail"
    const val MetadataRow = "main_metadata_row"
    const val HeroPhoto = "main_hero_photo"
    const val BottomActions = "main_bottom_actions"
    const val BannerRow = "main_banner_row"
    const val ProceedAffordance = "main_proceed_affordance"
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .background(MainScreenTokens.appBackground)
            .padding(horizontal = MainScreenTokens.screenPadding, vertical = 14.dp),
    ) {
        val heroAspectRatio = if (maxHeight > 720.dp) 0.74f else 0.8f

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MainScreenTokens.sectionSpacing),
        ) {
            MainTopBar(title = uiState.title)
            MainThumbnailStrip(uiState = uiState)
            MainMetadataRow(uiState = uiState)
            HeroPhotoCard(
                photo = uiState.currentPhoto,
                heroAspectRatio = heroAspectRatio,
            )
            BottomActionRow()
            PremiumBannerRow()
            ProceedAffordance()
        }
    }
}

@Composable
private fun MainTopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MainScreenTokens.topBarHeight)
            .testTag(MainScreenTags.TopBar),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            color = MainScreenTokens.primaryText,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = title,
            color = MainScreenTokens.primaryText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "⋮",
            color = MainScreenTokens.primaryText,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MainThumbnailStrip(uiState: MainUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MainScreenTags.ThumbnailRail),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.visibleThumbnails.forEachIndexed { index, photo ->
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
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${index + 1}",
                    color = MainScreenTokens.secondaryText,
                    fontWeight = if (photo.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun MainMetadataRow(uiState: MainUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(MainScreenTags.MetadataRow),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetadataChip("i")
        MetadataChip(uiState.fileSizeLabel)
        MetadataChip(uiState.mimeTypeLabel)
        MetadataChip(uiState.currentPositionLabel)
    }
}

@Composable
private fun MetadataChip(label: String) {
    Box(
        modifier = Modifier
            .height(MainScreenTokens.metadataHeight)
            .clip(RoundedCornerShape(MainScreenTokens.metadataCornerRadius))
            .background(MainScreenTokens.chromeSurface)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MainScreenTokens.secondaryText,
        )
    }
}

@Composable
private fun HeroPhotoCard(
    photo: MainPhotoUiModel,
    heroAspectRatio: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .aspectRatio(heroAspectRatio)
            .clip(RoundedCornerShape(MainScreenTokens.heroCornerRadius))
            .background(Color(0xFF2A261F))
            .testTag(MainScreenTags.HeroPhoto),
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MainScreenTokens.heroGradient),
            )
        }
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
            color = MainScreenTokens.secondaryText,
        )
    }
}

@Composable
private fun BottomActionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = MainScreenTokens.actionRowBottomPadding)
            .testTag(MainScreenTags.BottomActions),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        MainActionButton(
            symbol = "⌫",
            label = null,
            background = MainScreenTokens.destructiveAction,
        )
        MainActionButton(
            symbol = "↶",
            label = "Undo",
            background = MainScreenTokens.neutralAction,
        )
        MainActionButton(
            symbol = "»",
            label = "Skip",
            background = MainScreenTokens.neutralAction,
        )
        MainActionButton(
            symbol = "✓",
            label = null,
            background = MainScreenTokens.confirmationAction,
        )
    }
}

@Composable
private fun MainActionButton(
    symbol: String,
    label: String?,
    background: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(MainScreenTokens.actionButtonSize)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                color = MainScreenTokens.primaryText,
                fontWeight = FontWeight.Medium,
            )
        }
        if (label != null) {
            Text(
                text = label,
                color = MainScreenTokens.secondaryText,
            )
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PremiumBannerRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MainScreenTokens.footerRowHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(MainScreenTokens.chromeSurface)
            .padding(horizontal = 12.dp)
            .testTag(MainScreenTags.BannerRow),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Organise into albums",
            color = MainScreenTokens.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MainScreenTokens.premiumAccent)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                text = "PREMIUM",
                color = MainScreenTokens.appBackground,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
private fun ignoreAdvanceCallback(onAdvance: () -> Unit) = Unit

@Composable
private fun ProceedAffordance() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MainScreenTokens.proceedTopPadding)
            .testTag(MainScreenTags.ProceedAffordance),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = "PROCEED  →",
            color = MainScreenTokens.proceedText,
            fontWeight = FontWeight.Medium,
        )
    }
}
