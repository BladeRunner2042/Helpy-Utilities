package com.example.helpyutilities.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive layout metrics based on screen width and system font scaling.
 * Supports compact phones (<= 360dp), standard screens, tablets, and large font accessibility settings.
 */
data class AdaptiveMetrics(
    val screenWidthDp: Int,
    val fontScale: Float,
    val isCompactWidth: Boolean,
    val isVeryCompactWidth: Boolean,
    val isLargeFontScale: Boolean,
    val horizontalScreenPadding: Dp,
    val cardContentPadding: Dp,
    val bentoGridMinSize: Dp,
    val bottomNavHeight: Dp,
)

@Composable
fun rememberAdaptiveMetrics(): AdaptiveMetrics {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthDp = configuration.screenWidthDp
    val fontScale = density.fontScale

    val isCompactWidth = screenWidthDp < 380
    val isVeryCompactWidth = screenWidthDp <= 360
    val isLargeFontScale = fontScale > 1.15f

    val horizontalScreenPadding = if (isCompactWidth) 14.dp else 18.dp
    val cardContentPadding = if (isCompactWidth) 14.dp else 18.dp
    val bentoGridMinSize = if (isVeryCompactWidth || isLargeFontScale) 140.dp else 156.dp
    val bottomNavHeight = if (isLargeFontScale) 84.dp else 74.dp

    return AdaptiveMetrics(
        screenWidthDp = screenWidthDp,
        fontScale = fontScale,
        isCompactWidth = isCompactWidth,
        isVeryCompactWidth = isVeryCompactWidth,
        isLargeFontScale = isLargeFontScale,
        horizontalScreenPadding = horizontalScreenPadding,
        cardContentPadding = cardContentPadding,
        bentoGridMinSize = bentoGridMinSize,
        bottomNavHeight = bottomNavHeight,
    )
}
