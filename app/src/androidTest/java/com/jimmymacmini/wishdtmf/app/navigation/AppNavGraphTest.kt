package com.jimmymacmini.wishdtmf.app.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.feature.entry.LaunchUiState
import com.jimmymacmini.wishdtmf.feature.main.MainScreenTags
import com.jimmymacmini.wishdtmf.feature.review.ReviewScreenTags
import org.junit.Rule
import org.junit.Test

class AppNavGraphTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun proceedRemainsBlockedUntilPhotosAreStaged() {
        composeRule.setContent {
            MaterialTheme {
                AppNavGraph(
                    uiState = LaunchUiState.Ready(sampleSession()),
                    onGrantAccess = {},
                    onRetry = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MainScreenTags.Root).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsNotEnabled()
        composeRule.onAllNodesWithTag(ReviewScreenTags.Root).assertCountEquals(0)
    }

    @Test
    fun proceedNavigatesToReviewAndBackReturnsToSameSwipeState() {
        composeRule.setContent {
            MaterialTheme {
                AppNavGraph(
                    uiState = LaunchUiState.Ready(sampleSession()),
                    onGrantAccess = {},
                    onRetry = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag(MainScreenTags.HeroPhoto).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MainScreenTags.Root)
            .assert(hasStateDescription("current:20;staged:10;complete:false"))
        composeRule.onNodeWithTag(MainScreenTags.UndoAction).assertIsEnabled()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsEnabled()

        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ReviewScreenTags.Root).assertIsDisplayed()
        composeRule.onNodeWithTag(ReviewScreenTags.Root)
            .assert(hasStateDescription("staged:10"))

        composeRule.onNodeWithTag(ReviewScreenTags.BackButton).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MainScreenTags.Root).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.Root)
            .assert(hasStateDescription("current:20;staged:10;complete:false"))
        composeRule.onNodeWithTag(MainScreenTags.UndoAction).assertIsEnabled()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsEnabled()
    }

    private fun sampleSession(): LaunchSession = LaunchSession(
        photos = listOf(
            samplePhoto(id = 10, suffix = "1"),
            samplePhoto(id = 20, suffix = "2"),
            samplePhoto(id = 30, suffix = "3"),
        ),
    )

    private fun samplePhoto(id: Long, suffix: String) = LocalPhoto(
        id = id,
        contentUri = "content://com.jimmymacmini.wishdtmf.test/photos/$suffix",
        mimeType = "image/jpeg",
        dateTakenMillis = 1_712_556_800_000,
        sizeBytes = 4_500_000,
    )

    private fun hasStateDescription(value: String): SemanticsMatcher {
        return SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
    }
}
