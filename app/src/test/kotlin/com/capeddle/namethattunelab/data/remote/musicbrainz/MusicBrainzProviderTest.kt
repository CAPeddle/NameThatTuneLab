package com.capeddle.namethattunelab.data.remote.musicbrainz

import com.capeddle.namethattunelab.util.PipelineLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MusicBrainzProviderTest {

    private val api: MusicBrainzApi = mockk()
    private val logger: PipelineLogger = mockk(relaxed = true)
    private val provider = MusicBrainzProvider(api, logger)

    private val emptyResponse = MusicBrainzRecordingSearchResponse(recordings = emptyList())

    @Test
    fun `lookup returns failure when recordings list is empty`() = runTest {
        coEvery { api.searchRecordings(any(), any(), any()) } returns emptyResponse

        val result = provider.lookup("Queen", "Bohemian Rhapsody", null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `lookup propagates API exception as failure`() = runTest {
        val exception = RuntimeException("Network timeout")
        coEvery { api.searchRecordings(any(), any(), any()) } throws exception

        val result = provider.lookup("Queen", "Bohemian Rhapsody", null)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `lookup passes artist and title to API`() = runTest {
        val artist = "The Beatles"
        val title = "Let It Be"
        coEvery { api.searchRecordings(any(), any(), any()) } returns emptyResponse

        provider.lookup(artist, title, title)

        coVerify { api.searchRecordings(artist = artist, title = title, limit = any()) }
    }
}
