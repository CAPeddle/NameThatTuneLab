package com.capeddle.namethattunelab.domain.repository

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata

/**
 * Resolves enriched [TrackMetadata] for a detected [NowPlayingEvent].
 *
 * Implementations use a cache-first strategy: check local cache, then fall back
 * to an external provider (MusicBrainz).
 *
 * Pure Kotlin interface — no Android framework imports.
 */
interface MetadataRepository {
    /**
     * Resolves metadata for the given [event].
     *
     * @return [Result.success] with [TrackMetadata] on success (year may be null if lookup
     *         found no match). [Result.failure] only if the entire lookup pipeline failed.
     */
    suspend fun resolveMetadata(event: NowPlayingEvent): Result<TrackMetadata>
}
