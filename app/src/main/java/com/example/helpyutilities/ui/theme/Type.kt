package com.example.helpyutilities.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Modern High-Utility Typography:
 * - Geometric headings with tight letter spacing (-0.5sp / -0.2sp)
 * - Inter / native Sans-Serif body with comfortable 1.4x line-height
 * - Dedicated tabular numbers style for calculators, timers, and live readouts
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = (-0.2).sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.15.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.3.sp,
    ),
)

/**
 * Dedicated Tabular Numbers Style for Timers, Calculators & Live Readouts
 * Prevents jittering as digits rapidly change
 */
fun tabularNumberStyle(
    size: Int = 28,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = TextPrimary,
): TextStyle {
    return TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = size.sp,
        fontWeight = fontWeight,
        color = color,
        fontFeatureSettings = "tnum",
    )
}
