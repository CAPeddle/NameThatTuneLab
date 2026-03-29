package com.capeddle.namethattunelab.data.repository

import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.nowplaying.MediaSessionMonitor
import com.capeddle.namethattunelab.nowplaying.TrackChangeDebouncer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class NowPlayingRepositoryImplTest {

    private val monitor: MediaSessionMonitor = mockk()
    private val debouncer: TrackChangeDebouncer = mockk()
    private val notificationAccessMonitor: NotificationAccessMonitor = mockk()

    private val repository = NowPlayingRepositoryImpl(monitor, debouncer, notificationAccessMonitor)

    @Test
    fun `observeNowPlaying delegates monitor events through debouncer`() {
        val monitorEvents = MutableSharedFlow<NowPlayingEvent>()
        val expectedFlow = flowOf(TEST_EVENT)

        every { monitor.events } returns monitorEvents
        every { debouncer.debounce(monitorEvents) } returns expectedFlow

        val result = repository.observeNowPlaying()

        assertSame(expectedFlow, result)
        verify(exactly = 1) { debouncer.debounce(monitorEvents) }
    }

    @Test
    fun `observeNotificationAccess delegates to notification access monitor`() {
        val expectedFlow = flowOf(true)
        every { notificationAccessMonitor.observe() } returns expectedFlow

        val result = repository.observeNotificationAccess()

        assertSame(expectedFlow, result)
        verify(exactly = 1) { notificationAccessMonitor.observe() }
    }

    companion object {
        private val TEST_EVENT = NowPlayingEvent(
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            sourceApp = "com.spotify.music"
        )
    }
}
