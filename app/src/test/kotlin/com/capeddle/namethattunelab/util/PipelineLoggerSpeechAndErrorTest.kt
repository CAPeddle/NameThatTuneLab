package com.capeddle.namethattunelab.util

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PipelineLoggerSpeechAndErrorTest {

    private val logger = PipelineLogger()

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns LOG_RETURN_CODE
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns LOG_RETURN_CODE
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `logSpeech writes speech message`() {
        logger.logSpeech(TEST_SPEECH)

        verify(exactly = 1) {
            Log.d(PipelineLogger.TAG_SPEECH, "Speaking: \"$TEST_SPEECH\"")
        }
    }

    @Test
    fun `logSpeechSkipped writes skip message`() {
        logger.logSpeechSkipped(TEST_ARTIST, TEST_TITLE)

        verify(exactly = 1) {
            Log.d(PipelineLogger.TAG_SPEECH, "Skipped (cooldown): \"$TEST_TITLE\" by $TEST_ARTIST")
        }
    }

    @Test
    fun `logError writes error log with throwable`() {
        val error = IllegalStateException(TEST_FAILURE_MESSAGE)

        logger.logError(PipelineLogger.TAG_METADATA, TEST_FAILURE_MESSAGE, error)

        verify(exactly = 1) {
            Log.e(PipelineLogger.TAG_METADATA, TEST_FAILURE_MESSAGE, error)
        }
    }

    companion object {
        private const val LOG_RETURN_CODE = 0
        private const val TEST_TITLE = "Bohemian Rhapsody"
        private const val TEST_ARTIST = "Queen"
        private const val TEST_SPEECH = "Bohemian Rhapsody — Queen — 1975"
        private const val TEST_FAILURE_MESSAGE = "network timeout"
    }
}
