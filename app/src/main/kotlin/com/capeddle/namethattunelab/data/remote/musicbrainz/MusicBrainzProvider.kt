package com.capeddle.namethattunelab.data.remote.musicbrainz

import com.capeddle.namethattunelab.data.mapper.MusicBrainzMapper
import com.capeddle.namethattunelab.data.remote.MetadataProvider
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.util.PipelineLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [MetadataProvider] implementation that queries MusicBrainz.
 *
 * **Rate limiting:** MusicBrainz policy allows at most 1 request per second from
 * authenticated clients. This class enforces that contract using a [Mutex] and a
 * [MIN_REQUEST_INTERVAL_MS] delay between releases of the mutex.
 */
@Singleton
class MusicBrainzProvider @Inject constructor(
    private val api: MusicBrainzApi,
    private val logger: PipelineLogger
) : MetadataProvider {

    private val rateLimitMutex = Mutex()

    /** Timestamp (System.currentTimeMillis) of the last request dispatch. */
    @Volatile
    private var lastRequestAt: Long = 0L

    override suspend fun lookup(artist: String, title: String, album: String?): Result<TrackMetadata> {
        // Enforce rate limit outside the main try/catch so callers still see
        // delay-related cancellations propagated correctly.
        rateLimitMutex.withLock { enforceRateLimit() }

        return runCatching {
            logger.logLookup(artist, title)
            val response = api.searchRecordings(artist = artist, title = title)
            lastRequestAt = System.currentTimeMillis()

            val metadata = MusicBrainzMapper.map(
                recordings = response.recordings,
                originalTitle = title,
                originalArtist = artist
            )

            if (metadata != null) {
                metadata
            } else {
                throw NoSuchElementException("No acceptable MusicBrainz result for '$artist - $title'")
            }
        }.onFailure { throwable ->
            if (throwable is CancellationException) throw throwable
            logger.logError("MusicBrainz", "lookup failed for '$artist - $title': ${throwable.message}", throwable)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sleeps for the remainder of the 1-second window since [lastRequestAt].
     * Must only be called while [rateLimitMutex] is held.
     */
    private suspend fun enforceRateLimit() {
        val elapsed = System.currentTimeMillis() - lastRequestAt
        if (elapsed < MIN_REQUEST_INTERVAL_MS) {
            kotlinx.coroutines.delay(MIN_REQUEST_INTERVAL_MS - elapsed)
        }
    }

    private companion object {
        const val MIN_REQUEST_INTERVAL_MS = 1_000L
    }
}
