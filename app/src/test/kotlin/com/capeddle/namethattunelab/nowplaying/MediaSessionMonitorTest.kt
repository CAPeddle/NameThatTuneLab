package com.capeddle.namethattunelab.nowplaying

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import app.cash.turbine.test
import com.capeddle.namethattunelab.util.PipelineLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaSessionMonitorTest {

    private val logger: PipelineLogger = mockk(relaxed = true)
    private val dispatcher = StandardTestDispatcher()

    private val monitor = MediaSessionMonitor(logger, dispatcher)

    @Test
    fun `attach registers callback once per package`() {
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        monitor.attach(controller, PACKAGE_SPOTIFY)
        monitor.attach(controller, PACKAGE_SPOTIFY)

        verify(exactly = 1) { controller.registerCallback(any()) }
    }

    @Test
    fun `detach unregisters callback when attached`() {
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        monitor.attach(controller, PACKAGE_SPOTIFY)
        monitor.detach(PACKAGE_SPOTIFY)

        verify(exactly = 1) { controller.unregisterCallback(any()) }
    }

    @Test
    fun `detachStalePackages unregisters non-active controllers`() {
        val stale: MediaController = mockk(relaxed = true)
        val active: MediaController = mockk(relaxed = true)
        every { stale.packageName } returns PACKAGE_STALE
        every { active.packageName } returns PACKAGE_ACTIVE

        monitor.attach(stale, PACKAGE_STALE)
        monitor.attach(active, PACKAGE_ACTIVE)

        monitor.detachStalePackages(setOf(PACKAGE_ACTIVE))

        verify(exactly = 1) { stale.unregisterCallback(any()) }
    }

    @Test
    fun `detachAll unregisters every attached controller`() {
        val c1: MediaController = mockk(relaxed = true)
        val c2: MediaController = mockk(relaxed = true)
        every { c1.packageName } returns PACKAGE_ONE
        every { c2.packageName } returns PACKAGE_TWO

        monitor.attach(c1, PACKAGE_ONE)
        monitor.attach(c2, PACKAGE_TWO)

        monitor.detachAll()

        verify(exactly = 1) { c1.unregisterCallback(any()) }
        verify(exactly = 1) { c2.unregisterCallback(any()) }
    }

    @Test
    fun `emits events after detachAll and reattach`() = runTest {
        val reconnectMonitor = MediaSessionMonitor(logger, StandardTestDispatcher(testScheduler))
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        val metadata: MediaMetadata = mockk(relaxed = true)
        every { metadata.getString(MediaMetadata.METADATA_KEY_TITLE) } returns "Track"
        every { metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) } returns "Artist"
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) } returns null
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) } returns "Album"
        every { controller.metadata } returns metadata

        val playback: PlaybackState = mockk(relaxed = true)
        every { playback.state } returns PlaybackState.STATE_PLAYING

        val callbackSlot = slot<MediaController.Callback>()
        every { controller.registerCallback(capture(callbackSlot)) } answers {}

        reconnectMonitor.attach(controller, PACKAGE_SPOTIFY)
        reconnectMonitor.detachAll()
        reconnectMonitor.attach(controller, PACKAGE_SPOTIFY)

        reconnectMonitor.events.test {
            callbackSlot.captured.onPlaybackStateChanged(playback)
            advanceUntilIdle()

            val event = awaitItem()
            assertEquals("Track", event.title)
            assertEquals("Artist", event.artist)
            assertEquals(PACKAGE_SPOTIFY, event.sourceApp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private const val PACKAGE_SPOTIFY = "com.spotify.music"
        private const val PACKAGE_STALE = "com.stale"
        private const val PACKAGE_ACTIVE = "com.active"
        private const val PACKAGE_ONE = "com.one"
        private const val PACKAGE_TWO = "com.two"
    }
}
