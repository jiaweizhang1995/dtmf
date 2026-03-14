package com.jimmymacmini.wishdtmf.feature.main

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
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
                    onAdvance = {},
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
        composeRule.onNodeWithText("Undo").assertIsDisplayed()
        composeRule.onNodeWithText("Skip").assertIsDisplayed()
        composeRule.onNodeWithText("PROCEED  →").assertIsDisplayed()
        composeRule.onNodeWithText("Photo 3").assertIsDisplayed()
        composeRule.onNodeWithText("Organise into albums").assertIsDisplayed()
    }

    @Test
    fun readyStateUsesSessionBackedPhotoDescriptionsAndExpectedSectionOrder() {
        composeRule.setContent {
            MaterialTheme {
                MainScreen(
                    uiState = MainUiState.fromSession(sampleSession()),
                    onAdvance = {},
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
