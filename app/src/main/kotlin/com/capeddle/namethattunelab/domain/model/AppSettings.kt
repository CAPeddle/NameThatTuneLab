package com.capeddle.namethattunelab.domain.model

/**
 * User-configurable runtime settings for app behavior.
 */
data class AppSettings(
    val musicBrainzUserAgent: String = DEFAULT_MUSIC_BRAINZ_USER_AGENT,
    val voiceOverDelayMs: Long = DEFAULT_VOICE_OVER_DELAY_MS
) {
    companion object {
        const val DEFAULT_MUSIC_BRAINZ_USER_AGENT = "NameThatTuneLab/1.0 (chrisapeddle@gmail.com)"
        const val DEFAULT_VOICE_OVER_DELAY_MS = 0L
        const val MAX_VOICE_OVER_DELAY_MS = 15_000L
    }
}
