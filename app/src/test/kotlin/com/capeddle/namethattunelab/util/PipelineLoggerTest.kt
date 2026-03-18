package com.capeddle.namethattunelab.util

import android.util.Log
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PipelineLoggerTest {

    private val logger = PipelineLogger()

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns LOG_RETURN_CODE
        every { Log.w(any<String>(), any<String>()) } returns LOG_RETURN_CODE
        every {
            Log.e(any<String>(), any<String>(), any<Throwable>())
        } returns LOG_RETURN_CODE
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `logDetection writes now playing tag`() {
        logger.logDetection(TEST_EVENT)

        verify(exactly = 1) {
            Log.d(
                PipelineLogger.TAG_NOW_PLAYING,
                "Detected: \"${TEST_EVENT.title}\" by ${TEST_EVENT.artist} (source: ${TEST_EVENT.sourceApp})"
            )
        }
    }

    @Test
    fun `logLookup writes metadata lookup message`() {
        logger.logLookup(TEST_ARTIST, TEST_TITLE)

        verify(exactly = 1) {
            Log.d(PipelineLogger.TAG_METADATA, "Looking up: \"$TEST_TITLE\" by $TEST_ARTIST")
        }
    }

    @Test
    fun `logLookupResult writes success message when result succeeds`() {
        logger.logLookupResult(TEST_ARTIST, TEST_TITLE, Result.success(TEST_METADATA))

        verify(exactly = 1) {
            Log.d(
                PipelineLogger.TAG_METADATA,
                "Lookup OK: \"${TEST_METADATA.title}\" by ${TEST_METADATA.artist}" +
                    " — year=${TEST_METADATA.year} confidence=${TEST_METADATA.confidence}"
            )
        }
    }

    @Test
    fun `logLookupResult writes warning message when result fails`() {
        val error = RuntimeException(TEST_FAILURE_MESSAGE)

        logger.logLookupResult(TEST_ARTIST, TEST_TITLE, Result.failure(error))

        verify(exactly = 1) {
            Log.w(
                PipelineLogger.TAG_METADATA,
                "Lookup FAILED for \"$TEST_TITLE\" by $TEST_ARTIST: $TEST_FAILURE_MESSAGE"
            )
        }
    }

    @Test
    fun `logCacheHit writes cache hit message`() {
        logger.logCacheHit(TEST_ARTIST, TEST_TITLE)

        verify(exactly = 1) {
            Log.d(PipelineLogger.TAG_CACHE, "Cache HIT: \"$TEST_TITLE\" by $TEST_ARTIST")
        }
    }

    @Test
    fun `logCacheMiss writes cache miss message`() {
        logger.logCacheMiss(TEST_ARTIST, TEST_TITLE)

        verify(exactly = 1) {
            Log.d(PipelineLogger.TAG_CACHE, "Cache MISS: \"$TEST_TITLE\" by $TEST_ARTIST — fetching from provider")
        }
    }

    companion object {
        private const val LOG_RETURN_CODE = 0
        private const val TEST_TITLE = "Bohemian Rhapsody"
        private const val TEST_ARTIST = "Queen"
        private const val TEST_FAILURE_MESSAGE = "network timeout"

        private val TEST_EVENT = NowPlayingEvent(
            title = TEST_TITLE,
            artist = TEST_ARTIST,
            album = "A Night at the Opera",
            sourceApp = "com.spotify.music"
        )

        private val TEST_METADATA = TrackMetadata(
            title = TEST_TITLE,
            artist = TEST_ARTIST,
            album = "A Night at the Opera",
            year = 1975,
            confidence = MetadataConfidence.HIGH
        )
    }
}
