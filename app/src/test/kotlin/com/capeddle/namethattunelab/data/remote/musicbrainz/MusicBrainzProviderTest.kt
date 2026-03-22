package com.capeddle.namethattunelab.data.remote.musicbrainz

import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import com.capeddle.namethattunelab.util.PipelineLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MusicBrainzProviderTest {

    private val api: MusicBrainzApi = mockk()
    private val logger: PipelineLogger = mockk(relaxed = true)
    private val appSettingsRepository: AppSettingsRepository = mockk()
    private val provider = MusicBrainzProvider(api, logger, appSettingsRepository)

    private val emptyResponse = MusicBrainzRecordingSearchResponse(recordings = emptyList())

    @Test
    fun `lookup returns failure when recordings list is empty`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings())
        coEvery { api.searchRecordings(any(), any(), any(), any()) } returns emptyResponse

        val result = provider.lookup("Queen", "Bohemian Rhapsody", null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
    }

    @Test
    fun `lookup propagates API exception as failure`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings())
        val exception = IllegalStateException("Network timeout")
        coEvery { api.searchRecordings(any(), any(), any(), any()) } throws exception

        val result = provider.lookup("Queen", "Bohemian Rhapsody", null)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `lookup passes artist title and configured user agent to API`() = runTest {
        val artist = "The Beatles"
        val title = "Let It Be"
        val userAgent = "NameThatTuneLab/2.0 (owner@example.com)"
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings(musicBrainzUserAgent = userAgent))
        coEvery { api.searchRecordings(any(), any(), any(), any()) } returns emptyResponse

        provider.lookup(artist, title, title)

        coVerify {
            api.searchRecordings(
                artist = artist,
                title = title,
                userAgent = userAgent,
                limit = any()
            )
        }
    }

    @Test
    fun `lookup serializes concurrent requests with at least one second between dispatches`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings())
        val dispatchTimes = mutableListOf<Long>()
        coEvery { api.searchRecordings(any(), any(), any(), any()) } coAnswers {
            dispatchTimes += testScheduler.currentTime
            emptyResponse
        }

        val first = async { provider.lookup("Artist A", "Title A", null) }
        val second = async { provider.lookup("Artist B", "Title B", null) }
        first.await()
        second.await()

        assertEquals(2, dispatchTimes.size)
        assertTrue(dispatchTimes[1] - dispatchTimes[0] >= MIN_INTERVAL_ASSERT_MS)
    }

    @Test
    fun `lookup preserves pacing when first API call fails`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings())
        val dispatchTimes = mutableListOf<Long>()
        var callCount = 0
        coEvery { api.searchRecordings(any(), any(), any(), any()) } coAnswers {
            dispatchTimes += testScheduler.currentTime
            callCount += 1
            if (callCount == 1) {
                error("Boom")
            }
            emptyResponse
        }

        val first = async { provider.lookup("Artist A", "Title A", null) }
        val second = async { provider.lookup("Artist B", "Title B", null) }

        val firstResult = first.await()
        val secondResult = second.await()

        assertTrue(firstResult.isFailure)
        assertTrue(secondResult.isFailure)
        assertEquals(2, dispatchTimes.size)
        assertTrue(dispatchTimes[1] - dispatchTimes[0] >= MIN_INTERVAL_ASSERT_MS)
    }

    companion object {
        private const val MIN_INTERVAL_ASSERT_MS = 900L
    }
}
