package com.jimmymacmini.wishdtmf.data.media

data class LocalPhoto(
    val id: Long,
    val contentUri: String,
    val displayName: String? = null,
    val mimeType: String? = null,
    val dateTakenMillis: Long? = null,
    val dateAddedSeconds: Long = 0L,
    val sizeBytes: Long = 0L,
)

interface PhotoRepository {
    suspend fun loadEligiblePhotos(limitHint: Int? = null): List<LocalPhoto>
}
