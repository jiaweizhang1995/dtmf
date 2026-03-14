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
        assertTrue(state.isSessionComplete)
    }

    private fun sampleSession(): LaunchSession = LaunchSession(
        photos = listOf(
            LocalPhoto(id = 1, contentUri = "content://test/1"),
            LocalPhoto(id = 2, contentUri = "content://test/2"),
            LocalPhoto(id = 3, contentUri = "content://test/3"),
        ),
    )
}
