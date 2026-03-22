package com.capeddle.namethattunelab.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppSettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { prefs ->
            val savedDelay = prefs[VOICE_OVER_DELAY_MS] ?: AppSettings.DEFAULT_VOICE_OVER_DELAY_MS
            AppSettings(
                musicBrainzUserAgent = prefs[MUSIC_BRAINZ_USER_AGENT]
                    ?.takeIf { it.isNotBlank() }
                    ?: AppSettings.DEFAULT_MUSIC_BRAINZ_USER_AGENT,
                voiceOverDelayMs = savedDelay.coerceIn(
                    minimumValue = AppSettings.DEFAULT_VOICE_OVER_DELAY_MS,
                    maximumValue = AppSettings.MAX_VOICE_OVER_DELAY_MS
                )
            )
        }

    override suspend fun updateSettings(settings: AppSettings) {
        val normalizedDelay = settings.voiceOverDelayMs.coerceIn(
            minimumValue = AppSettings.DEFAULT_VOICE_OVER_DELAY_MS,
            maximumValue = AppSettings.MAX_VOICE_OVER_DELAY_MS
        )

        dataStore.edit { prefs ->
            prefs[MUSIC_BRAINZ_USER_AGENT] = settings.musicBrainzUserAgent
                .ifBlank { AppSettings.DEFAULT_MUSIC_BRAINZ_USER_AGENT }
            prefs[VOICE_OVER_DELAY_MS] = normalizedDelay
        }
    }

    private companion object {
        val MUSIC_BRAINZ_USER_AGENT = stringPreferencesKey("music_brainz_user_agent")
        val VOICE_OVER_DELAY_MS = longPreferencesKey("voice_over_delay_ms")
    }
}
