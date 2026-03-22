package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.SpeechAnnouncer
import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Announces a [TrackMetadata] result through text-to-speech.
 *
 * Delegates to [SpeechAnnouncer] which handles audio focus, ducking,
 * deduplication, and cooldown logic.
 */
class AnnounceTrackUseCase @Inject constructor(
    private val speechAnnouncer: SpeechAnnouncer,
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(metadata: TrackMetadata): Result<Unit> {
        val delayMs = appSettingsRepository.observeSettings().first().voiceOverDelayMs
            .coerceAtLeast(AppSettings.DEFAULT_VOICE_OVER_DELAY_MS)
        delay(delayMs)
        return speechAnnouncer.announce(metadata)
    }
}
