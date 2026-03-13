package com.jimmymacmini.wishdtmf.data.media

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore

class MediaStorePhotoRepository private constructor(
    private val querySource: PhotoQuerySource,
    private val mapper: PhotoQueryMapper,
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
    )

    override suspend fun loadEligiblePhotos(limitHint: Int?): List<LocalPhoto> {
        return querySource.query(limitHint)
            .mapNotNull(mapper::mapEligiblePhoto)
    }

    internal companion object {
        fun forTesting(
            querySource: PhotoQuerySource,
            mapper: PhotoQueryMapper = PhotoQueryMapper(),
        ): MediaStorePhotoRepository {
            return MediaStorePhotoRepository(
                querySource = querySource,
                mapper = mapper,
            )
        }
    }
}

internal fun interface PhotoQuerySource {
    fun query(limitHint: Int?): List<PhotoQueryRow>
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
