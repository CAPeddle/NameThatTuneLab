package com.capeddle.namethattunelab.nowplaying

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.capeddle.namethattunelab.R
import com.capeddle.namethattunelab.util.PipelineLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android [NotificationListenerService] that observes active [MediaController]s and feeds
 * metadata changes into [MediaSessionMonitor].
 *
 * This service must be declared in `AndroidManifest.xml` with:
 *   - `android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"`
 *   - `android:foregroundServiceType="mediaPlayback"`
 *
 * The user must grant notification listener access in Settings → Special app access →
 * Notification access. The UI prompts for this via [PermissionStatusBar].
 */
@AndroidEntryPoint
class NowPlayingListenerService : NotificationListenerService() {

    @Inject
    lateinit var monitor: MediaSessionMonitor

    @Inject
    lateinit var logger: PipelineLogger

    private var sessionManager: MediaSessionManager? = null
    private val activeSessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        refreshControllers(controllers ?: emptyList())
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        startForegroundWithNotification()

        sessionManager = getSystemService(MEDIA_SESSION_SERVICE) as? MediaSessionManager
        val cn = ComponentName(this, NowPlayingListenerService::class.java)
        sessionManager?.addOnActiveSessionsChangedListener(
            activeSessionListener,
            cn
        )
        // Immediately attach any already-active sessions
        sessionManager
            ?.getActiveSessions(cn)
            ?.let { refreshControllers(it) }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        sessionManager?.removeOnActiveSessionsChangedListener(activeSessionListener)
        monitor.detachAll()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Active session changes are handled via OnActiveSessionsChangedListener.
        // This override is intentionally minimal.
    }

    // region Private helpers

    private fun refreshControllers(controllers: List<MediaController>) {
        controllers.forEach { controller ->
            monitor.attach(controller, controller.packageName)
        }
        val activePackages = controllers.map { it.packageName }.toSet()
        monitor.detachStalePackages(activePackages)
    }

    private fun startForegroundWithNotification() {
        val channelId = getString(R.string.notification_channel_id)
        val channelName = getString(R.string.notification_channel_name)

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "NameThatTuneLab pipeline status"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    // endregion

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1001
    }
}
