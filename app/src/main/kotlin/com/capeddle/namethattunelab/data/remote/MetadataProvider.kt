package com.capeddle.namethattunelab.data.remote

import com.capeddle.namethattunelab.domain.model.TrackMetadata

/**
 * Contract for an external metadata provider that resolves release year
 * and other enriched data for a given track.
 *
 * Lives in the data layer — this is an implementation detail of the data layer's
 * resolution strategy, not part of the domain.
 *
 * Implementations:
 *  - [MusicBrainzProvider] — primary, free, no API key required
 *  - (future) DiscogsProvider, LastFmProvider
 */
interface MetadataProvider {
    /**
     * Looks up metadata for the given [artist] and [title].
     *
     * @param artist Track artist.
     * @param title  Track title.
     * @param album  Optional album hint to improve match accuracy.
     * @return [Result.success] with [TrackMetadata] if found (year may be null),
     *         [Result.failure] on network or parsing errors.
     */
    suspend fun lookup(artist: String, title: String, album: String?): Result<TrackMetadata>
}
