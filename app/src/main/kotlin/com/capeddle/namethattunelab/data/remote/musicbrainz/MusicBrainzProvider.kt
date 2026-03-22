package com.capeddle.namethattunelab.data.remote.musicbrainz

import com.capeddle.namethattunelab.data.mapper.MusicBrainzMapper
import com.capeddle.namethattunelab.data.remote.MetadataProvider
import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import com.capeddle.namethattunelab.util.PipelineLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [MetadataProvider] implementation that queries MusicBrainz.
 *
 * **Rate limiting:** MusicBrainz policy allows at most 1 request per second from
 * authenticated clients. This class enforces that contract using a [Mutex] and a
 * [MIN_REQUEST_INTERVAL_MS] delay between request dispatches.
 */
@Singleton
class MusicBrainzProvider @Inject constructor(
    private val api: MusicBrainzApi,
    private val logger: PipelineLogger,
    private val appSettingsRepository: AppSettingsRepository
) : MetadataProvider {

    private val rateLimitMutex = Mutex()

    /** Timestamp (System.currentTimeMillis) of the last request dispatch. */
    @Volatile
    private var lastRequestAt: Long = 0L

    override suspend fun lookup(artist: String, title: String, album: String?): Result<TrackMetadata> {
        return runCatching {
            reserveRequestSlot()

            logger.logLookup(artist, title)
            val userAgent = appSettingsRepository.observeSettings().first().musicBrainzUserAgent
                .ifBlank { AppSettings.DEFAULT_MUSIC_BRAINZ_USER_AGENT }

            val response = api.searchRecordings(artist = artist, title = title, userAgent = userAgent)

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
     * Reserves the next dispatch slot, ensuring at least one second between
     * request start times across concurrent callers.
     */
    private suspend fun reserveRequestSlot() {
        rateLimitMutex.withLock {
            val elapsed = System.currentTimeMillis() - lastRequestAt
            if (elapsed < MIN_REQUEST_INTERVAL_MS) {
                kotlinx.coroutines.delay(MIN_REQUEST_INTERVAL_MS - elapsed)
            }

            // Mark dispatch time before issuing the request so pacing is
            // respected even when the API call fails.
            lastRequestAt = System.currentTimeMillis()
        }
    }

    private companion object {
        const val MIN_REQUEST_INTERVAL_MS = 1_000L
    }
}
