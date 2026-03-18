package com.capeddle.namethattunelab.speech

import android.content.Context
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.util.PipelineLogger
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TtsAnnouncerTest {

    private val context: Context = mockk(relaxed = true)
    private val audioFocusManager: AudioFocusManager = mockk(relaxed = true)
    private val announcementGuard: AnnouncementGuard = mockk()
    private val logger: PipelineLogger = mockk(relaxed = true)

    private val announcer = TtsAnnouncer(context, audioFocusManager, announcementGuard, logger)

    private val metadata = TrackMetadata(
        title = "Track",
        artist = "Artist",
        album = null,
        year = 1999,
        confidence = MetadataConfidence.HIGH
    )

    @Test
    fun `announce returns success when guard suppresses announcement`() = runTest {
        every { announcementGuard.shouldAnnounce(metadata) } returns false

        val result = announcer.announce(metadata)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { audioFocusManager.request() }
        verify(exactly = 1) { logger.logSpeechSkipped(metadata.artist, metadata.title) }
    }

    @Test
    fun `shutdown does not throw when engine not initialized`() {
        assertDoesNotThrow { announcer.shutdown() }
    }
}
