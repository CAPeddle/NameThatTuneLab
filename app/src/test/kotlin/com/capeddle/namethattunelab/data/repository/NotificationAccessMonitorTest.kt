package com.capeddle.namethattunelab.data.repository

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import app.cash.turbine.test
import com.capeddle.namethattunelab.nowplaying.NowPlayingListenerService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationAccessMonitorTest {

    private lateinit var context: Context
    private lateinit var monitor: NotificationAccessMonitor

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        monitor = NotificationAccessMonitor(context)

        ShadowContentResolver.reset()
        Settings.Secure.putString(
            context.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS_KEY,
            null
        )
    }

    @Test
    fun `observe emits false when secure setting is null`() = runTest {
        Settings.Secure.putString(context.contentResolver, ENABLED_NOTIFICATION_LISTENERS_KEY, null)

        monitor.observe().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe emits false when secure setting is empty`() = runTest {
        Settings.Secure.putString(context.contentResolver, ENABLED_NOTIFICATION_LISTENERS_KEY, "")

        monitor.observe().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe emits true when enabled list contains app listener component`() = runTest {
        val appService = listenerServiceName()
        Settings.Secure.putString(
            context.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS_KEY,
            "com.example.alpha/.Listener:$appService:com.example.beta/.Listener"
        )

        monitor.observe().test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe emits false when enabled list contains other components only`() = runTest {
        Settings.Secure.putString(
            context.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS_KEY,
            "com.example.alpha/.Listener:com.example.beta/.Listener"
        )

        monitor.observe().test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe matches listener component case-insensitively`() = runTest {
        Settings.Secure.putString(
            context.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS_KEY,
            listenerServiceName().uppercase()
        )

        monitor.observe().test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observe registers and unregisters content observer and reacts to updates`() = runTest {
        val settingsUri = Settings.Secure.getUriFor(ENABLED_NOTIFICATION_LISTENERS_KEY)
        val shadowResolver = shadowOf(context.contentResolver)

        assertTrue(shadowResolver.getContentObservers(settingsUri).isEmpty())

        monitor.observe().test {
            assertFalse(awaitItem())
            assertTrue(shadowResolver.getContentObservers(settingsUri).isNotEmpty())
            val registeredObserver = shadowResolver.getContentObservers(settingsUri).first()

            Settings.Secure.putString(
                context.contentResolver,
                ENABLED_NOTIFICATION_LISTENERS_KEY,
                listenerServiceName()
            )
            registeredObserver.onChange(false)

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(shadowResolver.getContentObservers(settingsUri).isEmpty())
    }

    private fun listenerServiceName(): String =
        ComponentName(context, NowPlayingListenerService::class.java).flattenToString()

    private companion object {
        private const val ENABLED_NOTIFICATION_LISTENERS_KEY = "enabled_notification_listeners"
    }
}
