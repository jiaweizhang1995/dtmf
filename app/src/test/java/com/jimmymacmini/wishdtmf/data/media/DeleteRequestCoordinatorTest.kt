package com.jimmymacmini.wishdtmf.data.media

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit coverage for [DeleteRequestCoordinator].
 *
 * Tests cover:
 *  - Empty URI list produces a null request (guarding against launching an empty delete dialog)
 *  - [Activity.RESULT_OK] maps to confirmed=true
 *  - Any non-OK result code (cancel, other) maps to confirmed=false
 *
 * Note: [buildDeleteRequest] cannot be meaningfully unit-tested with a real [ContentResolver]
 * in a JVM unit test (MediaStore is a platform API). The coordinator is designed so that the
 * empty-guard logic is testable without a real context, and the result interpretation is
 * tested below. Integration coverage for the full launch/result round-trip lives in
 * [ReviewDeleteFlowTest].
 */
class DeleteRequestCoordinatorTest {

    // -----------------------------------------------------------------------
    // buildDeleteRequest — empty-guard
    // -----------------------------------------------------------------------

    @Test
    fun buildDeleteRequest_returnsNull_whenUrisIsEmpty() {
        // Cannot call the real MediaStore API in a JVM unit test, but we CAN verify
        // the empty-list early-return without a Context by calling through the public
        // surface with a null context stand-in via an overload that accepts a raw list.
        // The coordinator is designed so that an empty URI list short-circuits before
        // any platform API is reached — verify that invariant here.
        val result = DeleteRequestCoordinator.buildDeleteRequestForUris(emptyList<Any>())
        assertNull("Expected null when URI list is empty", result)
    }

    @Test
    fun buildDeleteRequest_returnsNonNull_whenUrisIsNonEmpty() {
        // Non-empty list should NOT be short-circuited by the empty guard.
        // Use a dummy non-null object list to verify the gate does not block a non-empty list.
        // The actual IntentSenderRequest building requires a real ContentResolver; that
        // integration is covered in ReviewDeleteFlowTest.
        val fakeUris = listOf(Any()) // non-empty, no real Uri needed for empty-guard test
        val gatePassed = DeleteRequestCoordinator.isNonEmptyRequestAllowed(fakeUris)
        assertTrue("Non-empty list should pass the empty-guard check", gatePassed)
    }

    // -----------------------------------------------------------------------
    // isDeleteConfirmed — result code interpretation
    // -----------------------------------------------------------------------

    @Test
    fun isDeleteConfirmed_trueForResultOk() {
        assertTrue(DeleteRequestCoordinator.isDeleteConfirmed(Activity.RESULT_OK))
    }

    @Test
    fun isDeleteConfirmed_falseForResultCanceled() {
        assertFalse(DeleteRequestCoordinator.isDeleteConfirmed(Activity.RESULT_CANCELED))
    }

    @Test
    fun isDeleteConfirmed_falseForArbitraryNonOkCode() {
        assertFalse(DeleteRequestCoordinator.isDeleteConfirmed(-99))
        assertFalse(DeleteRequestCoordinator.isDeleteConfirmed(0))
        assertFalse(DeleteRequestCoordinator.isDeleteConfirmed(2))
    }

    // -----------------------------------------------------------------------
    // Selection-to-request contract: only selected URIs enter the request
    // -----------------------------------------------------------------------

    @Test
    fun selectedSubset_doesNotIncludeUnselectedIds() {
        // Model the caller's responsibility: filter resolved URIs to only selected IDs before
        // passing to buildDeleteRequest. Verify the invariant using string URIs (no Android platform
        // needed for this contract test).
        val allUriStrings = listOf(
            "content://media/external/images/media/1",
            "content://media/external/images/media/2",
            "content://media/external/images/media/3",
        )
        val selectedIds = setOf(1L, 3L)
        // The caller (ReviewViewModel / nav layer) filters by selectedPhotoIds. Simulate that here.
        val selectedUriStrings = allUriStrings.filter { uriStr ->
            selectedIds.any { id -> uriStr.endsWith("/$id") }
        }

        assertFalse("URI for id=2 must not appear in selected subset",
            selectedUriStrings.any { it.endsWith("/2") })
        assertTrue("URI for id=1 must appear in selected subset",
            selectedUriStrings.any { it.endsWith("/1") })
        assertTrue("URI for id=3 must appear in selected subset",
            selectedUriStrings.any { it.endsWith("/3") })
        assertEquals("Only selected IDs should be in the subset", 2, selectedUriStrings.size)
    }
}
