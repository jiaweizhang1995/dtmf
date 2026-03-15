package com.jimmymacmini.wishdtmf.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

object MainScreenTags {
    const val Root = "main_root"
    const val ThumbnailRail = "main_thumbnail_rail"
    const val MetadataRow = "main_metadata_row"
    const val HeroPhoto = "main_hero_photo"
    const val BottomActions = "main_bottom_actions"
    const val UndoAction = "main_undo_action"
    const val ProceedAffordance = "main_proceed_affordance"
    const val ProceedMessage = "main_proceed_message"
    const val SessionCompleteMessage = "main_session_complete_message"
}

fun thumbnailTag(photoId: Long): String = "main_thumbnail_$photoId"

@Composable
fun MainScreen(
    uiState: MainUiState,
    onStageCurrentPhoto: () -> Unit,
    onSkipCurrentPhoto: () -> Unit,
    onUndoLastDecision: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .background(MainScreenTokens.appBackground)
            .statusBarsPadding()
            .testTag(MainScreenTags.Root)
            .semantics {
                stateDescription = buildString {
                    append("current:${uiState.activePhoto.id};")
                    append("staged:${uiState.stagedPhotoIds.sorted().joinToString(",")};")
                    append("complete:${uiState.isSessionComplete}")
                }
            }
            .padding(horizontal = MainScreenTokens.screenPadding, vertical = 14.dp),
    ) {
        val heroAspectRatio = if (maxHeight > 720.dp) 0.74f else 0.8f
        var showThumbnails by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MainScreenTokens.sectionSpacing),
        ) {
            MainTopBar(
                showThumbnails = showThumbnails,
                onToggleThumbnails = { showThumbnails = !showThumbnails },
                canProceed = uiState.canProceed,
                onProceed = onProceed,
            )
            if (showThumbnails) {
                ThumbnailStrip(
                    photos = uiState.photos,
                    activeIndex = uiState.activePhotoIndex,
                )
            }
            MainMetadataRow(uiState = uiState)
            if (uiState.isSessionComplete) {
                SessionCompleteCard(
                    heroAspectRatio = heroAspectRatio,
                    completedMessage = uiState.completedMessage,
                )
            } else {
                SwipePhotoCard(
                    photo = uiState.activePhoto,
                    heroAspectRatio = heroAspectRatio,
                    onStagePhoto = onStageCurrentPhoto,
                    onSkipPhoto = onSkipCurrentPhoto,
                )
            }
            BottomActionRow(
                canUndo = uiState.canUndo,
                onUndoLastDecision = onUndoLastDecision,
                onSkipCurrentPhoto = onSkipCurrentPhoto,
            )
        }
    }
}

@Composable
private fun SessionCompleteCard(
    heroAspectRatio: Float,
    completedMessage: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .aspectRatio(heroAspectRatio)
            .clip(RoundedCornerShape(MainScreenTokens.heroCornerRadius))
            .background(MainScreenTokens.chromeSurface)
            .testTag(MainScreenTags.HeroPhoto),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "全部照片已审阅",
                color = MainScreenTokens.primaryText,
                fontWeight = FontWeight.SemiBold,
            )
            completedMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.testTag(MainScreenTags.SessionCompleteMessage),
                    color = MainScreenTokens.secondaryText,
                )
            }
        }
    }
}

@Composable
private fun MainTopBar(
    showThumbnails: Boolean,
    onToggleThumbnails: () -> Unit,
    canProceed: Boolean,
    onProceed: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MainScreenTokens.topBarHeight)
            .background(MainScreenTokens.appBackground),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "DtMF",
            color = Color(0xFFFFD600),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onProceed,
                enabled = canProceed,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainScreenTokens.proceedSurface,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.testTag(MainScreenTags.ProceedAffordance),
            ) {
                Text(text = "继续", fontWeight = FontWeight.SemiBold)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MainScreenTokens.primaryText,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("显示缩略图") },
                        onClick = {
                            onToggleThumbnails()
                            menuExpanded = false
                        },
                        trailingIcon = if (showThumbnails) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null,
                    )
                }
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
private fun BottomActionRow(
    canUndo: Boolean,
    onUndoLastDecision: () -> Unit,
    onSkipCurrentPhoto: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = MainScreenTokens.actionRowBottomPadding)
            .testTag(MainScreenTags.BottomActions),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        MainActionButton(
            symbol = "↶",
            label = "撤销",
            background = MainScreenTokens.neutralAction,
            onClick = onUndoLastDecision,
            enabled = canUndo,
            modifier = Modifier.testTag(MainScreenTags.UndoAction),
        )
        MainActionButton(
            symbol = "»",
            label = "跳过",
            background = MainScreenTokens.neutralAction,
            onClick = onSkipCurrentPhoto,
        )
    }
}

@Composable
private fun MainActionButton(
    symbol: String,
    label: String?,
    background: Color,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .then(modifier)
                .size(MainScreenTokens.actionButtonSize)
                .clip(CircleShape)
                .background(background)
                .alpha(if (enabled) 1f else 0.45f)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            enabled = enabled,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    }
                ),
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

