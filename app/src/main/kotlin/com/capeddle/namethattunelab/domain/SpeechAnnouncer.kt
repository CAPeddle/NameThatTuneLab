package com.capeddle.namethattunelab.domain

import com.capeddle.namethattunelab.domain.model.TrackMetadata

/**
 * Speaks a [TrackMetadata] result using text-to-speech.
 *
 * This interface lives in the domain layer so [AnnounceTrackUseCase] can depend on it
 * without importing Android framework classes.
 *
 * The implementation ([TtsAnnouncer]) lives in the speech package.
 */
interface SpeechAnnouncer {
    /**
     * Announces the track. Suspends until speech completes or is interrupted.
     *
     * @return [Result.success] when speech completed. [Result.failure] if TTS engine
     *         is unavailable or audio focus could not be obtained.
     */
    suspend fun announce(metadata: TrackMetadata): Result<Unit>

    /** Releases TTS engine resources. Call when the owning component is destroyed. */
    fun shutdown()
}
