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
    private val allowlistPolicy = MediaPackageAllowlistPolicy(setOf(PACKAGE_SPOTIFY), normalize = false)

    private val monitor = MediaSessionMonitor(logger, dispatcher, allowlistPolicy)

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
        val reconnectMonitor = MediaSessionMonitor(
            logger,
            StandardTestDispatcher(testScheduler),
            allowlistPolicy
        )
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        val metadata: MediaMetadata = mockk(relaxed = true)
        every { metadata.getString(MediaMetadata.METADATA_KEY_TITLE) } returns TRACK_TITLE
        every { metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) } returns TRACK_ARTIST
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) } returns null
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) } returns TRACK_ALBUM
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
            assertEquals(TRACK_TITLE, event.title)
            assertEquals(TRACK_ARTIST, event.artist)
            assertEquals(PACKAGE_SPOTIFY, event.sourceApp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should not emit now playing event when package is not allowlisted`() = runTest {
        val strictMonitor = MediaSessionMonitor(
            logger,
            StandardTestDispatcher(testScheduler),
            allowlistPolicy
        )
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_NON_ALLOWLISTED

        val metadata: MediaMetadata = mockk(relaxed = true)
        every { metadata.getString(MediaMetadata.METADATA_KEY_TITLE) } returns TRACK_TITLE
        every { metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) } returns TRACK_ARTIST
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) } returns null
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) } returns TRACK_ALBUM
        every { controller.metadata } returns metadata

        val playback: PlaybackState = mockk(relaxed = true)
        every { playback.state } returns PlaybackState.STATE_PLAYING

        val callbackSlot = slot<MediaController.Callback>()
        every { controller.registerCallback(capture(callbackSlot)) } answers {}

        strictMonitor.attach(controller, PACKAGE_NON_ALLOWLISTED)

        strictMonitor.events.test {
            callbackSlot.captured.onPlaybackStateChanged(playback)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            logger.logIgnoredCandidate(
                PipelineLogger.IgnoredCandidateReason.PACKAGE_NOT_ALLOWLISTED,
                PACKAGE_NON_ALLOWLISTED,
                emptyMap()
            )
        }
    }

    @Test
    fun `should not emit now playing event when playback state is not playing`() = runTest {
        val strictMonitor = MediaSessionMonitor(
            logger,
            StandardTestDispatcher(testScheduler),
            allowlistPolicy
        )
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        val metadata: MediaMetadata = mockk(relaxed = true)
        every { metadata.getString(MediaMetadata.METADATA_KEY_TITLE) } returns TRACK_TITLE
        every { metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) } returns TRACK_ARTIST
        every { controller.metadata } returns metadata

        val playback: PlaybackState = mockk(relaxed = true)
        every { playback.state } returns PlaybackState.STATE_PAUSED

        val callbackSlot = slot<MediaController.Callback>()
        every { controller.registerCallback(capture(callbackSlot)) } answers {}

        strictMonitor.attach(controller, PACKAGE_SPOTIFY)

        strictMonitor.events.test {
            callbackSlot.captured.onPlaybackStateChanged(playback)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            logger.logIgnoredCandidate(
                PipelineLogger.IgnoredCandidateReason.PLAYBACK_NOT_PLAYING,
                PACKAGE_SPOTIFY,
                mapOf("state" to PlaybackState.STATE_PAUSED.toString())
            )
        }
    }

    @Test
    fun `should not emit now playing event when playback is playing and metadata is null`() = runTest {
        val strictMonitor = MediaSessionMonitor(
            logger,
            StandardTestDispatcher(testScheduler),
            allowlistPolicy
        )
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY
        every { controller.metadata } returns null

        val playback: PlaybackState = mockk(relaxed = true)
        every { playback.state } returns PlaybackState.STATE_PLAYING

        val callbackSlot = slot<MediaController.Callback>()
        every { controller.registerCallback(capture(callbackSlot)) } answers {}

        strictMonitor.attach(controller, PACKAGE_SPOTIFY)

        strictMonitor.events.test {
            callbackSlot.captured.onPlaybackStateChanged(playback)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            logger.logIgnoredCandidate(
                PipelineLogger.IgnoredCandidateReason.METADATA_MISSING_REQUIRED_FIELDS,
                PACKAGE_SPOTIFY,
                mapOf(KEY_MISSING to "title,artist")
            )
        }
    }

    @Test
    fun `should not emit now playing event when required metadata is missing`() = runTest {
        val strictMonitor = MediaSessionMonitor(
            logger,
            StandardTestDispatcher(testScheduler),
            allowlistPolicy
        )
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        val metadata: MediaMetadata = mockk(relaxed = true)
        every { metadata.getString(MediaMetadata.METADATA_KEY_TITLE) } returns null
        every { metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) } returns TRACK_ARTIST
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) } returns null
        every { controller.metadata } returns metadata

        val playback: PlaybackState = mockk(relaxed = true)
        every { playback.state } returns PlaybackState.STATE_PLAYING

        val callbackSlot = slot<MediaController.Callback>()
        every { controller.registerCallback(capture(callbackSlot)) } answers {}

        strictMonitor.attach(controller, PACKAGE_SPOTIFY)

        strictMonitor.events.test {
            callbackSlot.captured.onPlaybackStateChanged(playback)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            logger.logIgnoredCandidate(
                PipelineLogger.IgnoredCandidateReason.METADATA_MISSING_REQUIRED_FIELDS,
                PACKAGE_SPOTIFY,
                mapOf(KEY_MISSING to "title")
            )
        }
    }

    @Test
    fun `should record telemetry reason when candidate is ignored`() = runTest {
        val strictMonitor = MediaSessionMonitor(
            logger,
            StandardTestDispatcher(testScheduler),
            allowlistPolicy
        )
        val controller: MediaController = mockk(relaxed = true)
        every { controller.packageName } returns PACKAGE_SPOTIFY

        val metadata: MediaMetadata = mockk(relaxed = true)
        every { metadata.getString(MediaMetadata.METADATA_KEY_TITLE) } returns TRACK_TITLE
        every { metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) } returns ""
        every { metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) } returns null
        every { controller.metadata } returns metadata

        val playback: PlaybackState = mockk(relaxed = true)
        every { playback.state } returns PlaybackState.STATE_PLAYING

        val callbackSlot = slot<MediaController.Callback>()
        every { controller.registerCallback(capture(callbackSlot)) } answers {}

        strictMonitor.attach(controller, PACKAGE_SPOTIFY)

        strictMonitor.events.test {
            callbackSlot.captured.onPlaybackStateChanged(playback)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            logger.logIgnoredCandidate(
                PipelineLogger.IgnoredCandidateReason.METADATA_MISSING_REQUIRED_FIELDS,
                PACKAGE_SPOTIFY,
                mapOf(KEY_MISSING to "artist")
            )
        }
    }

    companion object {
        private const val PACKAGE_SPOTIFY = "com.spotify.music"
        private const val PACKAGE_NON_ALLOWLISTED = "com.example.nonmedia"
        private const val PACKAGE_STALE = "com.stale"
        private const val PACKAGE_ACTIVE = "com.active"
        private const val PACKAGE_ONE = "com.one"
        private const val PACKAGE_TWO = "com.two"
        private const val KEY_MISSING = "missing"
        private const val TRACK_TITLE = "Track"
        private const val TRACK_ARTIST = "Artist"
        private const val TRACK_ALBUM = "Album"
    }
}
