package com.capeddle.namethattunelab.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capeddle.namethattunelab.R
import com.capeddle.namethattunelab.presentation.theme.NtlTheme

/**
 * A slim status banner that communicates the listener permission state.
 *
 * Shown at the top of [MainScreen] so the user immediately knows whether
 * the NotificationListenerService has been granted access.
 *
 * @param isGranted `true` when BIND_NOTIFICATION_LISTENER_SERVICE is active.
 * @param onOpenSettings invoked when the missing-permission banner is tapped.
 */
@Composable
fun PermissionStatusBar(isGranted: Boolean, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (isGranted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = if (isGranted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .testTag(PERMISSION_STATUS_BAR_TAG)
            .then(
                if (isGranted) {
                    Modifier
                } else {
                    Modifier.clickable(role = Role.Button, onClick = onOpenSettings)
                }
            ),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isGranted) {
                    stringResource(R.string.permission_granted)
                } else {
                    stringResource(R.string.permission_missing)
                },
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
@Suppress("UnusedPrivateMember")
private fun PreviewPermissionStatusBarGranted() {
    NtlTheme { PermissionStatusBar(isGranted = true, onOpenSettings = {}) }
}

@Preview(showBackground = true)
@Composable
@Suppress("UnusedPrivateMember")
private fun PreviewPermissionStatusBarDenied() {
    NtlTheme { PermissionStatusBar(isGranted = false, onOpenSettings = {}) }
}

internal const val PERMISSION_STATUS_BAR_TAG = "permission_status_banner"
