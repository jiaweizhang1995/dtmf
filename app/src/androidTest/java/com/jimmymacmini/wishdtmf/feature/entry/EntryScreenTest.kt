package com.jimmymacmini.wishdtmf.feature.entry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import org.junit.Rule
import org.junit.Test

class EntryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun deniedPermissionStateShowsRetryCopy() {
        composeRule.setContent {
            EntryScreen(
                uiState = LaunchUiState.NeedsPermission(showSettingsHint = true),
                onGrantAccess = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("Gallery access is still required to build a fresh batch.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Try permission again").assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsRetryAction() {
        composeRule.setContent {
            EntryScreen(
                uiState = LaunchUiState.Empty,
                onGrantAccess = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("No photos to clean up").assertIsDisplayed()
        composeRule.onNodeWithText("Scan again").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsRetryAction() {
        composeRule.setContent {
            EntryScreen(
                uiState = LaunchUiState.Error(message = "Could not prepare a photo batch."),
                onGrantAccess = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("Could not prepare a photo batch.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun readyStateShowsCurrentBatchProgress() {
        composeRule.setContent {
            EntryScreen(
                uiState = LaunchUiState.Ready(
                    session = LaunchSession(
                        photos = listOf(
                            LocalPhoto(id = 1L, contentUri = "content://photo/1"),
                            LocalPhoto(id = 2L, contentUri = "content://photo/2"),
                            LocalPhoto(id = 3L, contentUri = "content://photo/3"),
                        ),
                        currentIndex = 1,
                    ),
                ),
                onGrantAccess = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("Session ready with 3 photos.").assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Settings-hint button (Wave 0)
    // -----------------------------------------------------------------------

    @Test
    fun needsPermission_showsSettingsButton_whenShowSettingsHintTrue() {
        composeRule.setContent {
            EntryScreen(
                uiState = LaunchUiState.NeedsPermission(showSettingsHint = true),
                onGrantAccess = {},
                onRetry = {},
            )
        }

        composeRule.onNodeWithText("Open app settings").assertIsDisplayed()
    }
}
