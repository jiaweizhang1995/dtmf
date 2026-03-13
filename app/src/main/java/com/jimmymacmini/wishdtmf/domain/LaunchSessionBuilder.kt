package com.jimmymacmini.wishdtmf.domain

import com.jimmymacmini.wishdtmf.data.media.LocalPhoto

private const val DEFAULT_BATCH_SIZE = 30

data class LaunchSession(
    val photos: List<LocalPhoto>,
    val currentIndex: Int = 0,
) {
    init {
        require(photos.isNotEmpty()) { "LaunchSession requires at least one photo." }
        require(currentIndex in photos.indices) {
            "Current index $currentIndex must point at an existing photo."
        }
    }

    val photoCount: Int
        get() = photos.size

    fun withCurrentIndex(index: Int): LaunchSession {
        require(index in photos.indices) {
            "Current index $index must point at an existing photo."
        }
        return copy(currentIndex = index)
    }
}

fun interface LaunchPhotoShuffler {
    fun shuffle(photos: List<LocalPhoto>): List<LocalPhoto>
}

class LaunchSessionBuilder(
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
    private val shuffler: LaunchPhotoShuffler = LaunchPhotoShuffler { photos -> photos.shuffled() },
) {
    init {
        require(batchSize > 0) { "Batch size must be positive." }
    }

    fun build(eligiblePhotos: List<LocalPhoto>): LaunchSession? {
        if (eligiblePhotos.isEmpty()) {
            return null
        }

        val batch = shuffler.shuffle(eligiblePhotos).take(batchSize)
        return LaunchSession(photos = batch)
    }
}
