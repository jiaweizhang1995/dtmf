package com.jimmymacmini.wishdtmf.feature.review

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Integration coverage for the Phase 4 destructive delete flow.
 *
 * These tests exercise [ReviewScreen] + [ReviewViewModel] together so they can assert
 * realistic end-to-end state transitions without launching the system dialog (which is
 * platform-owned and cannot be driven from instrumentation).
 *
 * Covered invariants:
 *  - Only selected photos enter the delete request (non-selected IDs are excluded)
 *  - An empty selection disables the delete CTA and prevents delete requests
 *  - Canceling the platform result (simulated by NOT calling onDeleteConfirmed) leaves
 *    review/session state unchanged
 *  - A successful delete result (simulated by calling onDeleteConfirmed) delivers only the
 *    confirmed IDs to the callback and advances state correctly
 */
class ReviewDeleteFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun reviewPhoto(id: Long) = ReviewPhoto(
        id = id,
        contentUri = "content://com.jimmymacmini.wishdtmf.test/photos/$id",
    )

    private fun loadedState(
        photos: List<ReviewPhoto>,
        selectedIds: Set<Long> = photos.map { it.id }.toSet(),
    ) = ReviewUiState(
        stagedPhotos = photos,
        selectedPhotoIds = selectedIds,
        isLoading = false,
    )

    private fun hasStateDescription(value: String): SemanticsMatcher {
        return SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)
    }

    // -----------------------------------------------------------------------
    // Only selected photos enter the delete request
    // -----------------------------------------------------------------------

    /**
     * Verify that when the user deselects a photo before tapping Delete forever, only the
     * remaining selected IDs would be submitted — not the full staged set.
     *
     * This is validated through ViewModel state: after deselecting photo 2, selectedPhotoIds
     * should contain only {1, 3}, confirming the delete request would be scoped correctly.
     */
    @Test
    fun deleteRequestContainsOnlySelectedIds_afterDeselectingOnePhoto() {
        val vm = ReviewViewModel(SavedStateHandle())
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L), reviewPhoto(3L))
        vm.onPhotosResolved(photos)

        // Deselect photo 2 — user wants to keep it
        vm.togglePhotoSelection(2L)

        val selectedIds = vm.uiState.value.selectedPhotoIds
        assertTrue("Photo 1 must be in delete request", selectedIds.contains(1L))
        assertFalse("Photo 2 must NOT be in delete request (deselected)", selectedIds.contains(2L))
        assertTrue("Photo 3 must be in delete request", selectedIds.contains(3L))
        assertEquals("Only 2 photos should be selected for deletion", 2, selectedIds.size)
    }

    @Test
    fun deleteRequestScope_excludesDeselectedPhotos_evenAfterMultipleToggles() {
        val vm = ReviewViewModel(SavedStateHandle())
        val photos = listOf(reviewPhoto(10L), reviewPhoto(20L), reviewPhoto(30L))
        vm.onPhotosResolved(photos)

        // Deselect then reselect 10, deselect 30
        vm.togglePhotoSelection(10L) // deselect
        vm.togglePhotoSelection(10L) // reselect
        vm.togglePhotoSelection(30L) // deselect

        val selectedIds = vm.uiState.value.selectedPhotoIds
        assertTrue("Photo 10 must be selected (reselected)", selectedIds.contains(10L))
        assertTrue("Photo 20 must be selected (never toggled)", selectedIds.contains(20L))
        assertFalse("Photo 30 must NOT be selected (deselected)", selectedIds.contains(30L))
    }

    // -----------------------------------------------------------------------
    // Empty selection prevents delete
    // -----------------------------------------------------------------------

    @Test
    fun deleteForeverCta_isDisabledWhenNothingSelected() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        composeRule.setContent {
            MaterialTheme {
                ReviewScreen(
                    stagedPhotoIds = listOf(1L, 2L),
                    uiState = loadedState(photos, selectedIds = emptySet()),
                    onBack = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ReviewScreenTags.DeleteForeverButton)
            .assert(hasStateDescription("disabled"))
    }

    @Test
    fun onDeleteForever_doesNotEmitRequestEvent_whenSelectionIsEmpty() {
        val vm = ReviewViewModel(SavedStateHandle())
        val photos = listOf(reviewPhoto(1L))
        vm.onPhotosResolved(photos)

        // Deselect all
        vm.togglePhotoSelection(1L)
        assertEquals("Selection should be empty", 0, vm.uiState.value.selectedCount)

        // onDeleteForever should guard against empty selection — no event emitted.
        // Collect events into a list to verify nothing was emitted.
        val events = mutableListOf<ReviewEvent>()
        val testScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        testScope.launch {
            vm.events.collect { events.add(it) }
        }
        vm.onDeleteForever()
        // Yield briefly on the instrumentation thread before checking
        composeRule.waitForIdle()
        assertTrue("No delete event should be emitted when selection is empty", events.isEmpty())
        testScope.cancel()
    }

    // -----------------------------------------------------------------------
    // Cancel leaves state unchanged
    // -----------------------------------------------------------------------

    /**
     * Simulates a canceled platform confirmation by NOT calling onDeleteConfirmed after the
     * delete request event is emitted. Verify review state and selected IDs remain intact.
     */
    @Test
    fun cancelDelete_leavesReviewStateUnchanged() {
        val vm = ReviewViewModel(SavedStateHandle())
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        vm.onPhotosResolved(photos)

        // User deselects photo 2 before the dialog
        vm.togglePhotoSelection(2L)
        val selectedBefore = vm.uiState.value.selectedPhotoIds.toSet()

        // Emit the delete request event (simulating tap) but do NOT call onDeleteConfirmed
        vm.onDeleteForever()

        // Review state should remain unchanged (cancel path)
        val selectedAfter = vm.uiState.value.selectedPhotoIds
        assertEquals("Selected IDs must be unchanged after cancel", selectedBefore, selectedAfter)
        assertEquals("Staged photos must be unchanged after cancel", photos, vm.uiState.value.stagedPhotos)
    }

    @Test
    fun cancelDelete_doesNotChangeSelectedCount() {
        val vm = ReviewViewModel(SavedStateHandle())
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L), reviewPhoto(3L))
        vm.onPhotosResolved(photos)

        // Deselect one photo so 2 remain selected
        vm.togglePhotoSelection(3L)
        assertEquals(2, vm.uiState.value.selectedCount)

        // Emit delete request without confirming (cancel path)
        vm.onDeleteForever()

        assertEquals("Selected count must be unchanged after cancel", 2, vm.uiState.value.selectedCount)
    }

    // -----------------------------------------------------------------------
    // Success removes only confirmed items
    // -----------------------------------------------------------------------

    /**
     * Simulate a successful platform confirmation: the route calls onDeleteConfirmed, which
     * should trigger a DeleteConfirmed event carrying only the IDs that were selected.
     */
    @Test
    fun successPath_emitsDeleteConfirmedEvent_withSelectedIdsOnly() = runBlocking {
        val vm = ReviewViewModel(SavedStateHandle())
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L), reviewPhoto(3L))
        vm.onPhotosResolved(photos)

        // Deselect photo 3 — only 1 and 2 should be confirmed for deletion
        vm.togglePhotoSelection(3L)
        val expectedIds = setOf(1L, 2L)

        // Simulate the platform returning success — the route normally calls this
        vm.onDeleteConfirmed(vm.uiState.value.selectedPhotoIds)

        // Collect the emitted event with a timeout
        val event = withTimeout(500L) { vm.events.first() }

        assertTrue("Event must be DeleteConfirmed", event is ReviewEvent.DeleteConfirmed)
        assertEquals(
            "Confirmed IDs must match selected subset",
            expectedIds,
            (event as ReviewEvent.DeleteConfirmed).deletedPhotoIds,
        )
    }

    // -----------------------------------------------------------------------
    // Screen: delete button tap fires callback
    // -----------------------------------------------------------------------

    @Test
    fun tappingDeleteForeverButton_firesCallback_whenEnabled() {
        val photos = listOf(reviewPhoto(1L), reviewPhoto(2L))
        var deleteClicked = false
        composeRule.setContent {
            MaterialTheme {
                ReviewScreen(
                    stagedPhotoIds = listOf(1L, 2L),
                    uiState = loadedState(photos),
                    onBack = {},
                    onDeleteForever = { deleteClicked = true },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ReviewScreenTags.DeleteForeverButton).assertIsDisplayed()
        composeRule.onNodeWithTag(ReviewScreenTags.DeleteForeverButton).performClick()
        composeRule.waitForIdle()
        assertTrue("onDeleteForever callback must be invoked when CTA is tapped", deleteClicked)
    }

    @Test
    fun tappingDeleteForeverButton_doesNotFireCallback_whenDisabled() {
        val photos = listOf(reviewPhoto(1L))
        var deleteClicked = false
        composeRule.setContent {
            MaterialTheme {
                ReviewScreen(
                    stagedPhotoIds = listOf(1L),
                    uiState = loadedState(photos, selectedIds = emptySet()),
                    onBack = {},
                    onDeleteForever = { deleteClicked = true },
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ReviewScreenTags.DeleteForeverButton).performClick()
        composeRule.waitForIdle()
        assertFalse("onDeleteForever must NOT be invoked when CTA is disabled", deleteClicked)
    }
}
