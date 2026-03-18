package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.repository.NowPlayingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveNowPlayingUseCaseTest {

    private val repository: NowPlayingRepository = mockk()
    private val useCase = ObserveNowPlayingUseCase(repository)

    @Test
    fun `invoke delegates to repository and returns events`() = runTest {
        val event = NowPlayingEvent("Bohemian Rhapsody", "Queen", null, "com.spotify.music")
        every { repository.observeNowPlaying() } returns flowOf(event)

        val result = useCase().toList()

        assertEquals(1, result.size)
        assertEquals(event, result.first())
    }

    @Test
    fun `invoke returns empty flow when repository emits nothing`() = runTest {
        every { repository.observeNowPlaying() } returns flowOf()

        val result = useCase().toList()

        assertEquals(0, result.size)
    }
}
