package com.jimmymacmini.wishdtmf.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.LaunchSessionBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaunchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val photoRepository: PhotoRepository,
    private val launchSessionBuilder: LaunchSessionBuilder = LaunchSessionBuilder(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow(savedStateHandle.restoreState())
    val uiState: StateFlow<LaunchUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            loadBatch()
        } else {
            updateState(LaunchUiState.NeedsPermission(showSettingsHint = true))
        }
    }

    fun retry() {
        when (val currentState = _uiState.value) {
            is LaunchUiState.Error,
            LaunchUiState.Empty,
            LaunchUiState.LoadingBatch,
            is LaunchUiState.Ready -> loadBatch()
            is LaunchUiState.NeedsPermission -> {
                updateState(currentState.copy(showSettingsHint = false))
            }
        }
    }

    fun advanceToNextPhoto() {
        val currentState = _uiState.value as? LaunchUiState.Ready ?: return
        val nextIndex = (currentState.session.currentIndex + 1)
            .coerceAtMost(currentState.session.photoCount - 1)
        if (nextIndex != currentState.session.currentIndex) {
            updateState(
                LaunchUiState.Ready(
                    session = currentState.session.withCurrentIndex(nextIndex),
                ),
            )
        }
    }

    private fun loadBatch() {
        updateState(LaunchUiState.LoadingBatch)
        viewModelScope.launch {
            val nextState = runCatching {
                withContext(ioDispatcher) {
                    photoRepository.loadEligiblePhotos()
                }
            }.fold(
                onSuccess = { photos ->
                    launchSessionBuilder.build(photos)?.let { session ->
                        LaunchUiState.Ready(session = session)
                    } ?: LaunchUiState.Empty
                },
                onFailure = { error ->
                    LaunchUiState.Error(
                        message = error.message ?: DEFAULT_ERROR_MESSAGE,
                    )
                },
            )
            updateState(nextState)
        }
    }

    private fun updateState(state: LaunchUiState) {
        savedStateHandle.persistState(state)
        _uiState.value = state
    }

    companion object {
        fun factory(photoRepository: PhotoRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T {
                    return LaunchViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        photoRepository = photoRepository,
                    ) as T
                }
            }
        }
    }
}

private const val BATCH_SIZE = 30
private const val DEFAULT_ERROR_MESSAGE = "Could not prepare a photo batch."
private const val STATE_KIND_KEY = "launch_state_kind"
private const val STATE_HINT_KEY = "launch_state_hint"
private const val STATE_MESSAGE_KEY = "launch_state_message"
private const val STATE_PHOTO_IDS_KEY = "launch_state_photo_ids"
private const val STATE_PHOTO_URIS_KEY = "launch_state_photo_uris"
private const val STATE_CURRENT_INDEX_KEY = "launch_state_current_index"

private enum class LaunchStateKind {
    NeedsPermission,
    LoadingBatch,
    Ready,
    Empty,
    Error,
}

private fun SavedStateHandle.restoreState(): LaunchUiState {
    return when (get<String>(STATE_KIND_KEY).toLaunchStateKind()) {
        LaunchStateKind.LoadingBatch -> LaunchUiState.LoadingBatch
        LaunchStateKind.Ready -> restoreReadyState()
        LaunchStateKind.Empty -> LaunchUiState.Empty
        LaunchStateKind.Error -> LaunchUiState.Error(
            message = get<String>(STATE_MESSAGE_KEY) ?: DEFAULT_ERROR_MESSAGE,
        )
        LaunchStateKind.NeedsPermission,
        null -> LaunchUiState.NeedsPermission(
            showSettingsHint = get<Boolean>(STATE_HINT_KEY) ?: false,
        )
    }
}

private fun SavedStateHandle.persistState(state: LaunchUiState) {
    when (state) {
        is LaunchUiState.NeedsPermission -> {
            set(STATE_KIND_KEY, LaunchStateKind.NeedsPermission.name)
            set(STATE_HINT_KEY, state.showSettingsHint)
            clearReadyState()
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        LaunchUiState.LoadingBatch -> {
            set(STATE_KIND_KEY, LaunchStateKind.LoadingBatch.name)
            set<Boolean?>(STATE_HINT_KEY, null)
            clearReadyState()
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        is LaunchUiState.Ready -> {
            set(STATE_KIND_KEY, LaunchStateKind.Ready.name)
            set(STATE_PHOTO_IDS_KEY, state.session.photos.map { it.id }.toLongArray())
            set(STATE_PHOTO_URIS_KEY, ArrayList(state.session.photos.map { it.contentUri }))
            set(STATE_CURRENT_INDEX_KEY, state.session.currentIndex)
            set<Boolean?>(STATE_HINT_KEY, null)
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        LaunchUiState.Empty -> {
            set(STATE_KIND_KEY, LaunchStateKind.Empty.name)
            set<Boolean?>(STATE_HINT_KEY, null)
            clearReadyState()
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        is LaunchUiState.Error -> {
            set(STATE_KIND_KEY, LaunchStateKind.Error.name)
            set(STATE_MESSAGE_KEY, state.message)
            set<Boolean?>(STATE_HINT_KEY, null)
            clearReadyState()
        }
    }
}

private fun SavedStateHandle.restoreReadyState(): LaunchUiState {
    val ids = get<LongArray>(STATE_PHOTO_IDS_KEY)?.toList().orEmpty()
    val uris = get<ArrayList<String>>(STATE_PHOTO_URIS_KEY).orEmpty()
    val photos = ids.zip(uris) { id, uri ->
        LocalPhoto(id = id, contentUri = uri)
    }

    return if (photos.isEmpty()) {
        LaunchUiState.NeedsPermission(
            showSettingsHint = get<Boolean>(STATE_HINT_KEY) ?: false,
        )
    } else {
        val currentIndex = (get<Int>(STATE_CURRENT_INDEX_KEY) ?: 0)
            .coerceIn(0, photos.lastIndex)
        LaunchUiState.Ready(
            session = LaunchSession(
                photos = photos,
                currentIndex = currentIndex,
            ),
        )
    }
}

private fun SavedStateHandle.clearReadyState() {
    set<LongArray?>(STATE_PHOTO_IDS_KEY, null)
    set<ArrayList<String>?>(STATE_PHOTO_URIS_KEY, null)
    set<Int?>(STATE_CURRENT_INDEX_KEY, null)
}

private fun String?.toLaunchStateKind(): LaunchStateKind? {
    return LaunchStateKind.entries.firstOrNull { it.name == this }
}
