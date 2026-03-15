package com.jimmymacmini.wishdtmf.data.media

import android.net.Uri

data class LocalPhoto(
    val id: Long,
    val contentUri: String,
    val displayName: String? = null,
    val mimeType: String? = null,
    val dateTakenMillis: Long? = null,
    val dateAddedSeconds: Long = 0L,
    val sizeBytes: Long = 0L,
)

/**
 * Lightweight display model for review cards. Contains only the data needed to
 * render a grid tile; heavier metadata is not needed at review time.
 */
data class ReviewPhoto(
    val id: Long,
    val contentUri: String,
)

interface PhotoRepository {
    suspend fun loadEligiblePhotos(limitHint: Int? = null): List<LocalPhoto>

    /**
     * Resolve a deterministic ordered list of staged photo IDs into [ReviewPhoto] cards at the
     * review boundary. IDs that no longer exist in MediaStore are silently omitted so the review
     * grid stays consistent with what is actually available.
     *
     * The returned list preserves the original order of [orderedIds].
     */
    suspend fun loadReviewPhotos(orderedIds: List<Long>): List<ReviewPhoto>

    /**
     * Resolve the given set of staged photo IDs into their content [Uri]s for use in a delete
     * request. IDs that no longer exist in MediaStore are silently omitted.
     *
     * Returns an empty list when [photoIds] is empty or no IDs could be resolved.
     */
    suspend fun resolveUrisForDelete(photoIds: Set<Long>): List<Uri>
}
