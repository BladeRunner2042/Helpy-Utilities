package com.example.helpyutilities.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.data.AppThemeMode

val LocalAppTheme = staticCompositionLocalOf { AppThemeMode.DARK }

@Composable
fun HelpyUtilitiesTheme(
    themeMode: AppThemeMode? = null,
    content: @Composable () -> Unit,
) {
    val activePrefMode by AppPreferences.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val mode = (themeMode ?: activePrefMode).resolved(isSystemDark)

    val colorScheme = if (mode.isLight) {
        lightColorScheme(
            primary = mode.primary,
            onPrimary = mode.onPrimary,
            primaryContainer = mode.primary.copy(alpha = 0.15f),
            onPrimaryContainer = mode.primary,
            secondary = mode.secondary,
            onSecondary = Color.White,
            secondaryContainer = mode.secondary.copy(alpha = 0.15f),
            onSecondaryContainer = mode.secondary,
            background = mode.background,
            onBackground = mode.textPrimary,
            surface = mode.background,
            onSurface = mode.textPrimary,
            surfaceVariant = mode.cardBackground,
            onSurfaceVariant = mode.textSecondary,
            outline = mode.borderColor,
        )
    } else {
        darkColorScheme(
            primary = mode.primary,
            onPrimary = mode.onPrimary,
            primaryContainer = mode.primary.copy(alpha = 0.25f),
            onPrimaryContainer = mode.primary,
            secondary = mode.secondary,
            onSecondary = Color.White,
            secondaryContainer = mode.secondary.copy(alpha = 0.25f),
            onSecondaryContainer = mode.secondary,
            background = mode.background,
            onBackground = mode.textPrimary,
            surface = mode.background,
            onSurface = mode.textPrimary,
            surfaceVariant = mode.cardBackground,
            onSurfaceVariant = mode.textSecondary,
            outline = mode.borderColor,
        )
    }

    CompositionLocalProvider(LocalAppTheme provides mode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
