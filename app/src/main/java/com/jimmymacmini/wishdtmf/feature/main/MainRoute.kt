package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jimmymacmini.wishdtmf.domain.LaunchSession

@Composable
fun MainRoute(
    session: LaunchSession,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState = remember(session) { MainUiState.fromSession(session) }

    MainRouteScaffold(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
private fun MainRouteScaffold(
    uiState: MainUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MainScreenTokens.appBackground)
            .padding(MainScreenTokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(MainScreenTokens.contentSpacing),
    ) {
        TopBar(title = uiState.title)
        ThumbnailStrip(uiState = uiState)
        MetadataRow(uiState = uiState)
        HeroPlaceholder(photo = uiState.currentPhoto)
        ActionRow()
        FooterRow()
        ProceedAffordance()
    }
}

@Composable
private fun TopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MainScreenTokens.topBarHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "‹", color = MainScreenTokens.primaryText)
        Text(
            text = title,
            color = MainScreenTokens.primaryText,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = "⋮", color = MainScreenTokens.primaryText)
    }
}

@Composable
private fun ThumbnailStrip(uiState: MainUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        uiState.visibleThumbnails.forEach { photo ->
            Box(
                modifier = Modifier
                    .size(
                        width = MainScreenTokens.thumbnailWidth,
                        height = MainScreenTokens.thumbnailHeight,
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(MainScreenTokens.mutedSurface)
                    .border(
                        width = if (photo.isCurrent) 2.dp else 1.dp,
                        color = if (photo.isCurrent) {
                            MainScreenTokens.thumbnailBorder
                        } else {
                            MainScreenTokens.thumbnailInactiveBorder
                        },
                        shape = RoundedCornerShape(8.dp),
                    ),
            )
        }
    }
}

@Composable
private fun MetadataRow(uiState: MainUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            .clip(RoundedCornerShape(MainScreenTokens.metadataCornerRadius))
            .background(MainScreenTokens.chromeSurface)
            .height(MainScreenTokens.metadataHeight)
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
private fun HeroPlaceholder(photo: MainPhotoUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(MainScreenTokens.heroCornerRadius))
            .background(Color(0xFF2A261F)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(MainScreenTokens.heroOverlaySize)
                .clip(RoundedCornerShape(12.dp))
                .background(MainScreenTokens.chromeSurface),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "◻", color = MainScreenTokens.primaryText)
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
private fun ActionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MainScreenTokens.actionRowBottomPadding),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(symbol = "⌫", label = "Delete", background = MainScreenTokens.destructiveAction)
        ActionButton(symbol = "↶", label = "Undo", background = MainScreenTokens.neutralAction)
        ActionButton(symbol = "»", label = "Skip", background = MainScreenTokens.neutralAction)
        ActionButton(symbol = "✓", label = "", background = MainScreenTokens.confirmationAction)
    }
}

@Composable
private fun ActionButton(
    symbol: String,
    label: String,
    background: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(MainScreenTokens.actionButtonSize)
                .clip(RoundedCornerShape(32.dp))
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = symbol, color = MainScreenTokens.primaryText)
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = MainScreenTokens.secondaryText,
            )
        }
    }
}

@Composable
private fun FooterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MainScreenTokens.footerRowHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(MainScreenTokens.chromeSurface)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Organise into albums",
            color = MainScreenTokens.secondaryText,
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
            )
        }
    }
}

@Composable
private fun ProceedAffordance() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MainScreenTokens.proceedTopPadding),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = "PROCEED →",
            color = MainScreenTokens.proceedText,
            fontWeight = FontWeight.Medium,
        )
    }
}
