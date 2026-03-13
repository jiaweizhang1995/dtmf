package com.jimmymacmini.wishdtmf.app

import android.app.Application
import com.jimmymacmini.wishdtmf.data.media.MediaStorePhotoRepository
import com.jimmymacmini.wishdtmf.data.media.PhotoRepository

class App : Application() {
    private val defaultPhotoRepository: PhotoRepository by lazy {
        MediaStorePhotoRepository(contentResolver = contentResolver)
    }

    val photoRepository: PhotoRepository
        get() = photoRepositoryOverride ?: defaultPhotoRepository

    companion object {
        var photoRepositoryOverride: PhotoRepository? = null
    }
}
