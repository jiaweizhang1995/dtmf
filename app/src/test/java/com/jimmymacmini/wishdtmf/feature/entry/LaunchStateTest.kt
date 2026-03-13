package com.jimmymacmini.wishdtmf.feature.entry

import androidx.lifecycle.SavedStateHandle
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchStateTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `permission grant loads a ready state with capped photo count`() = runTest(dispatcher) {
        val viewModel = buildViewModel(
            photoRepository = FakePhotoRepository(
                photos = List(34) { index ->
                    LocalPhoto(id = index.toLong(), contentUri = "content://photo/$index")
                },
            ),
        )

        viewModel.onPermissionResult(granted = true)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(LaunchUiState.Ready(photoCount = 30), viewModel.uiState.value)
    }

    @Test
    fun `permission denial stays in needs permission with retry hint`() {
        val viewModel = buildViewModel()

        viewModel.onPermissionResult(granted = false)

        assertEquals(
            LaunchUiState.NeedsPermission(showSettingsHint = true),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `empty repository produces an empty launch state`() = runTest(dispatcher) {
        val viewModel = buildViewModel(
            photoRepository = FakePhotoRepository(photos = emptyList()),
        )

        viewModel.onPermissionResult(granted = true)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(LaunchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `repository failure produces an error state`() = runTest(dispatcher) {
        val viewModel = buildViewModel(
            photoRepository = FakePhotoRepository(error = IllegalStateException("boom")),
        )

        viewModel.onPermissionResult(granted = true)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LaunchUiState.Error)
        assertEquals("boom", (state as LaunchUiState.Error).message)
    }

    @Test
    fun `saved state restores the previous ready session`() {
        val savedState = SavedStateHandle(
            mapOf(
                "launch_state_kind" to "Ready",
                "launch_state_count" to 12,
            ),
        )

        val viewModel = buildViewModel(savedStateHandle = savedState)

        assertEquals(LaunchUiState.Ready(photoCount = 12), viewModel.uiState.value)
    }

    private fun buildViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        photoRepository: FakePhotoRepository = FakePhotoRepository(),
    ): LaunchViewModel {
        return LaunchViewModel(
            savedStateHandle = savedStateHandle,
            photoRepository = photoRepository,
            ioDispatcher = dispatcher,
        )
    }
}

private class FakePhotoRepository(
    private val photos: List<LocalPhoto> = emptyList(),
    private val error: Throwable? = null,
) : PhotoRepository {
    override suspend fun loadEligiblePhotos(limitHint: Int?): List<LocalPhoto> {
        error?.let { throw it }
        return photos
    }
}
