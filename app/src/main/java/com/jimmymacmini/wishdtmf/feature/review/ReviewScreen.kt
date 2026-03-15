package com.jimmymacmini.wishdtmf.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto

// ---------------------------------------------------------------------------
// Test tags
// ---------------------------------------------------------------------------

object ReviewScreenTags {
    const val Root = "review_root"
    const val BackButton = "review_back_button"
    const val Title = "review_title"
    const val DestructivePrompt = "review_destructive_prompt"
    const val HelperLink = "review_helper_link"
    const val PhotoGrid = "review_photo_grid"
    const val BottomHelper = "review_bottom_helper"
    const val DeleteForeverButton = "review_delete_forever_button"

    /** Prefix: append the photo ID to get the per-tile tag. */
    const val PhotoTilePrefix = "review_photo_tile_"

    /** Prefix: append the photo ID to get the per-tile check badge tag. */
    const val CheckBadgePrefix = "review_check_badge_"

    /** Tag for the empty-grid message shown when both staged lists are empty and not loading. */
    const val EmptyGridMessage = "review_empty_grid_message"
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun ReviewScreen(
    /** Raw staged photo IDs used for semantics so existing nav-graph tests keep passing. */
    stagedPhotoIds: List<Long>,
    /** Full review-local UI state; drives selection affordances, copy, and CTA enablement. */
    uiState: ReviewUiState,
    onBack: () -> Unit,
    onTogglePhotoSelection: (Long) -> Unit = {},
    onDeleteForever: () -> Unit = {},
) {
    val T = ReviewScreenTokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(T.BackgroundColor)
            .statusBarsPadding()
            .semantics {
                // Keep the same semantics contract that nav-graph tests assert on.
                stateDescription = stagedPhotoIds.joinToString(
                    prefix = "staged:",
                    separator = ",",
                )
            }
            .testTag(ReviewScreenTags.Root),
    ) {
        // ---- App bar ----
        ReviewAppBar(
            onBack = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = T.HorizontalPadding,
                    vertical = T.AppBarTopPadding,
                ),
        )

        // ---- Destructive prompt section ----
        // Count and copy are driven by selected subset, not total staged set.
        DestructivePromptSection(
            promptText = if (uiState.isLoading) {
                // While loading, fall back to the total count from staged IDs.
                val count = stagedPhotoIds.size
                if (count == 1) "Permanently delete 1 item?" else "Permanently delete $count items?"
            } else {
                uiState.destructivePromptText
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = T.HorizontalPadding),
        )

        Spacer(modifier = Modifier.height(T.SectionSpacing))

        // ---- Photo grid ----
        LazyVerticalGrid(
            columns = GridCells.Fixed(T.GridColumns),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = T.HorizontalPadding)
                .weight(1f)
                .testTag(ReviewScreenTags.PhotoGrid),
            horizontalArrangement = Arrangement.spacedBy(T.GridSpacing),
            verticalArrangement = Arrangement.spacedBy(T.GridSpacing),
        ) {
            if (uiState.stagedPhotos.isNotEmpty()) {
                items(uiState.stagedPhotos, key = { it.id }) { photo ->
                    ReviewPhotoTile(
                        photo = photo,
                        isSelected = uiState.isSelected(photo.id),
                        onToggle = { onTogglePhotoSelection(photo.id) },
                    )
                }
            } else if (stagedPhotoIds.isNotEmpty()) {
                // Still loading or MediaStore lookup pending — render placeholder tiles so the
                // grid shape is visible while resolution completes.
                items(stagedPhotoIds, key = { it }) { id ->
                    PlaceholderTile(id = id)
                }
            } else if (!uiState.isLoading && stagedPhotoIds.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "No photos staged for deletion.",
                        color = T.SubtleTextColor,
                        modifier = Modifier
                            .padding(T.HorizontalPadding)
                            .testTag(ReviewScreenTags.EmptyGridMessage),
                    )
                }
            }
        }

        // ---- Bottom area ----
        BottomActionArea(
            isDeleteEnabled = uiState.isDeleteEnabled,
            onDeleteForever = onDeleteForever,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = T.HorizontalPadding,
                    vertical = T.BottomBarBottomPadding,
                ),
        )
    }
}

// ---------------------------------------------------------------------------
// App bar
// ---------------------------------------------------------------------------

@Composable
private fun ReviewAppBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val T = ReviewScreenTokens
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back affordance
        Text(
            text = "<",
            color = T.OnSurfaceColor,
            fontSize = T.AppBarTitleSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onBack)
                .testTag(ReviewScreenTags.BackButton)
                .semantics { role = Role.Button },
        )

        Spacer(modifier = Modifier.width(T.AppBarIconTitleGap))

        Text(
            text = T.AppBarTitle,
            color = T.OnSurfaceColor,
            fontSize = T.AppBarTitleSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(ReviewScreenTags.Title),
        )
    }
}

// ---------------------------------------------------------------------------
// Destructive prompt
// ---------------------------------------------------------------------------

@Composable
private fun DestructivePromptSection(
    promptText: String,
    modifier: Modifier = Modifier,
) {
    val T = ReviewScreenTokens
    Column(modifier = modifier) {
        // "Permanently delete N items?" with left teal border
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(T.PromptBorderWidth)
                    .height(32.dp)
                    .background(T.PromptBorderColor),
            )
            Spacer(modifier = Modifier.width(T.PromptBorderTextGap))
            Text(
                text = promptText,
                color = T.OnSurfaceColor,
                fontSize = T.PromptHeadingSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag(ReviewScreenTags.DestructivePrompt),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Helper link below the prompt
        Text(
            text = T.HelperLinkText,
            color = T.AccentTeal,
            fontSize = T.HelperLinkSize,
            modifier = Modifier
                .testTag(ReviewScreenTags.HelperLink)
                .clickable { /* Move-to-trash flow — Phase 5 deferred */ },
        )
    }
}

// ---------------------------------------------------------------------------
// Photo tiles
// ---------------------------------------------------------------------------

@Composable
private fun ReviewPhotoTile(
    photo: ReviewPhoto,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val T = ReviewScreenTokens
    Box(
        modifier = modifier
            .aspectRatio(T.TileAspectRatio)
            .clip(RoundedCornerShape(T.TileCornerRadius))
            .let { m ->
                if (isSelected) {
                    m.border(
                        width = 2.dp,
                        color = T.TileSelectedBorderColor,
                        shape = RoundedCornerShape(T.TileCornerRadius),
                    )
                } else m
            }
            .clickable(onClick = onToggle)
            .testTag("${ReviewScreenTags.PhotoTilePrefix}${photo.id}")
            .semantics {
                stateDescription = if (isSelected) "selected" else "deselected"
                role = Role.Checkbox
            },
    ) {
        AsyncImage(
            model = photo.contentUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                // Dim tiles that are deselected (kept) so selected-for-delete items stand out.
                .alpha(if (isSelected) 1f else 0.4f),
        )

        // Checkmark badge — top-left, visible only when selected
        if (isSelected) {
            CheckBadge(
                photoId = photo.id,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
            )
        }
    }
}

@Composable
private fun PlaceholderTile(
    id: Long,
    modifier: Modifier = Modifier,
) {
    val T = ReviewScreenTokens
    Box(
        modifier = modifier
            .aspectRatio(T.TileAspectRatio)
            .clip(RoundedCornerShape(T.TileCornerRadius))
            .background(T.SurfaceColor)
            .testTag("${ReviewScreenTags.PhotoTilePrefix}${id}"),
    )
}

@Composable
private fun CheckBadge(
    photoId: Long,
    modifier: Modifier = Modifier,
) {
    val T = ReviewScreenTokens
    Box(
        modifier = modifier
            .size(T.CheckBadgeSize)
            .clip(RoundedCornerShape(T.CheckBadgeCornerRadius))
            .background(T.CheckBadgeBackground)
            .testTag("${ReviewScreenTags.CheckBadgePrefix}${photoId}"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "\u2713", // checkmark character
            color = T.CheckBadgeTint,
            fontSize = T.CheckIconSize.value.let {
                androidx.compose.ui.unit.TextUnit(it, androidx.compose.ui.unit.TextUnitType.Sp)
            },
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---------------------------------------------------------------------------
// Bottom action area
// ---------------------------------------------------------------------------

@Composable
private fun BottomActionArea(
    isDeleteEnabled: Boolean,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val T = ReviewScreenTokens
    Column(modifier = modifier) {
        // Helper copy
        Text(
            text = T.BottomHelperText,
            color = T.SubtleTextColor,
            fontSize = T.BottomHelperSize,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .testTag(ReviewScreenTags.BottomHelper),
        )

        // "Delete forever" — full width, centered; disabled (dimmed, non-interactive) when nothing is selected
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(T.CtaCornerRadius))
                .background(if (isDeleteEnabled) T.AccentTeal else T.DecideLaterColor)
                .let { m ->
                    if (isDeleteEnabled) m.clickable(onClick = onDeleteForever) else m
                }
                .padding(vertical = T.CtaButtonVerticalPadding)
                .testTag(ReviewScreenTags.DeleteForeverButton)
                .semantics {
                    stateDescription = if (isDeleteEnabled) "enabled" else "disabled"
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = T.DeleteForeverLabel,
                color = if (isDeleteEnabled) T.OnSurfaceColor else T.SubtleTextColor,
                fontSize = T.CtaLabelSize,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
