package com.capeddle.namethattunelab.data.mapper

import com.capeddle.namethattunelab.data.local.MetadataCacheEntity
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata

/**
 * Bidirectional mapper between [MetadataCacheEntity] (Room) and
 * [TrackMetadata] (domain).
 *
 * Key normalisation (lower-case + trim) is applied here so persistence
 * details stay out of domain and use-case code.
 */
object MetadataCacheMapper {

    // ─────────────────────────────────────────────────────────────────────────
    // Domain → Entity
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Converts a [TrackMetadata] domain model to a [MetadataCacheEntity].
     * [cachedAt] defaults to the current system time.
     */
    fun toEntity(metadata: TrackMetadata, cachedAt: Long = System.currentTimeMillis()): MetadataCacheEntity =
        MetadataCacheEntity(
            artistKey = metadata.artist.normalise(),
            titleKey = metadata.title.normalise(),
            title = metadata.title,
            artist = metadata.artist,
            album = metadata.album,
            year = metadata.year,
            confidence = metadata.confidence.name,
            cachedAt = cachedAt
        )

    // ─────────────────────────────────────────────────────────────────────────
    // Entity → Domain
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Converts a [MetadataCacheEntity] back to a [TrackMetadata] domain model.
     * Unknown [MetadataConfidence] names fall back to [MetadataConfidence.NONE].
     */
    fun toDomain(entity: MetadataCacheEntity): TrackMetadata = TrackMetadata(
        title = entity.title,
        artist = entity.artist,
        album = entity.album,
        year = entity.year,
        confidence = entity.confidence.toConfidence()
    )

    // ─────────────────────────────────────────────────────────────────────────
    // Lookup key helpers (called by repository, exposed as top-level extensions)
    // ─────────────────────────────────────────────────────────────────────────

    fun String.normalise(): String = this.trim().lowercase()

    private fun String.toConfidence(): MetadataConfidence =
        MetadataConfidence.entries.firstOrNull { it.name == this } ?: MetadataConfidence.NONE
}
