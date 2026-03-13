package com.jimmymacmini.wishdtmf.data.media

import android.database.Cursor
import android.os.Build
import android.provider.MediaStore

internal data class PhotoQueryRow(
    val id: Long,
    val displayName: String?,
    val mimeType: String?,
    val dateTakenMillis: Long?,
    val dateAddedSeconds: Long,
    val sizeBytes: Long,
    val relativePath: String?,
    val isPending: Boolean,
    val isTrashed: Boolean,
)

class PhotoQueryMapper(
    private val sdkInt: Int = Build.VERSION.SDK_INT,
) {
    fun projection(): Array<String> {
        return arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.IS_PENDING,
            MediaStore.Images.Media.IS_TRASHED,
        )
    }

    fun sortOrder(): String = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    internal fun readRows(cursor: Cursor): List<PhotoQueryRow> {
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val dateTakenIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val relativePathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
        val pendingIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_PENDING)
        val trashedIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_TRASHED)
        val rows = mutableListOf<PhotoQueryRow>()

        while (cursor.moveToNext()) {
            rows += PhotoQueryRow(
                id = cursor.getLong(idIndex),
                displayName = cursor.getString(displayNameIndex),
                mimeType = cursor.getString(mimeTypeIndex),
                dateTakenMillis = cursor.getLongOrNull(dateTakenIndex),
                dateAddedSeconds = cursor.getLong(dateAddedIndex),
                sizeBytes = cursor.getLong(sizeIndex),
                relativePath = cursor.getString(relativePathIndex),
                isPending = cursor.getInt(pendingIndex) != 0,
                isTrashed = if (sdkInt >= Build.VERSION_CODES.R) {
                    cursor.getInt(trashedIndex) != 0
                } else {
                    false
                },
            )
        }

        return rows
    }

    internal fun mapEligiblePhoto(row: PhotoQueryRow): LocalPhoto? {
        if (row.id <= 0L) return null
        if (row.sizeBytes <= 0L) return null
        if (row.isPending || row.isTrashed) return null
        if (row.mimeType?.startsWith("image/") != true) return null
        // Hidden/shared library behavior varies by OEM. Phase 1 makes a best-effort exclusion
        // by skipping dot-prefixed paths and file names, but does not claim perfect coverage.
        if (row.displayName?.startsWith(".") == true) return null
        if (row.relativePath?.split('/')?.any { it.startsWith(".") } == true) return null

        return LocalPhoto(
            id = row.id,
            contentUri = "content://media/external/images/media/${row.id}",
            displayName = row.displayName,
            mimeType = row.mimeType,
            dateTakenMillis = row.dateTakenMillis,
            dateAddedSeconds = row.dateAddedSeconds,
            sizeBytes = row.sizeBytes,
        )
    }
}

private fun Cursor.getLongOrNull(index: Int): Long? {
    return if (isNull(index)) {
        null
    } else {
        getLong(index)
    }
}
