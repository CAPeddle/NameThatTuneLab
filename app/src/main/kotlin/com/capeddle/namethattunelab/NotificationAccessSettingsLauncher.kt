package com.capeddle.namethattunelab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

internal object NotificationAccessSettingsLauncher {

    fun launch(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        return launch(
            context = context,
            packageName = packageName,
            canResolve = { candidate -> candidate.resolveActivity(packageManager) != null },
            startActivity = context::startActivity
        )
    }

    internal fun launch(
        context: Context,
        packageName: String,
        canResolve: (Intent) -> Boolean,
        startActivity: (Intent) -> Unit
    ): Boolean {
        val launchIntent = createLaunchIntent(packageName, canResolve) ?: return false

        if (context !is Activity) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return runCatching {
            startActivity(launchIntent)
        }.fold(
            onSuccess = { true },
            onFailure = { throwable ->
                if (throwable is RuntimeException) {
                    false
                } else {
                    throw throwable
                }
            }
        )
    }

    internal fun createLaunchIntent(packageName: String, canResolve: (Intent) -> Boolean): Intent? =
        candidateIntents(packageName).firstOrNull(canResolve)

    private fun candidateIntents(packageName: String): List<Intent> = listOf(
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        },
        Intent(Settings.ACTION_APPLICATION_SETTINGS)
    )
}
