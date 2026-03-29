package com.capeddle.namethattunelab.domain.repository

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import kotlinx.coroutines.flow.Flow

/**
 * Provides a stream of [NowPlayingEvent]s as the active media session changes.
 *
 * Pure Kotlin interface — no Android framework imports.
 */
interface NowPlayingRepository {
    /**
     * Returns a [Flow] that emits a [NowPlayingEvent] each time the playing track changes.
     * The flow is debounced and deduplicated — consumers will not see rapid duplicate events.
     */
    fun observeNowPlaying(): Flow<NowPlayingEvent>

    /**
     * Returns whether notification-listener access is currently granted for this app.
     */
    fun observeNotificationAccess(): Flow<Boolean>
}
