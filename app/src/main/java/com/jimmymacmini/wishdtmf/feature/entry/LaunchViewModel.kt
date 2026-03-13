package com.jimmymacmini.wishdtmf.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class LaunchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val photoRepository: PhotoRepository,
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

    private fun loadBatch() {
        updateState(LaunchUiState.LoadingBatch)
        viewModelScope.launch {
            val nextState = runCatching {
                withContext(ioDispatcher) {
                    photoRepository.loadEligiblePhotos(limitHint = BATCH_SIZE)
                }
            }.fold(
                onSuccess = { photos ->
                    if (photos.isEmpty()) {
                        LaunchUiState.Empty
                    } else {
                        LaunchUiState.Ready(photoCount = min(BATCH_SIZE, photos.size))
                    }
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
private const val STATE_COUNT_KEY = "launch_state_count"
private const val STATE_MESSAGE_KEY = "launch_state_message"

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
        LaunchStateKind.Ready -> LaunchUiState.Ready(
            photoCount = (get<Int>(STATE_COUNT_KEY) ?: BATCH_SIZE)
                .coerceIn(1, BATCH_SIZE),
        )
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
            set<Int?>(STATE_COUNT_KEY, null)
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        LaunchUiState.LoadingBatch -> {
            set(STATE_KIND_KEY, LaunchStateKind.LoadingBatch.name)
            set<Boolean?>(STATE_HINT_KEY, null)
            set<Int?>(STATE_COUNT_KEY, null)
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        is LaunchUiState.Ready -> {
            set(STATE_KIND_KEY, LaunchStateKind.Ready.name)
            set(STATE_COUNT_KEY, state.photoCount)
            set<Boolean?>(STATE_HINT_KEY, null)
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        LaunchUiState.Empty -> {
            set(STATE_KIND_KEY, LaunchStateKind.Empty.name)
            set<Boolean?>(STATE_HINT_KEY, null)
            set<Int?>(STATE_COUNT_KEY, null)
            set<String?>(STATE_MESSAGE_KEY, null)
        }

        is LaunchUiState.Error -> {
            set(STATE_KIND_KEY, LaunchStateKind.Error.name)
            set(STATE_MESSAGE_KEY, state.message)
            set<Boolean?>(STATE_HINT_KEY, null)
            set<Int?>(STATE_COUNT_KEY, null)
        }
    }
}

private fun String?.toLaunchStateKind(): LaunchStateKind? {
    return LaunchStateKind.entries.firstOrNull { it.name == this }
}
