package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.SpeechAnnouncer
import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AnnounceTrackUseCaseTest {

    private val announcer: SpeechAnnouncer = mockk()
    private val useCase = AnnounceTrackUseCase(announcer)

    private val metadata = TrackMetadata("Lose Yourself", "Eminem", "8 Mile", 2002, MetadataConfidence.HIGH)

    @Test
    fun `invoke returns success when announcer succeeds`() = runTest {
        coEvery { announcer.announce(metadata) } returns Result.success(Unit)

        val result = useCase(metadata)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when announcer fails`() = runTest {
        val error = IllegalStateException("TTS not ready")
        coEvery { announcer.announce(metadata) } returns Result.failure(error)

        val result = useCase(metadata)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke delegates to announcer with exact metadata`() = runTest {
        coEvery { announcer.announce(any()) } returns Result.success(Unit)

        useCase(metadata)

        coVerify(exactly = 1) { announcer.announce(metadata) }
    }
}
