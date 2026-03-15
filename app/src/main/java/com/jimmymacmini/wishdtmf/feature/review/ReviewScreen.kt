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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
    const val DecideLaterButton = "review_decide_later_button"
    const val DeleteForeverButton = "review_delete_forever_button"

    /** Prefix: append the photo ID to get the per-tile tag. */
    const val PhotoTilePrefix = "review_photo_tile_"

    /** Prefix: append the photo ID to get the per-tile check badge tag. */
    const val CheckBadgePrefix = "review_check_badge_"
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun ReviewScreen(
    /** Raw staged photo IDs used for semantics so existing nav-graph tests keep passing. */
    stagedPhotoIds: List<Long>,
    /** Resolved review cards; may be empty while loading or if MediaStore lookup fails. */
    stagedPhotos: List<ReviewPhoto>,
    onBack: () -> Unit,
    onDecideLater: () -> Unit = {},
    onDeleteForever: () -> Unit = {},
) {
    val T = ReviewScreenTokens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(T.BackgroundColor)
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
        DestructivePromptSection(
            count = if (stagedPhotos.isNotEmpty()) stagedPhotos.size else stagedPhotoIds.size,
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
            if (stagedPhotos.isNotEmpty()) {
                items(stagedPhotos, key = { it.id }) { photo ->
                    ReviewPhotoTile(
                        photo = photo,
                        isSelected = true,  // All selected by default; toggling lands in plan 04-02.
                    )
                }
            } else if (!stagedPhotoIds.isEmpty()) {
                // Still loading or MediaStore lookup pending — render placeholder tiles so the
                // grid shape is visible while resolution completes.
                items(stagedPhotoIds, key = { it }) { id ->
                    PlaceholderTile(id = id)
                }
            }
        }

        // ---- Bottom area ----
        BottomActionArea(
            onDecideLater = onDecideLater,
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
    count: Int,
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
            val label = if (count == 1) "1 item" else "$count items"
            Text(
                text = "Permanently delete $label?",
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
            .testTag("${ReviewScreenTags.PhotoTilePrefix}${photo.id}"),
    ) {
        AsyncImage(
            model = photo.contentUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Checkmark badge — top-left, visible when selected
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
            fontSize = T.CheckIconSize.value.let { androidx.compose.ui.unit.TextUnit(it, androidx.compose.ui.unit.TextUnitType.Sp) },
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---------------------------------------------------------------------------
// Bottom action area
// ---------------------------------------------------------------------------

@Composable
private fun BottomActionArea(
    onDecideLater: () -> Unit,
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

        // CTA row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // "Decide Later"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(T.CtaCornerRadius))
                    .background(T.DecideLaterColor)
                    .clickable(onClick = onDecideLater)
                    .padding(vertical = T.CtaButtonVerticalPadding)
                    .testTag(ReviewScreenTags.DecideLaterButton),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = T.DecideLaterLabel,
                    color = T.DecideLaterTextColor,
                    fontSize = T.CtaLabelSize,
                    fontWeight = FontWeight.Medium,
                )
            }

            // "Delete forever"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(T.CtaCornerRadius))
                    .background(T.AccentTeal)
                    .clickable(onClick = onDeleteForever)
                    .padding(vertical = T.CtaButtonVerticalPadding)
                    .testTag(ReviewScreenTags.DeleteForeverButton),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = T.DeleteForeverLabel,
                    color = T.OnSurfaceColor,
                    fontSize = T.CtaLabelSize,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
