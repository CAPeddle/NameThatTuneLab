package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateAppSettingsUseCaseTest {

    private val repository: AppSettingsRepository = mockk()
    private val useCase = UpdateAppSettingsUseCase(repository)

    @Test
    fun `invoke delegates settings to repository updateSettings`() = runTest {
        val settings = AppSettings(
            musicBrainzUserAgent = "NameThatTuneLab/2.0 (test@example.com)",
            voiceOverDelayMs = 5_000L
        )
        coJustRun { repository.updateSettings(settings) }

        useCase(settings)

        coVerify(exactly = 1) { repository.updateSettings(settings) }
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        val settings = AppSettings()
        coEvery { repository.updateSettings(any()) } throws IOException("disk full")

        assertThrows<IOException> {
            useCase(settings)
        }
    }
}
