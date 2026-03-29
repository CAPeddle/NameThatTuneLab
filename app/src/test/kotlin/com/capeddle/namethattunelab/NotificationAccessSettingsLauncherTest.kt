package com.capeddle.namethattunelab

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationAccessSettingsLauncherTest {

    @Test
    fun `should prefer notification listener settings when primary action resolves`() {
        val intent = NotificationAccessSettingsLauncher.createLaunchIntent(PACKAGE_NAME) { candidate ->
            candidate.action == Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        }

        assertEquals(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, intent?.action)
    }

    @Test
    fun `should use app details settings when primary action cannot resolve`() {
        val intent = NotificationAccessSettingsLauncher.createLaunchIntent(PACKAGE_NAME) { candidate ->
            candidate.action != Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        }

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent?.action)
        assertEquals("package:$PACKAGE_NAME", intent?.dataString)
    }

    @Test
    fun `should use application settings when only fallback action resolves`() {
        val intent = NotificationAccessSettingsLauncher.createLaunchIntent(PACKAGE_NAME) { candidate ->
            candidate.action == Settings.ACTION_APPLICATION_SETTINGS
        }

        assertEquals(Settings.ACTION_APPLICATION_SETTINGS, intent?.action)
    }

    @Test
    fun `should return null when no settings intents resolve`() {
        val intent = NotificationAccessSettingsLauncher.createLaunchIntent(PACKAGE_NAME) { false }

        assertNull(intent)
    }

    @Test
    fun `should return false when startActivity throws runtime exception`() {
        val launched = NotificationAccessSettingsLauncher.launch(
            context = AnyContext,
            packageName = PACKAGE_NAME,
            canResolve = { true },
            startActivity = { throw SecurityException("blocked") }
        )

        assertFalse(launched)
    }

    @Test(expected = AssertionError::class)
    fun `should rethrow non-runtime throwable from startActivity`() {
        NotificationAccessSettingsLauncher.launch(
            context = AnyContext,
            packageName = PACKAGE_NAME,
            canResolve = { true },
            startActivity = { throw AssertionError("non-runtime failure") }
        )
    }

    @Test
    fun `should add new task flag when context is not an activity`() {
        var capturedFlags = 0

        val launched = NotificationAccessSettingsLauncher.launch(
            context = AnyContext,
            packageName = PACKAGE_NAME,
            canResolve = { true },
            startActivity = { intent -> capturedFlags = intent.flags }
        )

        assertTrue(launched)
        assertTrue(capturedFlags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    private companion object {
        private const val PACKAGE_NAME = "com.capeddle.namethattunelab"
        private val AnyContext = org.robolectric.RuntimeEnvironment.getApplication()
    }
}
