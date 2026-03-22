package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.SpeechAnnouncer
import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnnounceTrackUseCaseTest {

    private val announcer: SpeechAnnouncer = mockk()
    private val appSettingsRepository: AppSettingsRepository = mockk()
    private val useCase = AnnounceTrackUseCase(announcer, appSettingsRepository)

    private val metadata = TrackMetadata("Lose Yourself", "Eminem", "8 Mile", 2002, MetadataConfidence.HIGH)

    @Test
    fun `invoke returns success when announcer succeeds`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings(voiceOverDelayMs = 0L))
        coEvery { announcer.announce(metadata) } returns Result.success(Unit)

        val result = useCase(metadata)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when announcer fails`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings(voiceOverDelayMs = 0L))
        val error = IllegalStateException("TTS not ready")
        coEvery { announcer.announce(metadata) } returns Result.failure(error)

        val result = useCase(metadata)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke delegates to announcer with exact metadata`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings(voiceOverDelayMs = 0L))
        coEvery { announcer.announce(any()) } returns Result.success(Unit)

        useCase(metadata)

        coVerify(exactly = 1) { announcer.announce(metadata) }
    }

    @Test
    fun `invoke waits configured delay before announcing`() = runTest {
        every { appSettingsRepository.observeSettings() } returns flowOf(AppSettings(voiceOverDelayMs = DELAY_MS))
        coEvery { announcer.announce(any()) } returns Result.success(Unit)

        val job = backgroundScope.launch {
            useCase(metadata)
        }

        runCurrent()

        advanceTimeBy(DELAY_MINUS_ONE_MS)
        coVerify(exactly = 0) { announcer.announce(any()) }

        advanceTimeBy(1L) // 1ms: exact boundary cross
        runCurrent()
        coVerify(exactly = 1) { announcer.announce(metadata) }
        job.cancel()
    }
    companion object {
        private const val DELAY_MS = 1_500L
        private const val DELAY_MINUS_ONE_MS = 1_499L
    }
}
