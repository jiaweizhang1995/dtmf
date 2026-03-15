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
 * Instrumentation coverage for the Phase 4 review screen hierarchy.
 *
 * These tests pin the top-level layout contract — title, destructive prompt, staged-photo
 * grid, bottom actions — before later plans add selection toggling and delete mutations.
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

    private fun setReviewScreen(
        stagedPhotoIds: List<Long> = emptyList(),
        stagedPhotos: List<ReviewPhoto> = emptyList(),
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            MaterialTheme {
                ReviewScreen(
                    stagedPhotoIds = stagedPhotoIds,
                    stagedPhotos = stagedPhotos,
                    onBack = onBack,
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
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            stagedPhotos = listOf(reviewPhoto(1L), reviewPhoto(2L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.Root).assertIsDisplayed()
    }

    @Test
    fun titleIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            stagedPhotos = listOf(reviewPhoto(1L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.Title).assertIsDisplayed()
    }

    @Test
    fun backButtonIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            stagedPhotos = listOf(reviewPhoto(1L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.BackButton).assertIsDisplayed()
    }

    @Test
    fun destructivePromptIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            stagedPhotos = listOf(reviewPhoto(1L), reviewPhoto(2L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.DestructivePrompt).assertIsDisplayed()
    }

    @Test
    fun helperLinkIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            stagedPhotos = listOf(reviewPhoto(1L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.HelperLink).assertIsDisplayed()
    }

    @Test
    fun photoGridIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L, 2L),
            stagedPhotos = listOf(reviewPhoto(1L), reviewPhoto(2L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.PhotoGrid).assertIsDisplayed()
    }

    @Test
    fun bottomHelperIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            stagedPhotos = listOf(reviewPhoto(1L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.BottomHelper).assertIsDisplayed()
    }

    @Test
    fun decideLaterButtonIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            stagedPhotos = listOf(reviewPhoto(1L)),
        )
        composeRule.onNodeWithTag(ReviewScreenTags.DecideLaterButton).assertIsDisplayed()
    }

    @Test
    fun deleteForeverButtonIsDisplayed() {
        setReviewScreen(
            stagedPhotoIds = listOf(1L),
            stagedPhotos = listOf(reviewPhoto(1L)),
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
            stagedPhotos = ids.map { reviewPhoto(it) },
        )
        ids.forEach { id ->
            composeRule
                .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id")
                .assertIsDisplayed()
        }
    }

    @Test
    fun eachStagedPhotoHasACheckBadge() {
        val ids = listOf(10L, 20L)
        setReviewScreen(
            stagedPhotoIds = ids,
            stagedPhotos = ids.map { reviewPhoto(it) },
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
            stagedPhotos = listOf(reviewPhoto(99L)),
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
            stagedPhotos = listOf(reviewPhoto(10L)),
        )
        composeRule
            .onNodeWithTag(ReviewScreenTags.Root)
            .assert(hasStateDescription("staged:10"))
    }

    @Test
    fun rootHasStagedIdsInStateDescription_multipleIds() {
        setReviewScreen(
            stagedPhotoIds = listOf(10L, 20L),
            stagedPhotos = listOf(reviewPhoto(10L), reviewPhoto(20L)),
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
            stagedPhotos = listOf(reviewPhoto(1L)),
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
            stagedPhotos = emptyList(),
        )
        // Placeholder tiles should be present for each ID
        ids.forEach { id ->
            composeRule
                .onNodeWithTag("${ReviewScreenTags.PhotoTilePrefix}$id")
                .assertIsDisplayed()
        }
    }
}
