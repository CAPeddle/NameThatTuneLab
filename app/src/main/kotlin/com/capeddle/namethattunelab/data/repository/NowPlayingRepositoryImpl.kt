package com.capeddle.namethattunelab.data.repository

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.domain.repository.NowPlayingRepository
import com.capeddle.namethattunelab.nowplaying.MediaSessionMonitor
import com.capeddle.namethattunelab.nowplaying.TrackChangeDebouncer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Implements [NowPlayingRepository] by subscribing to [MediaSessionMonitor] events
 * and applying debouncing + deduplication via [TrackChangeDebouncer].
 */
@Singleton
class NowPlayingRepositoryImpl @Inject constructor(
    private val monitor: MediaSessionMonitor,
    private val debouncer: TrackChangeDebouncer,
    private val notificationAccessMonitor: NotificationAccessMonitor
) : NowPlayingRepository {

    override fun observeNowPlaying(): Flow<NowPlayingEvent> = debouncer.debounce(monitor.events)

    override fun observeNotificationAccess(): Flow<Boolean> = notificationAccessMonitor.observe()
}
