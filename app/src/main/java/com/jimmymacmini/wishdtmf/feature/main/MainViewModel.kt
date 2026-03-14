package com.jimmymacmini.wishdtmf.feature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import com.jimmymacmini.wishdtmf.domain.SwipeDecision
import com.jimmymacmini.wishdtmf.domain.SwipeDecisionReducer
import com.jimmymacmini.wishdtmf.domain.SwipeDirection
import com.jimmymacmini.wishdtmf.domain.SwipeSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val session: LaunchSession,
) : ViewModel() {

    private val swipeState = MutableStateFlow(savedStateHandle.restoreSwipeState(session))
    private val _navigationEvents = MutableSharedFlow<MainNavigationEvent>(extraBufferCapacity = 1)
    val navigationEvents: SharedFlow<MainNavigationEvent> = _navigationEvents.asSharedFlow()
    val uiState: StateFlow<MainUiState> = swipeState
        .map { swipeSessionState ->
            MainUiState.fromSession(
                session = session.withCurrentIndex(swipeSessionState.currentIndex),
                swipeState = swipeSessionState,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = MainUiState.fromSession(
                session = session.withCurrentIndex(swipeState.value.currentIndex),
                swipeState = swipeState.value,
            ),
        )

    fun stageCurrentPhoto() {
        updateState(SwipeDecisionReducer.stageCurrentPhoto(session, swipeState.value))
    }

    fun skipCurrentPhoto() {
        updateState(SwipeDecisionReducer.skipCurrentPhoto(session, swipeState.value))
    }

    fun undoLastDecision() {
        updateState(SwipeDecisionReducer.undoLastDecision(session, swipeState.value))
    }

    fun onProceedToReview() {
        val stagedPhotoIds = swipeState.value.stagedPhotoIds
        if (stagedPhotoIds.isNotEmpty()) {
            _navigationEvents.tryEmit(MainNavigationEvent.OpenReview(stagedPhotoIds))
        }
    }

    private fun updateState(nextState: SwipeSessionState) {
        savedStateHandle.persistSwipeState(session, nextState)
        swipeState.value = nextState
    }

    companion object {
        fun factory(session: LaunchSession): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T {
                    return MainViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        session = session,
                    ) as T
                }
            }
        }
    }
}

sealed interface MainNavigationEvent {
    data class OpenReview(val stagedPhotoIds: Set<Long>) : MainNavigationEvent
}

private const val SWIPE_PHOTO_IDS_KEY = "main_swipe_photo_ids"
private const val SWIPE_CURRENT_INDEX_KEY = "main_swipe_current_index"
private const val SWIPE_STAGED_IDS_KEY = "main_swipe_staged_ids"
private const val SWIPE_LAST_PHOTO_ID_KEY = "main_swipe_last_photo_id"
private const val SWIPE_LAST_DIRECTION_KEY = "main_swipe_last_direction"
private const val SWIPE_LAST_PREVIOUS_INDEX_KEY = "main_swipe_last_previous_index"
private const val SWIPE_COMPLETE_KEY = "main_swipe_complete"

private fun SavedStateHandle.restoreSwipeState(session: LaunchSession): SwipeSessionState {
    val restoredIds = get<LongArray>(SWIPE_PHOTO_IDS_KEY)?.toList()
    val sessionIds = session.photos.map { it.id }
    if (restoredIds != sessionIds) {
        return SwipeSessionState(currentIndex = session.currentIndex)
    }

    val currentIndex = (get<Int>(SWIPE_CURRENT_INDEX_KEY) ?: session.currentIndex)
        .coerceIn(0, session.photos.lastIndex)
    val stagedPhotoIds = get<LongArray>(SWIPE_STAGED_IDS_KEY)?.toSet().orEmpty()
        .intersect(sessionIds.toSet())
    val direction = get<String>(SWIPE_LAST_DIRECTION_KEY)
        ?.let(SwipeDirection::valueOf)
    val lastPhotoId = get<Long>(SWIPE_LAST_PHOTO_ID_KEY)
    val previousIndex = get<Int>(SWIPE_LAST_PREVIOUS_INDEX_KEY)

    return SwipeSessionState(
        currentIndex = currentIndex,
        stagedPhotoIds = stagedPhotoIds,
        lastDecision = if (
            direction != null &&
            lastPhotoId != null &&
            previousIndex != null &&
            sessionIds.contains(lastPhotoId)
        ) {
            SwipeDecision(
                photoId = lastPhotoId,
                direction = direction,
                previousIndex = previousIndex.coerceIn(0, session.photos.lastIndex),
            )
        } else {
            null
        },
        isSessionComplete = get<Boolean>(SWIPE_COMPLETE_KEY) ?: false,
    )
}

private fun SavedStateHandle.persistSwipeState(
    session: LaunchSession,
    state: SwipeSessionState,
) {
    set(SWIPE_PHOTO_IDS_KEY, session.photos.map { it.id }.toLongArray())
    set(SWIPE_CURRENT_INDEX_KEY, state.currentIndex)
    set(SWIPE_STAGED_IDS_KEY, state.stagedPhotoIds.toLongArray())
    set(SWIPE_LAST_PHOTO_ID_KEY, state.lastDecision?.photoId)
    set(SWIPE_LAST_DIRECTION_KEY, state.lastDecision?.direction?.name)
    set(SWIPE_LAST_PREVIOUS_INDEX_KEY, state.lastDecision?.previousIndex)
    set(SWIPE_COMPLETE_KEY, state.isSessionComplete)
}
