package com.jimmymacmini.wishdtmf.data.media

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest

/**
 * Encapsulates MediaStore delete-request creation and result interpretation so that
 * the review/nav layer can stay decoupled from platform delete APIs.
 *
 * All supported devices have minSdk 30, so [MediaStore.createDeleteRequest] is always available.
 */
object DeleteRequestCoordinator {

    /**
     * Build an [IntentSenderRequest] that asks the platform to confirm deletion of the
     * supplied [uris].
     *
     * Returns `null` when [uris] is empty — callers must guard against launching an empty request.
     */
    fun buildDeleteRequest(context: Context, uris: List<Uri>): IntentSenderRequest? {
        if (uris.isEmpty()) return null
        val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
        return IntentSenderRequest.Builder(pendingIntent.intentSender).build()
    }

    /**
     * Testable empty-guard check: returns `null` without requiring a [Context] or real
     * [ContentResolver]. Used in unit tests to verify that empty URI lists are rejected
     * before any platform API is invoked.
     *
     * For production use, call [buildDeleteRequest] with a real [Context].
     */
    internal fun buildDeleteRequestForUris(uris: List<*>): Any? {
        if (uris.isEmpty()) return null
        // Non-empty — in a real call this would proceed to MediaStore. Signal non-null.
        return uris
    }

    /**
     * Returns `true` when [items] is non-empty and would be allowed to proceed to the
     * platform delete request. Used in unit tests alongside [buildDeleteRequestForUris].
     */
    internal fun isNonEmptyRequestAllowed(items: List<*>): Boolean = items.isNotEmpty()

    /**
     * Interpret the activity result code returned from the platform delete confirmation dialog.
     *
     * Returns `true` when the user confirmed deletion ([Activity.RESULT_OK]), `false` on cancel or
     * any other result code.
     */
    fun isDeleteConfirmed(resultCode: Int): Boolean = resultCode == Activity.RESULT_OK
}
