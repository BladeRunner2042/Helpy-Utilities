package com.example.helpyutilities.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.helpyutilities.data.AppPreferences

/**
 * Clean Material 3 Tonal Canvas Background
 * Strictly avoids battery-wasting glowing orbs or neon aurora meshes.
 */
@Composable
fun AnimatedMeshBackground(
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background),
    )
}
