package com.capeddle.namethattunelab.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.capeddle.namethattunelab.domain.model.AppSettings
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppSettingsRepositoryImplTest {

    @Test
    fun `observeSettings returns defaults when store is empty`() = runTest {
        val repository = AppSettingsRepositoryImpl(createStore())

        val settings = repository.observeSettings().first()

        assertEquals(AppSettings.DEFAULT_MUSIC_BRAINZ_USER_AGENT, settings.musicBrainzUserAgent)
        assertEquals(AppSettings.DEFAULT_VOICE_OVER_DELAY_MS, settings.voiceOverDelayMs)
    }

    @Test
    fun `updateSettings persists values`() = runTest {
        val repository = AppSettingsRepositoryImpl(createStore())
        val expected = AppSettings(
            musicBrainzUserAgent = "NameThatTuneLab/2.0 (persist@example.com)",
            voiceOverDelayMs = 2_000L
        )

        repository.updateSettings(expected)
        val actual = repository.observeSettings().first()

        assertEquals(expected.musicBrainzUserAgent, actual.musicBrainzUserAgent)
        assertEquals(expected.voiceOverDelayMs, actual.voiceOverDelayMs)
    }

    @Test
    fun `updateSettings clamps delay to max value`() = runTest {
        val repository = AppSettingsRepositoryImpl(createStore())

        repository.updateSettings(
            AppSettings(
                musicBrainzUserAgent = "NameThatTuneLab/2.0 (clamp@example.com)",
                voiceOverDelayMs = AppSettings.MAX_VOICE_OVER_DELAY_MS + 5_000L
            )
        )

        val actual = repository.observeSettings().first()
        assertEquals(AppSettings.MAX_VOICE_OVER_DELAY_MS, actual.voiceOverDelayMs)
    }

    private fun createStore() = PreferenceDataStoreFactory.create(
        produceFile = {
            File(createTempDirectory().toFile(), "app_settings.preferences_pb")
        }
    )
}
