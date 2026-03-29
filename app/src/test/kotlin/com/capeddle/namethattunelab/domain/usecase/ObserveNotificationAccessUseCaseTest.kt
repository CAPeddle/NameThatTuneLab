package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.repository.NowPlayingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveNotificationAccessUseCaseTest {

    private val repository: NowPlayingRepository = mockk()
    private val useCase = ObserveNotificationAccessUseCase(repository)

    @Test
    fun `invoke delegates to repository observeNotificationAccess`() = runTest {
        every { repository.observeNotificationAccess() } returns flowOf(false)

        val emission = useCase().toList()

        assertEquals(listOf(false), emission)
        verify(exactly = 1) { repository.observeNotificationAccess() }
    }
}
