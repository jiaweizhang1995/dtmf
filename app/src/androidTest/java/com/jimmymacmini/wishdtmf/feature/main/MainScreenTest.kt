package com.jimmymacmini.wishdtmf.feature.main

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.SwipeDecision
import com.jimmymacmini.wishdtmf.domain.SwipeDirection
import com.jimmymacmini.wishdtmf.domain.SwipeSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun readyStateShowsMainScreenSectionsAndAffordances() {
        composeRule.setContent {
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(sampleSession()),
                    onStageCurrentPhoto = {},
                    onSkipCurrentPhoto = {},
                    onUndoLastDecision = {},
                    onProceed = {},
                )
            }
        }

        composeRule.onNodeWithTag(MainScreenTags.TopBar).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.ThumbnailRail).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.MetadataRow).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.HeroPhoto).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.BottomActions).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.BannerRow).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.UndoAction).assertIsNotEnabled()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsNotEnabled()
        composeRule.onNodeWithText("Undo").assertIsDisplayed()
        composeRule.onNodeWithText("Skip").assertIsDisplayed()
        composeRule.onNodeWithText("Proceed").assertIsDisplayed()
        composeRule.onNodeWithText("Photo 3").assertIsDisplayed()
        composeRule.onNodeWithText("Organise into albums").assertIsDisplayed()
        composeRule.onAllNodesWithText("PREMIUM").assertCountEquals(0)
    }

    @Test
    fun readyStateUsesSessionBackedPhotoDescriptionsAndExpectedSectionOrder() {
        composeRule.setContent {
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(sampleSession()),
                    onStageCurrentPhoto = {},
                    onSkipCurrentPhoto = {},
                    onUndoLastDecision = {},
                    onProceed = {},
                )
            }
        }

        composeRule.onNode(
            hasTestTag(MainScreenTags.HeroPhoto) and hasContentDescription("Photo 3"),
            useUnmergedTree = true,
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(thumbnailTag(1)).assertIsDisplayed()
        composeRule.onNodeWithTag(thumbnailTag(3)).assertIsDisplayed()
        composeRule.onNodeWithTag(thumbnailTag(5)).assertIsDisplayed()

        val thumbnailBounds = composeRule.onNodeWithTag(MainScreenTags.ThumbnailRail).fetchSemanticsNode().boundsInRoot
        val metadataBounds = composeRule.onNodeWithTag(MainScreenTags.MetadataRow).fetchSemanticsNode().boundsInRoot
        val heroBounds = composeRule.onNodeWithTag(MainScreenTags.HeroPhoto).fetchSemanticsNode().boundsInRoot

        assertTrue(thumbnailBounds.top < metadataBounds.top)
        assertTrue(metadataBounds.top < heroBounds.top)
    }

    @Test
    fun readyState_keepsHeroAndCurrentThumbnailAligned() {
        composeRule.setContent {
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(sampleSession()),
                    onStageCurrentPhoto = {},
                    onSkipCurrentPhoto = {},
                    onUndoLastDecision = {},
                    onProceed = {},
                )
            }
        }

        composeRule.onNode(
            hasTestTag(MainScreenTags.HeroPhoto) and hasContentDescription("Photo 3"),
            useUnmergedTree = true,
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(thumbnailTag(3)).assertIsDisplayed()
    }

    @Test
    fun completedState_showsCompletedMessagingAndLeavesUndoAvailable() {
        composeRule.setContent {
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(
                        session = sampleSession(),
                        swipeState = SwipeSessionState(
                            currentIndex = 4,
                            stagedPhotoIds = setOf(3L, 4L),
                            lastDecision = SwipeDecision(
                                photoId = 5L,
                                direction = SwipeDirection.Right,
                                previousIndex = 4,
                            ),
                            isSessionComplete = true,
                        ),
                    ),
                    onStageCurrentPhoto = {},
                    onSkipCurrentPhoto = {},
                    onUndoLastDecision = {},
                    onProceed = {},
                )
            }
        }

        composeRule.onNodeWithTag(MainScreenTags.SessionCompleteMessage).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.UndoAction).assertIsEnabled()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsEnabled()
    }

    @Test
    fun proceedAction_onlyInvokesCallbackWhenEnabled() {
        var proceedClicks = 0

        composeRule.setContent {
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(
                        session = sampleSession(),
                        swipeState = SwipeSessionState(
                            currentIndex = 2,
                            stagedPhotoIds = setOf(3L),
                            lastDecision = SwipeDecision(
                                photoId = 3L,
                                direction = SwipeDirection.Left,
                                previousIndex = 2,
                            ),
                        ),
                    ),
                    onStageCurrentPhoto = {},
                    onSkipCurrentPhoto = {},
                    onUndoLastDecision = {},
                    onProceed = { proceedClicks += 1 },
                )
            }
        }

        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).assertIsEnabled()
        composeRule.onNodeWithTag(MainScreenTags.ProceedAffordance).performClick()
        composeRule.runOnIdle {
            assertEquals(1, proceedClicks)
        }
    }

    private fun sampleSession(): LaunchSession = LaunchSession(
        photos = listOf(
            samplePhoto(id = 1, suffix = "1"),
            samplePhoto(id = 2, suffix = "2"),
            samplePhoto(id = 3, suffix = "3"),
            samplePhoto(id = 4, suffix = "4"),
            samplePhoto(id = 5, suffix = "5"),
        ),
        currentIndex = 2,
    )

    private fun samplePhoto(id: Long, suffix: String) = LocalPhoto(
        id = id,
        contentUri = "content://com.jimmymacmini.wishdtmf.test/photos/$suffix",
        mimeType = "image/jpeg",
        dateTakenMillis = 1_712_556_800_000,
        sizeBytes = 4_500_000,
    )
}
