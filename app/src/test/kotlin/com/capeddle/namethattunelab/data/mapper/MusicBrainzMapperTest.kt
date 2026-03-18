package com.capeddle.namethattunelab.data.mapper

import com.capeddle.namethattunelab.data.remote.musicbrainz.MbArtist
import com.capeddle.namethattunelab.data.remote.musicbrainz.MbArtistCredit
import com.capeddle.namethattunelab.data.remote.musicbrainz.MbRecording
import com.capeddle.namethattunelab.data.remote.musicbrainz.MbRelease
import com.capeddle.namethattunelab.data.remote.musicbrainz.MbReleaseGroup
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

// ─────────────────────────────────────────────────────────────────────────────
// Shared test fixtures (file-level to avoid duplication across nested classes)
// ─────────────────────────────────────────────────────────────────────────────

private const val TITLE = "Bohemian Rhapsody"
private const val ARTIST = "Queen"
private const val RELEASE_YEAR = 1975
private const val FALLBACK_TITLE = "title"
private const val FALLBACK_ARTIST = "artist"

private fun recording(
    title: String = TITLE,
    score: Int = 100,
    artistName: String = ARTIST,
    releases: List<MbRelease> = emptyList()
) = MbRecording(
    id = "id-1",
    title = title,
    score = score,
    artistCredit = listOf(
        MbArtistCredit(name = artistName, artist = MbArtist("artist-id", artistName, artistName))
    ),
    releases = releases
)

private fun officialRelease(date: String?, albumTitle: String = "A Night at the Opera") = MbRelease(
    id = "r-1",
    title = albumTitle,
    date = date,
    status = "Official",
    releaseGroup = MbReleaseGroup(primaryType = "Album")
)

// ─────────────────────────────────────────────────────────────────────────────
// Year extraction & confidence tests
// ─────────────────────────────────────────────────────────────────────────────

class MusicBrainzMapperYearTest {

    @Test
    fun `map returns null for empty recordings`() {
        val result = MusicBrainzMapper.map(emptyList(), FALLBACK_TITLE, FALLBACK_ARTIST)
        assertNull(result)
    }

    @Test
    fun `map returns null when top score is below threshold`() {
        val result = MusicBrainzMapper.map(listOf(recording(score = 50)), FALLBACK_TITLE, FALLBACK_ARTIST)
        assertNull(result)
    }

    @Test
    fun `map extracts year from YYYY-MM-DD date`() {
        val releases = listOf(officialRelease(date = "1975-11-21"))
        val result = MusicBrainzMapper.map(listOf(recording(releases = releases)), TITLE, ARTIST)

        assertNotNull(result)
        assertEquals(RELEASE_YEAR, result!!.year)
    }

    @Test
    fun `map extracts year from YYYY-MM date`() {
        val releases = listOf(officialRelease(date = "1975-11"))
        val result = MusicBrainzMapper.map(listOf(recording(releases = releases)), TITLE, ARTIST)

        assertNotNull(result)
        assertEquals(RELEASE_YEAR, result!!.year)
    }

    @Test
    fun `map extracts year from YYYY date`() {
        val releases = listOf(officialRelease(date = "1975"))
        val result = MusicBrainzMapper.map(listOf(recording(releases = releases)), TITLE, ARTIST)

        assertNotNull(result)
        assertEquals(RELEASE_YEAR, result!!.year)
    }

    @Test
    fun `map assigns HIGH confidence for official release with date`() {
        val releases = listOf(officialRelease(date = "1975-11-21"))
        val result = MusicBrainzMapper.map(listOf(recording(releases = releases)), TITLE, ARTIST)

        assertEquals(MetadataConfidence.HIGH, result!!.confidence)
    }

    @Test
    fun `map assigns MEDIUM confidence for non-official release with date`() {
        val nonOfficialRelease = MbRelease(
            id = "r-2",
            title = "Some Release",
            date = "1975",
            status = "Promotion",
            releaseGroup = null
        )
        val result = MusicBrainzMapper.map(
            listOf(recording(releases = listOf(nonOfficialRelease))),
            FALLBACK_TITLE,
            FALLBACK_ARTIST
        )

        assertEquals(MetadataConfidence.MEDIUM, result!!.confidence)
    }

    @Test
    fun `map assigns LOW confidence when no dates available`() {
        val releases = listOf(officialRelease(date = null))
        val result = MusicBrainzMapper.map(listOf(recording(releases = releases)), FALLBACK_TITLE, FALLBACK_ARTIST)

        assertEquals(MetadataConfidence.LOW, result!!.confidence)
        assertNull(result.year)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Artist resolution & date-ordering tests
// ─────────────────────────────────────────────────────────────────────────────

class MusicBrainzMapperArtistTest {

    @Test
    fun `map uses artist from artist-credit name field`() {
        val result = MusicBrainzMapper.map(
            listOf(recording(artistName = "The Beatles")),
            "Let It Be",
            "Beatles"
        )

        assertEquals("The Beatles", result!!.artist)
    }

    @Test
    fun `map falls back to original artist when artist-credit is empty`() {
        val rec = MbRecording(id = "x", title = "Song", score = 80, artistCredit = emptyList())
        val result = MusicBrainzMapper.map(listOf(rec), "Song", "Fallback Artist")

        assertEquals("Fallback Artist", result!!.artist)
    }

    @Test
    fun `map prefers earliest official release date`() {
        val releases = listOf(
            officialRelease(date = "1980"),
            officialRelease(date = "1975"),
            officialRelease(date = "1990")
        )
        val result = MusicBrainzMapper.map(listOf(recording(releases = releases)), FALLBACK_TITLE, FALLBACK_ARTIST)

        assertEquals(RELEASE_YEAR, result!!.year)
    }
}
