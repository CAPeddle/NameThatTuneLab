package com.capeddle.namethattunelab.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NtlPrimary,
    onPrimary = NtlOnPrimary,
    primaryContainer = NtlPrimaryContainer,
    onPrimaryContainer = NtlOnPrimaryContainer,
    secondary = NtlSecondary,
    onSecondary = NtlOnSecondary,
    secondaryContainer = NtlSecondaryContainer,
    onSecondaryContainer = NtlOnSecondaryContainer,
    tertiary = NtlTertiary,
    onTertiary = NtlOnTertiary,
    tertiaryContainer = NtlTertiaryContainer,
    onTertiaryContainer = NtlOnTertiaryContainer,
    error = NtlError,
    onError = NtlOnError,
    errorContainer = NtlErrorContainer,
    onErrorContainer = NtlOnErrorContainer,
    background = NtlBackground,
    onBackground = NtlOnBackground,
    surface = NtlSurface,
    onSurface = NtlOnSurface,
    surfaceVariant = NtlSurfaceVariant,
    onSurfaceVariant = NtlOnSurfaceVariant,
    outline = NtlOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = NtlDarkPrimary,
    onPrimary = NtlDarkOnPrimary,
    primaryContainer = NtlDarkPrimaryContainer,
    onPrimaryContainer = NtlDarkOnPrimaryContainer,
    secondary = NtlDarkSecondary,
    onSecondary = NtlDarkOnSecondary,
    secondaryContainer = NtlDarkSecondaryContainer,
    onSecondaryContainer = NtlDarkOnSecondaryContainer,
    tertiary = NtlDarkTertiary,
    onTertiary = NtlDarkOnTertiary,
    tertiaryContainer = NtlDarkTertiaryContainer,
    onTertiaryContainer = NtlDarkOnTertiaryContainer,
    error = NtlDarkError,
    onError = NtlDarkOnError,
    errorContainer = NtlDarkErrorContainer,
    onErrorContainer = NtlDarkOnErrorContainer,
    background = NtlDarkBackground,
    onBackground = NtlDarkOnBackground,
    surface = NtlDarkSurface,
    onSurface = NtlDarkOnSurface,
    surfaceVariant = NtlDarkSurfaceVariant,
    onSurfaceVariant = NtlDarkOnSurfaceVariant,
    outline = NtlDarkOutline
)

@Composable
fun NtlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NtlTypography,
        shapes = NtlShapes,
        content = content
    )
}
