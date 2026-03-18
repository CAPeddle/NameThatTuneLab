package com.capeddle.namethattunelab.speech

import com.capeddle.namethattunelab.domain.model.TrackMetadata
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guards against re-announcing the same track within a short cooldown window
 * and prevents double-announcements of equivalent tracks.
 *
 * An announcement is suppressed when BOTH conditions are true:
 * 1. The [TrackMetadata.artist] + [TrackMetadata.title] pair matches the last announced track.
 * 2. The elapsed time since the last announcement is less than [COOLDOWN_MS].
 *
 * The cooldown resets whenever a *different* track is announced.
 */
@Singleton
class AnnouncementGuard @Inject constructor() {

    @Volatile private var lastAnnouncedKey: String = ""

    @Volatile private var lastAnnouncedAt: Long = 0L

    /**
     * Returns `true` if [metadata] should be announced now.
     * Side effect: records the current track and timestamp when it returns `true`.
     */
    @Synchronized
    fun shouldAnnounce(metadata: TrackMetadata): Boolean {
        val key = buildKey(metadata)
        val now = System.currentTimeMillis()

        if (key == lastAnnouncedKey && now - lastAnnouncedAt < COOLDOWN_MS) {
            return false
        }

        lastAnnouncedKey = key
        lastAnnouncedAt = now
        return true
    }

    /** Resets internal state. Useful for testing. */
    @Synchronized
    fun reset() {
        lastAnnouncedKey = ""
        lastAnnouncedAt = 0L
    }

    private fun buildKey(metadata: TrackMetadata): String =
        "${metadata.artist.trim().lowercase()}|${metadata.title.trim().lowercase()}"

    private companion object {
        /** 30 seconds — prevents repeated announcements on seek / pause+resume. */
        const val COOLDOWN_MS = 30_000L
    }
}
