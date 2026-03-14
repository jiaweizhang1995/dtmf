package com.jimmymacmini.wishdtmf.feature.main

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.SwipeDecisionReducer
import com.jimmymacmini.wishdtmf.domain.SwipeSessionState
import org.junit.Rule
import org.junit.Test

class SwipeGestureTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun swipeLeft_stagesCurrentPhotoAndAdvances() {
        composeRule.setContent {
            var swipeState by mutableStateOf(SwipeSessionState())
            val session = sampleSession()
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(session, swipeState),
                    onStageCurrentPhoto = {
                        swipeState = SwipeDecisionReducer.stageCurrentPhoto(session, swipeState)
                    },
                    onSkipCurrentPhoto = {
                        swipeState = SwipeDecisionReducer.skipCurrentPhoto(session, swipeState)
                    },
                    onAdvance = {},
                )
            }
        }

        composeRule.onNodeWithTag(MainScreenTags.HeroPhoto)
            .performTouchInput { swipeLeft() }

        composeRule.waitForIdle()

        composeRule.onNode(
            hasTestTag(MainScreenTags.HeroPhoto) and hasContentDescription("Photo 2"),
            useUnmergedTree = true,
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(thumbnailTag(20)).assertIsDisplayed()
        composeRule.onNodeWithText("Proceed").assertIsDisplayed()
        composeRule.onAllNodesWithText("PREMIUM").assertCountEquals(0)
        composeRule.onNodeWithTag(MainScreenTags.Root)
            .assert(hasStateDescription("current:20;staged:10;complete:false"))
    }

    @Test
    fun swipeRight_keepsStagedIdsUntouchedAndAdvances() {
        composeRule.setContent {
            var swipeState by mutableStateOf(SwipeSessionState())
            val session = sampleSession()
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(session, swipeState),
                    onStageCurrentPhoto = {
                        swipeState = SwipeDecisionReducer.stageCurrentPhoto(session, swipeState)
                    },
                    onSkipCurrentPhoto = {
                        swipeState = SwipeDecisionReducer.skipCurrentPhoto(session, swipeState)
                    },
                    onAdvance = {},
                )
            }
        }

        composeRule.onNodeWithTag(MainScreenTags.HeroPhoto)
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(MainScreenTags.HeroPhoto)
            .performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNode(
            hasTestTag(MainScreenTags.HeroPhoto) and hasContentDescription("Photo 3"),
            useUnmergedTree = true,
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(thumbnailTag(30)).assertIsDisplayed()
        composeRule.onNodeWithTag(MainScreenTags.Root)
            .assert(hasStateDescription("current:30;staged:10;complete:false"))
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
