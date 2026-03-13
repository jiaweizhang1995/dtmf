package com.jimmymacmini.wishdtmf.feature.entry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jimmymacmini.wishdtmf.MainActivity
import org.junit.Rule
import org.junit.Test

class EntryFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsInPermissionEntryState() {
        composeRule.onNodeWithText("Allow gallery access").assertIsDisplayed()
    }
}
