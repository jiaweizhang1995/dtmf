package com.jimmymacmini.wishdtmf.feature.main

import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import com.jimmymacmini.wishdtmf.domain.LaunchSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun photoPresentationMapper_keepsActivePhotoMetadataAndThumbnailWindowAligned() {
        val presentation = PhotoPresentationMapper.map(sampleSession().withCurrentIndex(3))

        assertEquals(3, presentation.activePhotoIndex)
        assertEquals(4L, presentation.activePhoto.id)
        assertEquals("4/5", presentation.currentPositionLabel)
        assertTrue(presentation.visibleThumbnails.any { it.id == 4L && it.isCurrent })
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

    private fun sampleSession(): LaunchSession = LaunchSession(
        photos = (1L..5L).map { id ->
            LocalPhoto(
                id = id,
                contentUri = "content://test/photos/$id",
                mimeType = "image/jpeg",
                dateTakenMillis = 1_712_556_800_000,
                sizeBytes = 4_500_000,
            )
        },
    )
}
