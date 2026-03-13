package com.jimmymacmini.wishdtmf.data.media

data class LocalPhoto(
    val id: Long,
    val contentUri: String,
)

interface PhotoRepository {
    suspend fun loadEligiblePhotos(limitHint: Int? = null): List<LocalPhoto>
}
