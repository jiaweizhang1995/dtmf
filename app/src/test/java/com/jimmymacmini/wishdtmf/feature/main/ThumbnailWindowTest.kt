package com.jimmymacmini.wishdtmf.feature.main

import org.junit.Assert.assertEquals
import org.junit.Test

class ThumbnailWindowTest {

    @Test
    fun deriveThumbnailWindow_keepsLeadingContextAtSessionStart() {
        val window = deriveThumbnailWindow(samplePhotos(), currentIndex = 0)

        assertEquals((1L..4L).toList(), window.map { it.id })
    }

    @Test
    fun deriveThumbnailWindow_centersCurrentPhotoInSessionMiddle() {
        val window = deriveThumbnailWindow(samplePhotos(), currentIndex = 14)

        assertEquals((12L..18L).toList(), window.map { it.id })
        assertEquals(15L, window.single { it.isCurrent }.id)
    }

    @Test
    fun deriveThumbnailWindow_keepsTrailingContextAtSessionEnd() {
        val window = deriveThumbnailWindow(samplePhotos(), currentIndex = 29)

        assertEquals((27L..30L).toList(), window.map { it.id })
    }

    private fun samplePhotos(): List<MainPhotoPresentation> = (1L..30L).map { id ->
        MainPhotoPresentation(
            id = id,
            contentUri = "content://test/photos/$id",
            thumbnailContentDescription = "Thumbnail $id",
            heroContentDescription = "Photo $id",
            isCurrent = id == 15L,
        )
    }
}
