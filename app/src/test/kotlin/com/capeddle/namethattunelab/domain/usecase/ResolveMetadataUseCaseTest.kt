package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.MetadataConfidence
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.repository.MetadataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResolveMetadataUseCaseTest {

    private val repository: MetadataRepository = mockk()
    private val useCase = ResolveMetadataUseCase(repository)

    private val event = NowPlayingEvent(TRACK_TITLE, "John Lennon", null, "com.spotify.music")
    private val expected = TrackMetadata(TRACK_TITLE, "John Lennon", TRACK_TITLE, 1971, MetadataConfidence.HIGH)

    @Test
    fun `invoke returns success from repository`() = runTest {
        coEvery { repository.resolveMetadata(event) } returns Result.success(expected)

        val result = useCase(event)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.resolveMetadata(event) } returns Result.failure(error)

        val result = useCase(event)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `invoke delegates to repository with correct event`() = runTest {
        coEvery { repository.resolveMetadata(any()) } returns Result.success(expected)

        useCase(event)

        coVerify(exactly = 1) { repository.resolveMetadata(event) }
    }

    companion object {
        private const val TRACK_TITLE = "Imagine"
    }
}
