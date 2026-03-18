package com.capeddle.namethattunelab.speech

import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnnouncementGuardTest {

    private lateinit var guard: AnnouncementGuard

    private val track = TrackMetadata("Bohemian Rhapsody", "Queen", null, BOHEMIAN_YEAR, MetadataConfidence.HIGH)

    @BeforeEach
    fun setUp() {
        guard = AnnouncementGuard()
    }

    @Test
    fun `shouldAnnounce returns true on first call`() {
        assertTrue(guard.shouldAnnounce(track))
    }

    @Test
    fun `shouldAnnounce returns false for same track within cooldown`() {
        guard.shouldAnnounce(track) // prime
        assertFalse(guard.shouldAnnounce(track))
    }

    @Test
    fun `shouldAnnounce returns true for different track immediately`() {
        val other = TrackMetadata("Hotel California", "Eagles", null, HOTEL_CALIFORNIA_YEAR, MetadataConfidence.HIGH)
        guard.shouldAnnounce(track)
        assertTrue(guard.shouldAnnounce(other))
    }

    @Test
    fun `shouldAnnounce is case-insensitive for title`() {
        guard.shouldAnnounce(track)
        val upperTitle = track.copy(title = track.title.uppercase())
        assertFalse(guard.shouldAnnounce(upperTitle))
    }

    @Test
    fun `shouldAnnounce is case-insensitive for artist`() {
        guard.shouldAnnounce(track)
        val lowerArtist = track.copy(artist = track.artist.lowercase())
        assertFalse(guard.shouldAnnounce(lowerArtist))
    }

    @Test
    fun `shouldAnnounce returns true after reset`() {
        guard.shouldAnnounce(track)
        guard.reset()
        assertTrue(guard.shouldAnnounce(track))
    }

    @Test
    fun `shouldAnnounce ignores album and year differences for dedup`() {
        guard.shouldAnnounce(track)
        // Different album/year but same artist+title = still suppressed
        val sameKey = track.copy(album = "Different Album", year = 2000)
        assertFalse(guard.shouldAnnounce(sameKey))
    }

    companion object {
        private const val BOHEMIAN_YEAR = 1975
        private const val HOTEL_CALIFORNIA_YEAR = 1977
    }
}
