package com.jimmymacmini.wishdtmf.domain

enum class SwipeDirection {
    Left,
    Right,
}

data class SwipeDecision(
    val photoId: Long,
    val direction: SwipeDirection,
    val previousIndex: Int,
)

data class SwipeSessionState(
    val currentIndex: Int = 0,
    val stagedPhotoIds: Set<Long> = emptySet(),
    val lastDecision: SwipeDecision? = null,
    val isSessionComplete: Boolean = false,
)

object SwipeDecisionReducer {

    fun stageCurrentPhoto(
        session: LaunchSession,
        state: SwipeSessionState,
    ): SwipeSessionState = commit(session, state, SwipeDirection.Left)

    fun skipCurrentPhoto(
        session: LaunchSession,
        state: SwipeSessionState,
    ): SwipeSessionState = commit(session, state, SwipeDirection.Right)

    fun undoLastDecision(
        session: LaunchSession,
        state: SwipeSessionState,
    ): SwipeSessionState {
        val lastDecision = state.lastDecision ?: return state
        val previousIndex = lastDecision.previousIndex.coerceIn(0, session.photos.lastIndex)

        return SwipeSessionState(
            currentIndex = previousIndex,
            stagedPhotoIds = if (lastDecision.direction == SwipeDirection.Left) {
                state.stagedPhotoIds - lastDecision.photoId
            } else {
                state.stagedPhotoIds
            },
            lastDecision = null,
            isSessionComplete = false,
        )
    }

    private fun commit(
        session: LaunchSession,
        state: SwipeSessionState,
        direction: SwipeDirection,
    ): SwipeSessionState {
        if (state.isSessionComplete) {
            return state
        }

        val boundedIndex = state.currentIndex.coerceIn(0, session.photos.lastIndex)
        val photoId = session.photos[boundedIndex].id
        val isTerminalPhoto = boundedIndex == session.photos.lastIndex

        return SwipeSessionState(
            currentIndex = if (isTerminalPhoto) boundedIndex else boundedIndex + 1,
            stagedPhotoIds = if (direction == SwipeDirection.Left) {
                state.stagedPhotoIds + photoId
            } else {
                state.stagedPhotoIds
            },
            lastDecision = SwipeDecision(
                photoId = photoId,
                direction = direction,
                previousIndex = boundedIndex,
            ),
            isSessionComplete = isTerminalPhoto,
        )
    }
}
