package com.capeddle.namethattunelab.speech

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages audio focus for TTS announcements.
 *
 * Requests [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK] before TTS playback
 * so music continues at a reduced volume ("duck") while the announcement plays.
 *
 * Usage pattern:
 * ```
 * val granted = audioFocusManager.request()
 * if (granted) {
 *     tts.speak(...)
 *     audioFocusManager.abandon()
 * }
 * ```
 */
@Singleton
class AudioFocusManager private constructor(
    private val audioManager: AudioManager,
    private val focusRequest: AudioFocusRequest
) {

    @Inject
    constructor(
        @ApplicationContext context: Context
    ) : this(
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).build()
    )

    /**
     * Requests transient audio focus with ducking.
     *
     * @return `true` if focus was granted, `false` otherwise (caller should skip TTS).
     */
    fun request(): Boolean {
        val result = audioManager.requestAudioFocus(focusRequest)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    /**
     * Abandons audio focus after TTS playback completes.
     * Safe to call even if [request] was never called or failed.
     */
    fun abandon() {
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    companion object {
        internal fun createForTest(audioManager: AudioManager, focusRequest: AudioFocusRequest): AudioFocusManager =
            AudioFocusManager(audioManager, focusRequest)
    }
}
