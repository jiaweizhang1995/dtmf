package com.jimmymacmini.wishdtmf.data.media

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoRepositoryTest {

    @Test
    fun `repository keeps only normal image rows`() = runTest {
        val repository = MediaStorePhotoRepository.forTesting(
            querySource = PhotoQuerySource {
                listOf(
                    imageRow(id = 1L),
                    imageRow(id = 2L, mimeType = "video/mp4"),
                    imageRow(id = 3L, isPending = true),
                    imageRow(id = 4L, isTrashed = true),
                    imageRow(id = 5L, relativePath = "Pictures/.hidden/"),
                    imageRow(id = 6L, displayName = ".secret.jpg"),
                )
            },
        )

        val photos = repository.loadEligiblePhotos()

        assertEquals(listOf(1L), photos.map(LocalPhoto::id))
        assertTrue(photos.first().contentUri.startsWith("content://"))
    }

    @Test
    fun `repository returns fewer than 30 items when that is all that is available`() = runTest {
        val repository = MediaStorePhotoRepository.forTesting(
            querySource = PhotoQuerySource {
                List(7) { index -> imageRow(id = index + 1L) }
            },
        )

        val photos = repository.loadEligiblePhotos(limitHint = 30)

        assertEquals(7, photos.size)
    }

    @Test
    fun `repository preserves metadata needed for later phases`() = runTest {
        val repository = MediaStorePhotoRepository.forTesting(
            querySource = PhotoQuerySource {
                listOf(
                    imageRow(
                        id = 99L,
                        displayName = "IMG_0099.JPG",
                        dateTakenMillis = 1_710_000_000_000L,
                        dateAddedSeconds = 1_710_000_000L,
                        sizeBytes = 42_000L,
                    ),
                )
            },
        )

        val photo = repository.loadEligiblePhotos().single()

        assertEquals("IMG_0099.JPG", photo.displayName)
        assertEquals("image/jpeg", photo.mimeType)
        assertEquals(1_710_000_000_000L, photo.dateTakenMillis)
        assertEquals(1_710_000_000L, photo.dateAddedSeconds)
        assertEquals(42_000L, photo.sizeBytes)
    }
}

private fun imageRow(
    id: Long,
    displayName: String? = "IMG_$id.jpg",
    mimeType: String? = "image/jpeg",
    dateTakenMillis: Long? = 1_700_000_000_000L,
    dateAddedSeconds: Long = 1_700_000_000L,
    sizeBytes: Long = 4_096L,
    relativePath: String? = "Pictures/Camera/",
    isPending: Boolean = false,
    isTrashed: Boolean = false,
): PhotoQueryRow {
    return PhotoQueryRow(
        id = id,
        displayName = displayName,
        mimeType = mimeType,
        dateTakenMillis = dateTakenMillis,
        dateAddedSeconds = dateAddedSeconds,
        sizeBytes = sizeBytes,
        relativePath = relativePath,
        isPending = isPending,
        isTrashed = isTrashed,
    )
}
