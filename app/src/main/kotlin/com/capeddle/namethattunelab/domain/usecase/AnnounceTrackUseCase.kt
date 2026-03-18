package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.SpeechAnnouncer
import com.capeddle.namethattunelab.domain.model.TrackMetadata
import javax.inject.Inject

/**
 * Announces a [TrackMetadata] result through text-to-speech.
 *
 * Delegates to [SpeechAnnouncer] which handles audio focus, ducking,
 * deduplication, and cooldown logic.
 */
class AnnounceTrackUseCase @Inject constructor(
    private val speechAnnouncer: SpeechAnnouncer
) {
    suspend operator fun invoke(metadata: TrackMetadata): Result<Unit> = speechAnnouncer.announce(metadata)
}
