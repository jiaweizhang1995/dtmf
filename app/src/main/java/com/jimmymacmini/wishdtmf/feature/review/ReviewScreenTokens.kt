package com.jimmymacmini.wishdtmf.feature.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for all Phase 4 review-screen visual constants.
 *
 * Centralising these values here keeps later tuning localized and prevents screenshot
 * literals from scattering across composable files.
 */
object ReviewScreenTokens {

    // --- Colors ---

    /** Primary background: deep near-black matching the screenshot. */
    val BackgroundColor: Color = Color(0xFF0A0A0A)

    /** App-bar and card surface: slightly lighter than background. */
    val SurfaceColor: Color = Color(0xFF1A1A1A)

    /** Primary text on dark background. */
    val OnSurfaceColor: Color = Color.White

    /** Secondary/muted text color (helper text, captions). */
    val SubtleTextColor: Color = Color(0xFFAAAAAA)

    /** Teal accent used for helper links, checkmark badges, and "Delete forever" CTA. */
    val AccentTeal: Color = Color(0xFF3D9E9E)

    /** Left border accent next to the destructive prompt heading. */
    val PromptBorderColor: Color = Color(0xFF3D9E9E)

    /** Background for the "Decide Later" CTA button. */
    val DecideLaterColor: Color = Color(0xFF2A2A2A)

    /** Text color for "Decide Later" CTA button. */
    val DecideLaterTextColor: Color = Color.White

    /** Checkmark badge background (teal overlay on tile). */
    val CheckBadgeBackground: Color = Color(0xFF3D9E9E)

    /** Checkmark icon tint inside the badge. */
    val CheckBadgeTint: Color = Color.White

    /** Tile border/overlay color for selected photos. */
    val TileSelectedBorderColor: Color = Color(0xFF3D9E9E)

    // --- Spacing ---

    /** Horizontal padding for the root screen layout. */
    val HorizontalPadding: Dp = 16.dp

    /** Top padding below the status bar / app bar. */
    val AppBarTopPadding: Dp = 16.dp

    /** Vertical spacing between major layout sections. */
    val SectionSpacing: Dp = 20.dp

    /** Space between the app-bar back icon and title. */
    val AppBarIconTitleGap: Dp = 12.dp

    /** Bottom padding so the CTA buttons sit above the system nav bar. */
    val BottomBarBottomPadding: Dp = 24.dp

    /** Internal padding for each CTA button. */
    val CtaButtonVerticalPadding: Dp = 18.dp

    /** Left border width for the destructive prompt. */
    val PromptBorderWidth: Dp = 3.dp

    /** Padding between prompt border and prompt text. */
    val PromptBorderTextGap: Dp = 10.dp

    // --- Grid ---

    /** Number of columns in the staged-photo grid. */
    const val GridColumns: Int = 2

    /** Spacing between grid tiles. */
    val GridSpacing: Dp = 4.dp

    /** Aspect ratio for each grid tile (width / height). */
    const val TileAspectRatio: Float = 1f

    // --- Corner Radii ---

    /** Corner radius for CTA buttons. */
    val CtaCornerRadius: Dp = 8.dp

    /** Corner radius for each grid photo tile. */
    val TileCornerRadius: Dp = 4.dp

    /** Corner radius for the check badge. */
    val CheckBadgeCornerRadius: Dp = 4.dp

    // --- Sizes ---

    /** Size of the check badge square in each grid tile corner. */
    val CheckBadgeSize: Dp = 28.dp

    /** Checkmark icon size inside the badge. */
    val CheckIconSize: Dp = 16.dp

    // --- Typography ---

    /** App bar title font size. */
    val AppBarTitleSize: TextUnit = 18.sp

    /** Destructive prompt heading font size. */
    val PromptHeadingSize: TextUnit = 20.sp

    /** Helper link font size ("No, I want to move to trash"). */
    val HelperLinkSize: TextUnit = 14.sp

    /** Bottom helper copy font size ("You can unselect the ones you wish to keep"). */
    val BottomHelperSize: TextUnit = 13.sp

    /** CTA button label font size. */
    val CtaLabelSize: TextUnit = 16.sp

    // --- Copy ---

    /** App bar title text. */
    const val AppBarTitle: String = "审核"

    /** Tappable helper link below the destructive prompt. */
    const val HelperLinkText: String = "不，我想移到垃圾桶"

    /** Helper copy above the bottom action buttons. */
    const val BottomHelperText: String = "您可以取消选择想要保留的照片"

    /** "Decide Later" CTA label. */
    const val DecideLaterLabel: String = "稍后决定"

    /** "Delete forever" CTA label. */
    const val DeleteForeverLabel: String = "永久删除"
}
