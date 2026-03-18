package com.capeddle.namethattunelab.nowplaying

import com.capeddle.namethattunelab.util.PipelineLogger
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NowPlayingListenerServiceTest {

    @Test
    fun `onListenerConnected executes with Robolectric service lifecycle`() {
        val monitor: MediaSessionMonitor = mockk(relaxed = true)
        val logger: PipelineLogger = mockk(relaxed = true)
        val service = Robolectric.buildService(NowPlayingListenerService::class.java).create().get()
        service.monitor = monitor
        service.logger = logger

        service.onListenerConnected()
    }

    @Test
    fun `onListenerDisconnected detaches monitor`() {
        val monitor: MediaSessionMonitor = mockk(relaxed = true)
        val logger: PipelineLogger = mockk(relaxed = true)
        val service = Robolectric.buildService(NowPlayingListenerService::class.java).create().get()
        service.monitor = monitor
        service.logger = logger

        service.onListenerDisconnected()
        verify(exactly = 1) { monitor.detachAll() }
    }

    @Test
    fun `onNotificationPosted accepts null after lifecycle create`() {
        val monitor: MediaSessionMonitor = mockk(relaxed = true)
        val logger: PipelineLogger = mockk(relaxed = true)
        val service = Robolectric.buildService(NowPlayingListenerService::class.java).create().get()
        service.monitor = monitor
        service.logger = logger

        service.onNotificationPosted(null)
    }
}
