package com.capeddle.namethattunelab.domain.model

/**
 * Represents a detected media playback event from an active MediaSession.
 *
 * @property title      Track title as reported by the media session.
 * @property artist     Artist name as reported by the media session.
 * @property album      Album name, if available.
 * @property sourceApp  Package name of the app producing the media session (e.g. "com.spotify.music").
 */
data class NowPlayingEvent(
    val title: String,
    val artist: String,
    val album: String?,
    val sourceApp: String
)
