package com.jimmymacmini.wishdtmf.domain

import com.jimmymacmini.wishdtmf.data.media.LocalPhoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LaunchSessionBuilderTest {

    @Test
    fun `build uses the full eligible library as the candidate pool`() {
        val photos = photoList(count = 45)
        lateinit var seenPhotos: List<LocalPhoto>
        val builder = LaunchSessionBuilder(
            shuffler = LaunchPhotoShuffler { candidates ->
                seenPhotos = candidates
                candidates.reversed()
            },
        )

        val session = builder.build(photos)

        assertEquals(photos, seenPhotos)
        assertEquals(30, session?.photoCount)
        assertEquals(photos.last(), session?.photos?.first())
    }

    @Test
    fun `build keeps the actual count when fewer than 30 eligible photos exist`() {
        val photos = photoList(count = 7)
        val builder = LaunchSessionBuilder(
            shuffler = LaunchPhotoShuffler { candidates -> candidates },
        )

        val session = builder.build(photos)

        requireNotNull(session)
        assertEquals(7, session.photoCount)
        assertEquals(photos, session.photos)
        assertEquals(0, session.currentIndex)
    }

    @Test
    fun `build returns null when no eligible photos exist`() {
        val builder = LaunchSessionBuilder()

        assertNull(builder.build(emptyList()))
    }

    @Test
    fun `current index updates only within the active batch`() {
        val session = LaunchSession(photos = photoList(count = 3))

        val moved = session.withCurrentIndex(2)

        assertEquals(2, moved.currentIndex)
        assertEquals(session.photos, moved.photos)
    }

    private fun photoList(count: Int): List<LocalPhoto> {
        return List(count) { index ->
            LocalPhoto(
                id = index.toLong(),
                contentUri = "content://photo/$index",
            )
        }
    }
}
