package com.capeddle.namethattunelab.data.mapper

import com.capeddle.namethattunelab.data.local.MetadataCacheEntity
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MetadataCacheMapperTest {

    private val metadata = TrackMetadata(
        title = TRACK_TITLE,
        artist = "John Lennon",
        album = TRACK_TITLE,
        year = 1971,
        confidence = MetadataConfidence.HIGH
    )

    // ─────────────────────────────────────────────────────────────────────────
    // toEntity
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `toEntity normalises keys to lowercase trimmed`() {
        val entity = MetadataCacheMapper.toEntity(metadata)

        assertEquals("john lennon", entity.artistKey)
        assertEquals("imagine", entity.titleKey)
    }

    @Test
    fun `toEntity preserves original title and artist in data fields`() {
        val entity = MetadataCacheMapper.toEntity(metadata)

        assertEquals("Imagine", entity.title)
        assertEquals("John Lennon", entity.artist)
    }

    @Test
    fun `toEntity stores confidence as enum name`() {
        val entity = MetadataCacheMapper.toEntity(metadata)
        assertEquals("HIGH", entity.confidence)
    }

    @Test
    fun `toEntity uses provided cachedAt timestamp`() {
        val timestamp = CACHE_TIMESTAMP
        val entity = MetadataCacheMapper.toEntity(metadata, cachedAt = timestamp)
        assertEquals(timestamp, entity.cachedAt)
    }

    @Test
    fun `toEntity handles null album`() {
        val entity = MetadataCacheMapper.toEntity(metadata.copy(album = null))
        assertNull(entity.album)
    }

    @Test
    fun `toEntity handles null year`() {
        val entity = MetadataCacheMapper.toEntity(metadata.copy(year = null))
        assertNull(entity.year)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // toDomain
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `toDomain round-trips cleanly`() {
        val entity = MetadataCacheMapper.toEntity(metadata)
        val result = MetadataCacheMapper.toDomain(entity)

        assertEquals(metadata, result)
    }

    @Test
    fun `toDomain falls back to NONE for unknown confidence string`() {
        val entity = MetadataCacheEntity(
            artistKey = "artist",
            titleKey = "title",
            title = "Title",
            artist = "Artist",
            album = null,
            year = null,
            confidence = "UNKNOWN_FUTURE_VALUE",
            cachedAt = 0L
        )

        val result = MetadataCacheMapper.toDomain(entity)

        assertEquals(MetadataConfidence.NONE, result.confidence)
    }

    companion object {
        private const val TRACK_TITLE = "Imagine"
        private const val CACHE_TIMESTAMP = 123_456_789L
    }
}
