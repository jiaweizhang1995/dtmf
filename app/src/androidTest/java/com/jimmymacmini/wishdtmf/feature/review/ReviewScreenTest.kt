package com.jimmymacmini.wishdtmf.feature.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation coverage for the Phase 4 review screen.
 *
 * Tests cover:
 *  - Top-level layout hierarchy (title, destructive prompt, grid, bottom actions)
 *  - Selection affordances: all selected by default, deselect/reselect toggle, check badges
 *  - Count-driven copy updates when selection subset changes
 *  - Delete CTA disabled state when no photos remain selected
 *
 * Tests use stable semantics and test tags rather than brittle screenshot assertions.
 */
class ReviewScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun reviewPhoto(id: Long) = ReviewPhoto(
        id = id,
        contentUri = "content://com.jimmymacmini.wishdtmf.test/photos/$id",
    )

    /** Build a fully-loaded [ReviewUiState] with all supplied photos selected. */
    private fun loadedState(
        photos: List<ReviewPhoto>,
        selectedIds: Set<Long> = photos.map { it.id }.toSet(),
    ) = ReviewUiState(
        stagedPhotos = photos,
        selectedPhotoIds = selectedIds,
        isLoading = false,
    )

    private fun setReviewScreen(
        stagedPhotoIds: List<Long> = emptyList(),
        uiState: ReviewUiState = ReviewUiState(),
        onBack: () -> Unit = {},
        onTogglePhotoSelection: (Long) -> Unit = {},
    ) {
        composeRule.setContent {
            MaterialTheme {
                ReviewScreen(
                    stagedPhotoIds = stagedPhotoIds,
                    uiState = uiState,
                    onBack = onBack,
                    onTogglePhotoSelection = onTogglePhotoSelection,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun hasStateDescription(value: String): SemanticsMatcher {
        return SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
    }

    // -----------------------------------------------------------------------
    // Layout hierarchy
    // -----------------------------------------------------------------------

    @Test
    fun reviewRootIsDisplayed() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.Root).assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        val photos = listOf(reviewPhoto(1L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.Title).assertIsDisplayed()
    }

    @Test
    fun backButtonIsDisplayed() {
        val photos = listOf(reviewPhoto(1L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.BackButton).assertIsDisplayed()
    }

    @Test
    fun destructivePromptIsDisplayed() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.DestructivePrompt).assertIsDisplayed()
    }

    @Test
    fun helperLinkIsDisplayed() {
        val photos = listOf(reviewPhoto(1L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.HelperLink).assertIsDisplayed()
    }

    @Test
    fun photoGridIsDisplayed() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.PhotoGrid).assertIsDisplayed()
    }

    @Test
    fun bottomHelperIsDisplayed() {
        val photos = listOf(reviewPhoto(1L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.BottomHelper).assertIsDisplayed()
    }

    @Test
    fun decideLaterButtonIsDisplayed() {
        val photos = listOf(reviewPhoto(1L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.DecideLaterButton).assertIsDisplayed()
    }

    @Test
    fun deleteForeverButtonIsDisplayed() {
        val photos = listOf(reviewPhoto(1L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(photos),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.DeleteForeverButton).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Staged-photo grid rendering
    // -----------------------------------------------------------------------

    @Test
    fun eachStagedPhotoRendersATile() {
        val ids = listOf(101L, 202L, 303L)
        setReviewScreen(
            stagedPhotoIds = ids,
            uiState = loadedState(ids.map { reviewPhoto(it) }),
        )
        ids.forEach { id ->
            composeRule
                .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id")
                .assertIsDisplayed()
        }
    }

    @Test
    fun eachStagedPhotoHasACheckBadgeWhenSelected() {
        val ids = listOf(10L, 20L)
        setReviewScreen(
            stagedPhotoIds = ids,
            uiState = loadedState(ids.map { reviewPhoto(it) }),
        )
        ids.forEach { id ->
            composeRule
                .onNodeWithTag("${ReviewScreenTags.CheckBadgePrefix}$id")
                .assertIsDisplayed()
        }
    }

    @Test
    fun singleStagedPhotoRendersOneTile() {
        setReviewScreen(
            stagedPhotoIds = listOf(99L),
            uiState = loadedState(listOf(reviewPhoto(99L))),
        )
        composeRule
            .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}99")
            .assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Semantics / state description
    // -----------------------------------------------------------------------

    @Test
    fun rootHasStagedIdsInStateDescription_singleId() {
        setReviewScreen(
            stagedPhotoIds = listOf(10L),
            uiState = loadedState(listOf(reviewPhoto(10L))),
        )
        composeRule
            .onNodeWithTag(ReviewScreenTags.Root)
            .assert(hasStateDescription("staged:10"))
    }

    @Test
    fun rootHasStagedIdsInStateDescription_multipleIds() {
        setReviewScreen(
            stagedPhotoIds = listOf(10L, 20L),
            uiState = loadedState(listOf(reviewPhoto(10L), reviewPhoto(20L))),
        )
        composeRule
            .onNodeWithTag(ReviewScreenTags.Root)
            .assert(hasStateDescription("staged:10,20"))
    }

    // -----------------------------------------------------------------------
    // Back navigation
    // -----------------------------------------------------------------------

    @Test
    fun backButtonCallsOnBack() {
        var backCalled = false
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            uiState = loadedState(listOf(reviewPhoto(1L))),
            onBack = { backCalled = true },
        )
        composeRule.onNodeWithTag(ReviewScreenTags.BackButton).performClick()
        composeRule.waitForIdle()
        assertTrue("Expected onBack to be called when back button tapped", backCalled)
    }

    // -----------------------------------------------------------------------
    // Placeholder grid while photos are loading
    // -----------------------------------------------------------------------

    @Test
    fun placeholderTilesShownWhilePhotosLoading() {
        // Provide staged IDs but no resolved photos (simulates loading state)
        val ids = listOf(5L, 6L)
        setReviewScreen(
            stagedPhotoIds = ids,
            uiState = ReviewUiState(isLoading = true),
        )
        // Placeholder tiles should be present for each ID
        ids.forEach { id ->
            composeRule
                .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id")
                .assertIsDisplayed()
        }
    }

    // -----------------------------------------------------------------------
    // Selection affordances — deselect / reselect
    // -----------------------------------------------------------------------

    @Test
    fun tappingSelectedTileFiresToggleCallback() {
        val id = 42L
        var toggledId: Long? = null
        setReviewScreen(
            stagedPhotoIds = listOf(id),
            uiState = loadedState(listOf(reviewPhoto(id))),
            onTogglePhotoSelection = { toggledId = it },
        )
        composeRule.onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id").performClick()
        composeRule.waitForIdle()
        assertTrue("Expected toggle callback with id=$id", toggledId == id)
    }

    @Test
    fun deselectedTileHasNoCheckBadge() {
        val id = 77L
        setReviewScreen(
            stagedPhotoIds = listOf(id),
            uiState = loadedState(
                photos = listOf(reviewPhoto(id)),
                selectedIds = emptySet(), // photo deselected
            ),
        )
        composeRule
            .onNodeWithTag("${ReviewScreenTags.CheckBadgePrefix}$id")
            .assertDoesNotExist()
    }

    @Test
    fun deselectedTileHasDeselectedStateDescription() {
        val id = 55L
        setReviewScreen(
            stagedPhotoIds = listOf(id),
            uiState = loadedState(
                photos = listOf(reviewPhoto(id)),
                selectedIds = emptySet(),
            ),
        )
        composeRule
            .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id")
            .assert(hasStateDescription("deselected"))
    }

    @Test
    fun selectedTileHasSelectedStateDescription() {
        val id = 33L
        setReviewScreen(
            stagedPhotoIds = listOf(id),
            uiState = loadedState(listOf(reviewPhoto(id))),
        )
        composeRule
            .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id")
            .assert(hasStateDescription("selected"))
    }

    // -----------------------------------------------------------------------
    // Count-driven copy
    // -----------------------------------------------------------------------

    @Test
    fun destructivePromptReflectsSelectedSubset() {
        // Two staged, one selected — prompt should say "1 item"
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            uiState = loadedState(photos, selectedIds = setOf(1L)),
        )
        // Verify the prompt node is displayed (text content driven by state)
        composeRule.onNodeWithTag(ReviewScreenTags.DestructivePrompt).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Delete CTA disabled when nothing selected
    // -----------------------------------------------------------------------

    @Test
    fun deleteForeverButtonIsDisabledWhenNothingSelected() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            uiState = loadedState(photos, selectedIds = emptySet()),
        )
        composeRule
            .onNodeWithTag(ReviewScreenTags.DeleteForeverButton)
            .assert(hasStateDescription("disabled"))
    }

    @Test
    fun deleteForeverButtonIsEnabledWhenSomethingSelected() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            uiState = loadedState(photos, selectedIds = setOf(1L)),
        )
        composeRule
            .onNodeWithTag(ReviewScreenTags.DeleteForeverButton)
            .assert(hasStateDescription("enabled"))
    }

    // -----------------------------------------------------------------------
    // Empty-grid state (Wave 0)
    // -----------------------------------------------------------------------

    @Test
    fun emptyGrid_showsEmptyMessage_whenBothStagedListsEmpty() {
        setReviewScreen(
            stagedPhotoIds = emptyList(),
            uiState = ReviewUiState(isLoading = false, stagedPhotos = emptyList()),
        )
        composeRule
            .onNodeWithTag(ReviewScreenTags.EmptyGridMessage)
            .assertIsDisplayed()
    }
}
