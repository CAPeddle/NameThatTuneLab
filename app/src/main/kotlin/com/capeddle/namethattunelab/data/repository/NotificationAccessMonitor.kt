package com.capeddle.namethattunelab.data.repository

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.capeddle.namethattunelab.nowplaying.NowPlayingListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Monitors notification-listener permission state for this app.
 */
@Singleton
class NotificationAccessMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val enabledListenersKey = "enabled_notification_listeners"

    fun observe(): Flow<Boolean> = callbackFlow {
        val resolver = context.contentResolver
        val uri = Settings.Secure.getUriFor(enabledListenersKey)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(isListenerEnabled(resolver))
            }
        }

        trySend(isListenerEnabled(resolver))
        resolver.registerContentObserver(uri, false, observer)

        awaitClose {
            resolver.unregisterContentObserver(observer)
        }
    }.distinctUntilChanged()

    private fun isListenerEnabled(contentResolver: ContentResolver): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            enabledListenersKey
        ) ?: return false

        val serviceName = ComponentName(context, NowPlayingListenerService::class.java)
            .flattenToString()
        return enabled.split(':').any { it.equals(serviceName, ignoreCase = true) }
    }
}
