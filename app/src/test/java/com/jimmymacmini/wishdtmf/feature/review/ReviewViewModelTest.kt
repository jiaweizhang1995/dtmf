package com.jimmymacmini.wishdtmf.feature.review

import androidx.lifecycle.SavedStateHandle
import com.jimmymacmini.wishdtmf.data.media.ReviewPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit coverage for [ReviewViewModel] selection semantics.
 *
 * Verifies:
 *  - Default all-selected initialisation when photos are resolved
 *  - Single-item deselection reduces selected count
 *  - Reselection restores item to selected set
 *  - Selected-count derivation in [ReviewUiState]
 *  - [SavedStateHandle]-backed state restoration after ViewModel recreation
 *  - Delete CTA eligibility: enabled with selections, disabled with none
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private fun photo(id: Long) = ReviewPhoto(
        id = id,
        contentUri = "content://test/photos/$id",
    )

    private fun photos(vararg ids: Long): List<ReviewPhoto> = ids.map { photo(it) }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): ReviewViewModel =
        ReviewViewModel(savedStateHandle)

    // -----------------------------------------------------------------------
    // Default initialisation — all staged photos start selected
    // -----------------------------------------------------------------------

    @Test
    fun onPhotosResolved_allPhotosStartSelected() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L, 3L))

        val state = vm.uiState.value
        assertEquals(setOf(1L, 2L, 3L), state.selectedPhotoIds)
        assertEquals(3, state.selectedCount)
        assertTrue(state.isDeleteEnabled)
    }

    @Test
    fun onPhotosResolved_isLoadingIsFalseAfterResolution() {
        val vm = viewModel()
        assertTrue("Expected loading initially", vm.uiState.value.isLoading)

        vm.onPhotosResolved(photos(1L))
        assertFalse("Expected not loading after resolution", vm.uiState.value.isLoading)
    }

    @Test
    fun onPhotosResolved_stagedPhotosMatchResolvedList() {
        val resolved = photos(10L, 20L, 30L)
        val vm = viewModel()
        vm.onPhotosResolved(resolved)

        assertEquals(resolved, vm.uiState.value.stagedPhotos)
    }

    // -----------------------------------------------------------------------
    // Deselection — single item
    // -----------------------------------------------------------------------

    @Test
    fun togglePhotoSelection_deselects_selectedItem() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L, 3L))

        vm.togglePhotoSelection(2L)

        val state = vm.uiState.value
        assertFalse("Photo 2 should be deselected", state.isSelected(2L))
        assertTrue("Photo 1 should remain selected", state.isSelected(1L))
        assertTrue("Photo 3 should remain selected", state.isSelected(3L))
        assertEquals(2, state.selectedCount)
    }

    @Test
    fun togglePhotoSelection_deselectAll_disablesDeleteCta() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L))

        vm.togglePhotoSelection(1L)

        val state = vm.uiState.value
        assertFalse("Delete should be disabled when nothing selected", state.isDeleteEnabled)
        assertEquals(0, state.selectedCount)
    }

    // -----------------------------------------------------------------------
    // Reselection
    // -----------------------------------------------------------------------

    @Test
    fun togglePhotoSelection_reselects_deselectedItem() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L))

        // Deselect then reselect photo 1
        vm.togglePhotoSelection(1L)
        assertFalse("Photo 1 should be deselected after first toggle", vm.uiState.value.isSelected(1L))

        vm.togglePhotoSelection(1L)
        assertTrue("Photo 1 should be reselected after second toggle", vm.uiState.value.isSelected(1L))
        assertEquals(2, vm.uiState.value.selectedCount)
    }

    // -----------------------------------------------------------------------
    // Count derivation
    // -----------------------------------------------------------------------

    @Test
    fun selectedCount_matchesSelectedSubsetSize() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L, 3L, 4L))

        vm.togglePhotoSelection(1L)
        vm.togglePhotoSelection(3L)

        assertEquals(2, vm.uiState.value.selectedCount)
    }

    @Test
    fun destructivePromptText_pluralForMultipleSelected() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L, 3L))

        assertEquals("Permanently delete 3 items?", vm.uiState.value.destructivePromptText)
    }

    @Test
    fun destructivePromptText_singularForOneSelected() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L))

        vm.togglePhotoSelection(2L)

        assertEquals("Permanently delete 1 item?", vm.uiState.value.destructivePromptText)
    }

    // -----------------------------------------------------------------------
    // SavedStateHandle — state restoration after ViewModel recreation
    // -----------------------------------------------------------------------

    @Test
    fun savedStateHandle_restoredSelectedIds_survivesRecreation() {
        val savedStateHandle = SavedStateHandle()
        val vm1 = viewModel(savedStateHandle)
        vm1.onPhotosResolved(photos(1L, 2L, 3L))

        // User deselects photo 2 before rotation
        vm1.togglePhotoSelection(2L)
        assertEquals(setOf(1L, 3L), vm1.uiState.value.selectedPhotoIds)

        // Simulate ViewModel recreation with the same SavedStateHandle
        val vm2 = viewModel(savedStateHandle)
        vm2.onPhotosResolved(photos(1L, 2L, 3L))

        // After recreation, the prior selection (1 + 3, not 2) should be restored
        val restored = vm2.uiState.value.selectedPhotoIds
        assertEquals(setOf(1L, 3L), restored)
        assertFalse("Photo 2 should remain deselected after restoration", vm2.uiState.value.isSelected(2L))
    }

    @Test
    fun savedStateHandle_dropsStaleIds_whenResolvedPhotoSetChanges() {
        val savedStateHandle = SavedStateHandle()
        val vm1 = viewModel(savedStateHandle)
        vm1.onPhotosResolved(photos(1L, 2L, 3L))
        // All three selected at this point — persisted to handle

        // Simulate recreation with a reduced resolved set (photo 3 no longer in MediaStore)
        val vm2 = viewModel(savedStateHandle)
        vm2.onPhotosResolved(photos(1L, 2L))

        // Stale id=3 should be dropped; 1 and 2 should remain selected
        val restored = vm2.uiState.value.selectedPhotoIds
        assertTrue("Photo 1 should be selected", restored.contains(1L))
        assertTrue("Photo 2 should be selected", restored.contains(2L))
        assertFalse("Stale photo 3 should be dropped", restored.contains(3L))
    }

    @Test
    fun noSavedState_defaultsToAllPhotosSelected_onFreshInit() {
        // No prior persisted data — verify fresh state defaults all to selected
        val vm = viewModel(SavedStateHandle())
        vm.onPhotosResolved(photos(10L, 20L))

        assertEquals(setOf(10L, 20L), vm.uiState.value.selectedPhotoIds)
    }

    // -----------------------------------------------------------------------
    // Delete CTA eligibility
    // -----------------------------------------------------------------------

    @Test
    fun isDeleteEnabled_trueWhenAtLeastOneSelected() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L))

        vm.togglePhotoSelection(1L) // deselect one, one remains
        assertTrue(vm.uiState.value.isDeleteEnabled)
    }

    @Test
    fun isDeleteEnabled_falseWhenAllDeselected() {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L))

        vm.togglePhotoSelection(1L)
        assertFalse(vm.uiState.value.isDeleteEnabled)
    }

    // -----------------------------------------------------------------------
    // onDeleteConfirmed — partial delete propagation (Wave 0)
    // -----------------------------------------------------------------------

    @Test
    fun onDeleteConfirmed_partialDelete_onlyPassesActuallyDeletedIds() = runTest {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L, 3L))

        val emittedEvents = mutableListOf<ReviewEvent>()
        val collectJob = launch(testDispatcher) {
            vm.events.collect { emittedEvents.add(it) }
        }

        // Simulate: 3 photos were selected but only 1 and 3 were actually deleted by MediaStore.
        vm.onDeleteConfirmed(setOf(1L, 3L))

        // Allow the SharedFlow emission to be collected.
        testScheduler.advanceUntilIdle()

        val deleteConfirmed = emittedEvents.filterIsInstance<ReviewEvent.DeleteConfirmed>()
        assertEquals("Expected exactly one DeleteConfirmed event", 1, deleteConfirmed.size)
        assertEquals(setOf(1L, 3L), deleteConfirmed.first().deletedPhotoIds)
        assertFalse("Deleted IDs must not contain 2L", deleteConfirmed.first().deletedPhotoIds.contains(2L))

        collectJob.cancel()
    }

    @Test
    fun onDeleteConfirmed_emptyDeletedSet_emitsDeleteConfirmedWithEmptySet() = runTest {
        val vm = viewModel()
        vm.onPhotosResolved(photos(1L, 2L))

        val emittedEvents = mutableListOf<ReviewEvent>()
        val collectJob = launch(testDispatcher) {
            vm.events.collect { emittedEvents.add(it) }
        }

        // Guard against empty propagation lives in ReviewRoute, not ViewModel.
        // ViewModel should still emit DeleteConfirmed(emptySet()) when asked.
        vm.onDeleteConfirmed(emptySet())

        testScheduler.advanceUntilIdle()

        val deleteConfirmed = emittedEvents.filterIsInstance<ReviewEvent.DeleteConfirmed>()
        assertEquals("Expected exactly one DeleteConfirmed event", 1, deleteConfirmed.size)
        assertEquals(emptySet<Long>(), deleteConfirmed.first().deletedPhotoIds)

        collectJob.cancel()
    }
}
