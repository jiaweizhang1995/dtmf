package com.jimmymacmini.wishdtmf.domain

import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeDecisionReducerTest {

    @Test
    fun stageCurrentPhoto_addsCurrentPhotoAndAdvances() {
        val state = SwipeDecisionReducer.stageCurrentPhoto(sampleSession(), SwipeSessionState())

        assertEquals(1, state.currentIndex)
        assertEquals(setOf(1L), state.stagedPhotoIds)
        assertEquals(SwipeDirection.Left, state.lastDecision?.direction)
        assertEquals(0, state.lastDecision?.previousIndex)
        assertFalse(state.isSessionComplete)
    }

    @Test
    fun skipCurrentPhoto_leavesStagedIdsUntouchedAndAdvances() {
        val state = SwipeDecisionReducer.skipCurrentPhoto(
            session = sampleSession(),
            state = SwipeSessionState(stagedPhotoIds = setOf(9L)),
        )

        assertEquals(1, state.currentIndex)
        assertEquals(setOf(9L), state.stagedPhotoIds)
        assertEquals(SwipeDirection.Right, state.lastDecision?.direction)
        assertEquals(0, state.lastDecision?.previousIndex)
        assertFalse(state.isSessionComplete)
    }

    @Test
    fun terminalSwipe_marksSessionCompleteWithoutOverflowingIndex() {
        val state = SwipeDecisionReducer.stageCurrentPhoto(
            session = sampleSession(),
            state = SwipeSessionState(currentIndex = 2),
        )

        assertEquals(2, state.currentIndex)
        assertEquals(setOf(3L), state.stagedPhotoIds)
        assertEquals(2, state.lastDecision?.previousIndex)
        assertTrue(state.isSessionComplete)
    }

    @Test
    fun completedSession_ignoresAdditionalSwipeAttempts() {
        val initial = SwipeSessionState(
            currentIndex = 2,
            stagedPhotoIds = setOf(3L),
            lastDecision = SwipeDecision(photoId = 3L, direction = SwipeDirection.Left, previousIndex = 2),
            isSessionComplete = true,
        )

        val state = SwipeDecisionReducer.skipCurrentPhoto(
            session = sampleSession(),
            state = initial,
        )

        assertEquals(initial, state)
    }

    @Test
    fun undoLastDecision_revertsLeftSwipeAndRemovesStagedPhoto() {
        val committed = SwipeDecisionReducer.stageCurrentPhoto(sampleSession(), SwipeSessionState())

        val state = SwipeDecisionReducer.undoLastDecision(
            session = sampleSession(),
            state = committed,
        )

        assertEquals(0, state.currentIndex)
        assertTrue(state.stagedPhotoIds.isEmpty())
        assertEquals(null, state.lastDecision)
        assertFalse(state.isSessionComplete)
    }

    @Test
    fun undoLastDecision_revertsRightSwipeWithoutChangingStagedSet() {
        val committed = SwipeDecisionReducer.skipCurrentPhoto(
            session = sampleSession(),
            state = SwipeSessionState(stagedPhotoIds = setOf(9L)),
        )

        val state = SwipeDecisionReducer.undoLastDecision(
            session = sampleSession(),
            state = committed,
        )

        assertEquals(0, state.currentIndex)
        assertEquals(setOf(9L), state.stagedPhotoIds)
        assertEquals(null, state.lastDecision)
        assertFalse(state.isSessionComplete)
    }

    @Test
    fun undoLastDecision_revertsTerminalSwipeAndClearsCompletedState() {
        val committed = SwipeDecisionReducer.stageCurrentPhoto(
            session = sampleSession(),
            state = SwipeSessionState(currentIndex = 2),
        )

        val state = SwipeDecisionReducer.undoLastDecision(
            session = sampleSession(),
            state = committed,
        )

        assertEquals(2, state.currentIndex)
        assertTrue(state.stagedPhotoIds.isEmpty())
        assertEquals(null, state.lastDecision)
        assertFalse(state.isSessionComplete)
    }

    @Test
    fun undoLastDecision_withoutHistory_isNoOp() {
        val initial = SwipeSessionState(stagedPhotoIds = setOf(2L))

        val state = SwipeDecisionReducer.undoLastDecision(
            session = sampleSession(),
            state = initial,
        )

        assertEquals(initial, state)
    }

    private fun sampleSession(): LaunchSession = LaunchSession(
        photos = listOf(
            LocalPhoto(id = 1, contentUri = "content://test/1"),
            LocalPhoto(id = 2, contentUri = "content://test/2"),
            LocalPhoto(id = 3, contentUri = "content://test/3"),
        ),
    )
}
