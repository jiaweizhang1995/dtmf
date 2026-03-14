package com.jimmymacmini.wishdtmf.feature.entry

import androidx.lifecycle.SavedStateHandle
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository
import com.jimmymacmini.wishdtmf.domain.LaunchPhotoShuffler
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.LaunchSessionBuilder
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
        val photos = List(34) { index ->
            LocalPhoto(id = index.toLong(), contentUri = "content://photo/$index")
        }
        val viewModel = buildViewModel(
            photoRepository = FakePhotoRepository(
                photos = photos,
            ),
        )

        viewModel.onPermissionResult(granted = true)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            LaunchUiState.Ready(
                session = LaunchSession(
                    photos = photos.take(30),
                    currentIndex = 0,
                ),
            ),
            viewModel.uiState.value,
        )
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
        val restoredPhotos = listOf(
            LocalPhoto(id = 4L, contentUri = "content://photo/4"),
            LocalPhoto(id = 9L, contentUri = "content://photo/9"),
            LocalPhoto(id = 12L, contentUri = "content://photo/12"),
        )
        val savedState = SavedStateHandle(
            mapOf(
                "launch_state_kind" to "Ready",
                "launch_state_photo_ids" to longArrayOf(4L, 9L, 12L),
                "launch_state_photo_uris" to arrayListOf(
                    "content://photo/4",
                    "content://photo/9",
                    "content://photo/12",
                ),
                "launch_state_current_index" to 1,
            ),
        )

        val viewModel = buildViewModel(savedStateHandle = savedState)

        assertEquals(
            LaunchUiState.Ready(
                session = LaunchSession(
                    photos = restoredPhotos,
                    currentIndex = 1,
                ),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `saved state ignores unknown kinds and falls back to permission state`() {
        val savedState = SavedStateHandle(
            mapOf(
                "launch_state_kind" to "UnexpectedState",
                "launch_state_hint" to true,
            ),
        )

        val viewModel = buildViewModel(savedStateHandle = savedState)

        assertEquals(
            LaunchUiState.NeedsPermission(showSettingsHint = true),
            viewModel.uiState.value,
        )
    }

    private fun buildViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        photoRepository: FakePhotoRepository = FakePhotoRepository(),
    ): LaunchViewModel {
        return LaunchViewModel(
            savedStateHandle = savedStateHandle,
            photoRepository = photoRepository,
            launchSessionBuilder = LaunchSessionBuilder(
                shuffler = LaunchPhotoShuffler { candidates -> candidates },
            ),
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
