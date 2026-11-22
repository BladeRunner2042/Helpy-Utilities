package com.example.helpyutilities.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.helpyutilities.data.AppPreferences

/**
 * Clean Material 3 Surface Container Card:
 * Uses tonal surface elevation with a subtle 1dp hairline border (zero heavy glows)
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val themeMode = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val resolvedBg = backgroundColor ?: themeMode.cardBackground
    val resolvedBorder = borderColor ?: themeMode.borderColor

    Surface(
        modifier = modifier
            .clip(shape)
            .border(BorderStroke(borderWidth, resolvedBorder), shape),
        shape = shape,
        color = resolvedBg,
        tonalElevation = 0.dp,
    ) {
        Box {
            content()
        }
    }
}
