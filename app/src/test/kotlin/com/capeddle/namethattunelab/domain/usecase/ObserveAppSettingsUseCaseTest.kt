package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObserveAppSettingsUseCaseTest {

    private val repository: AppSettingsRepository = mockk()
    private val useCase = ObserveAppSettingsUseCase(repository)

    @Test
    fun `invoke delegates to repository observeSettings`() = runTest {
        val expected = AppSettings(
            musicBrainzUserAgent = "NameThatTuneLab/2.0 (test@example.com)",
            voiceOverDelayMs = 3_000L
        )
        every { repository.observeSettings() } returns flowOf(expected)

        val emitted = mutableListOf<AppSettings>()
        useCase().collect { emitted += it }

        assertEquals(1, emitted.size)
        assertEquals(expected, emitted.first())
    }

    @Test
    fun `invoke emits default settings when repository emits defaults`() = runTest {
        val defaults = AppSettings()
        every { repository.observeSettings() } returns flowOf(defaults)

        val emitted = mutableListOf<AppSettings>()
        useCase().collect { emitted += it }

        assertEquals(1, emitted.size)
        assertEquals(defaults, emitted.first())
    }
}
