package com.capeddle.namethattunelab.nowplaying

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import com.capeddle.namethattunelab.domain.model.NowPlayingEvent
import com.capeddle.namethattunelab.util.PipelineLogger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Monitors [MediaController] instances for metadata and playback state changes.
 *
 * Registers a [MediaController.Callback] on each active session and emits a
 * [NowPlayingEvent] to [events] whenever a track changes and the session is actively playing.
 *
 * Lifecycle: call [attach] when a new session token is available (typically from
 * [NowPlayingListenerService.onListenerConnected] / [NowPlayingListenerService.onNotificationPosted]).
 * Call [detachAll] when the service disconnects.
 */
@Singleton
class MediaSessionMonitor @Inject constructor(
    private val logger: PipelineLogger,
    @Named("io") private val ioDispatcher: CoroutineDispatcher,
    private val allowlistPolicy: MediaPackageAllowlistPolicy
) {
    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val _events = MutableSharedFlow<NowPlayingEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<NowPlayingEvent> = _events.asSharedFlow()

    private val controllers = mutableMapOf<String, MediaController>()
    private val callbacks = mutableMapOf<String, MediaController.Callback>()

    /**
     * Attaches a pre-built [MediaController] for [packageName].
     */
    fun attach(controller: MediaController, packageName: String) {
        if (controllers.containsKey(packageName)) return

        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                evaluateCandidate(
                    packageName = packageName,
                    playbackState = controller.playbackState?.state,
                    metadata = metadata
                )
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                evaluateCandidate(
                    packageName = packageName,
                    playbackState = state?.state,
                    metadata = controller.metadata
                )
            }

            override fun onSessionDestroyed() {
                detach(packageName)
            }
        }

        controllers[packageName] = controller
        callbacks[packageName] = callback
        controller.registerCallback(callback)
    }

    /** Detaches the controller for [packageName] and unregisters its callback. */
    fun detach(packageName: String) {
        callbacks.remove(packageName)?.let { cb ->
            controllers[packageName]?.unregisterCallback(cb)
        }
        controllers.remove(packageName)
    }

    /** Detaches all registered controllers. Call when the listener service disconnects. */
    fun detachAll() {
        controllers.keys.toList().forEach { detach(it) }
    }

    /**
     * Detaches controllers whose package names are NOT in [activePackages].
     * Called when the list of active sessions changes.
     */
    fun detachStalePackages(activePackages: Set<String>) {
        controllers.keys.toList()
            .filter { it !in activePackages }
            .forEach { detach(it) }
    }

    private fun evaluateCandidate(packageName: String, playbackState: Int?, metadata: MediaMetadata?) {
        if (!allowlistPolicy.isAllowed(packageName)) {
            logger.logIgnoredCandidate(
                reason = PipelineLogger.IgnoredCandidateReason.PACKAGE_NOT_ALLOWLISTED,
                packageName = packageName
            )
            return
        }

        if (playbackState != PlaybackState.STATE_PLAYING) {
            logger.logIgnoredCandidate(
                reason = PipelineLogger.IgnoredCandidateReason.PLAYBACK_NOT_PLAYING,
                packageName = packageName,
                details = mapOf("state" to playbackState.toString())
            )
            return
        }

        if (metadata == null) {
            logger.logIgnoredCandidate(
                reason = PipelineLogger.IgnoredCandidateReason.METADATA_MISSING_REQUIRED_FIELDS,
                packageName = packageName,
                details = mapOf("missing" to "title,artist")
            )
            return
        }

        emitEvent(metadata = metadata, sourceApp = packageName)
    }

    private fun emitEvent(metadata: MediaMetadata, sourceApp: String) {
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)?.takeIf { it.isNotBlank() }

        if (title == null || artist == null) {
            val missingFields = buildList {
                if (title == null) add("title")
                if (artist == null) add("artist")
            }.joinToString(",")

            logger.logIgnoredCandidate(
                reason = PipelineLogger.IgnoredCandidateReason.METADATA_MISSING_REQUIRED_FIELDS,
                packageName = sourceApp,
                details = mapOf("missing" to missingFields)
            )
            return
        }

        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }

        val event = NowPlayingEvent(
            title = title,
            artist = artist,
            album = album,
            sourceApp = sourceApp
        )

        logger.logDetection(event)
        scope.launch { _events.emit(event) }
    }
}
