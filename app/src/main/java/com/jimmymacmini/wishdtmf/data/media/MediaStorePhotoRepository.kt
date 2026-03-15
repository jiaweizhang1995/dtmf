package com.jimmymacmini.wishdtmf.data.media

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

class MediaStorePhotoRepository private constructor(
    private val querySource: PhotoQuerySource,
    private val mapper: PhotoQueryMapper,
    private val reviewQuerySource: ReviewPhotoQuerySource,
) : PhotoRepository {

    constructor(
        contentResolver: ContentResolver,
        mapper: PhotoQueryMapper = PhotoQueryMapper(),
    ) : this(
        querySource = ContentResolverPhotoQuerySource(
            contentResolver = contentResolver,
            mapper = mapper,
        ),
        mapper = mapper,
        reviewQuerySource = ContentResolverReviewPhotoQuerySource(contentResolver),
    )

    override suspend fun loadEligiblePhotos(limitHint: Int?): List<LocalPhoto> {
        return querySource.query(limitHint)
            .mapNotNull(mapper::mapEligiblePhoto)
    }

    /**
     * Resolve [orderedIds] into [ReviewPhoto] cards preserving order. IDs missing from
     * MediaStore are silently omitted so the grid stays consistent with available media.
     */
    override suspend fun loadReviewPhotos(orderedIds: List<Long>): List<ReviewPhoto> {
        if (orderedIds.isEmpty()) return emptyList()
        val resolved = reviewQuerySource.queryByIds(orderedIds)
        // Preserve the caller's ordering; omit IDs that could not be resolved.
        return orderedIds.mapNotNull { id -> resolved[id] }
    }

    /**
     * Resolve [photoIds] into content [Uri]s suitable for a MediaStore delete request.
     * IDs that no longer exist in MediaStore are silently omitted.
     */
    override suspend fun resolveUrisForDelete(photoIds: Set<Long>): List<Uri> {
        if (photoIds.isEmpty()) return emptyList()
        val resolved = reviewQuerySource.queryByIds(photoIds.toList())
        return photoIds.mapNotNull { id ->
            resolved[id]?.contentUri?.let { Uri.parse(it) }
        }
    }

    internal companion object {
        fun forTesting(
            querySource: PhotoQuerySource,
            mapper: PhotoQueryMapper = PhotoQueryMapper(),
            reviewQuerySource: ReviewPhotoQuerySource = EmptyReviewPhotoQuerySource,
        ): MediaStorePhotoRepository {
            return MediaStorePhotoRepository(
                querySource = querySource,
                mapper = mapper,
                reviewQuerySource = reviewQuerySource,
            )
        }
    }
}

internal fun interface PhotoQuerySource {
    fun query(limitHint: Int?): List<PhotoQueryRow>
}

/** Returns a map of id -> ReviewPhoto for the given IDs. Missing IDs are absent from the map. */
internal fun interface ReviewPhotoQuerySource {
    fun queryByIds(ids: List<Long>): Map<Long, ReviewPhoto>
}

private object EmptyReviewPhotoQuerySource : ReviewPhotoQuerySource {
    override fun queryByIds(ids: List<Long>): Map<Long, ReviewPhoto> = emptyMap()
}

private class ContentResolverPhotoQuerySource(
    private val contentResolver: ContentResolver,
    private val mapper: PhotoQueryMapper,
) : PhotoQuerySource {

    override fun query(limitHint: Int?): List<PhotoQueryRow> {
        // Phase 1 intentionally scans the full visible image library so later random selection
        // is not biased toward recency. `limitHint` is accepted by the contract but not enforced here.
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mapper.projection(),
            null,
            null,
            mapper.sortOrder(),
        ) ?: return emptyList()

        return cursor.use(mapper::readRows)
    }
}

private class ContentResolverReviewPhotoQuerySource(
    private val contentResolver: ContentResolver,
) : ReviewPhotoQuerySource {

    private val baseUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override fun queryByIds(ids: List<Long>): Map<Long, ReviewPhoto> {
        if (ids.isEmpty()) return emptyMap()

        // Build a WHERE IN clause for the requested IDs.
        val placeholders = ids.joinToString(",") { "?" }
        val selection = "${MediaStore.Images.Media._ID} IN ($placeholders)"
        val selectionArgs = ids.map { it.toString() }.toTypedArray()

        val cursor = contentResolver.query(
            baseUri,
            arrayOf(MediaStore.Images.Media._ID),
            selection,
            selectionArgs,
            null,
        ) ?: return emptyMap()

        return cursor.use { c ->
            val result = mutableMapOf<Long, ReviewPhoto>()
            val idIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (c.moveToNext()) {
                val id = c.getLong(idIndex)
                val contentUri = ContentUris.withAppendedId(baseUri, id).toString()
                result[id] = ReviewPhoto(id = id, contentUri = contentUri)
            }
            result
        }
    }
}
