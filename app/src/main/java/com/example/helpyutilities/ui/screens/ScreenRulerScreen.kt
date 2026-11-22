package com.example.helpyutilities.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.KeepAwakePill
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.tabularNumberStyle
import com.example.helpyutilities.util.HapticFeedbackHelper
import java.util.Locale

@Composable
fun ScreenRulerScreen(
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val density = LocalDensity.current
    val displayMetrics = context.resources.displayMetrics

    // Exact pixels per inch and pixels per cm
    val xdpi = displayMetrics.xdpi
    val ydpi = displayMetrics.ydpi
    val dpi = if (ydpi > 0f) ydpi else (density.density * 160f)
    val pixelsPerMm = dpi / 25.4f
    val pixelsPerInch = dpi

    var isMetric by remember { mutableStateOf(true) } // true: mm/cm, false: inches
    var caliperY by remember { mutableFloatStateOf(300f) }

    val measuredValue = if (isMetric) {
        caliperY / (pixelsPerMm * 10f) // in cm
    } else {
        caliperY / pixelsPerInch // in inches
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDismiss != null) {
                    IconButton(onClick = {
                        HapticFeedbackHelper.trigger(context)
                        onDismiss()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = currentTheme.textPrimary)
                    }
                }
                Text(
                    text = "Screen Ruler",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = isMetric,
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        isMetric = true
                    },
                    label = { Text("cm / mm", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentTheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = currentTheme.surfaceContainerHigh,
                        labelColor = currentTheme.textSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = !isMetric,
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        isMetric = false
                    },
                    label = { Text("inches", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentTheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = currentTheme.surfaceContainerHigh,
                        labelColor = currentTheme.textSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                KeepAwakePill()
            }
        }

        // Caliper Readout Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentTheme.surfaceContainerHigh)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Touch & drag line to measure:",
                fontSize = 12.sp,
                color = currentTheme.textSecondary
            )
            Text(
                text = if (isMetric) String.format(Locale.US, "%.2f cm (%.1f mm)", measuredValue, measuredValue * 10f)
                else String.format(Locale.US, "%.3f in", measuredValue),
                style = tabularNumberStyle(size = 16, fontWeight = FontWeight.Bold, color = EmeraldLime)
            )
        }

        // Interactive Ruler Area with Caliper Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        caliperY = (caliperY + dragAmount.y).coerceIn(0f, size.height.toFloat())
                    }
                }
        ) {
            // Ruler Ticks Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val heightPx = size.height
                val tickColor = Color(0x6694A3B8)
                val majorTickColor = currentTheme.textPrimary

                if (isMetric) {
                    var mm = 0
                    while (mm * pixelsPerMm <= heightPx) {
                        val y = mm * pixelsPerMm
                        val isCm = (mm % 10 == 0)
                        val isMid = (mm % 5 == 0)
                        val tickLen = when {
                            isCm -> 48.dp.toPx()
                            isMid -> 32.dp.toPx()
                            else -> 18.dp.toPx()
                        }
                        drawLine(
                            color = if (isCm) majorTickColor else tickColor,
                            start = Offset(0f, y),
                            end = Offset(tickLen, y),
                            strokeWidth = if (isCm) 2.dp.toPx() else 1.dp.toPx()
                        )
                        drawLine(
                            color = if (isCm) majorTickColor else tickColor,
                            start = Offset(size.width - tickLen, y),
                            end = Offset(size.width, y),
                            strokeWidth = if (isCm) 2.dp.toPx() else 1.dp.toPx()
                        )
                        mm++
                    }
                } else {
                    val pixelsPer16th = pixelsPerInch / 16f
                    var step = 0
                    while (step * pixelsPer16th <= heightPx) {
                        val y = step * pixelsPer16th
                        val isInch = (step % 16 == 0)
                        val isHalf = (step % 8 == 0)
                        val isQuarter = (step % 4 == 0)
                        val tickLen = when {
                            isInch -> 52.dp.toPx()
                            isHalf -> 36.dp.toPx()
                            isQuarter -> 26.dp.toPx()
                            else -> 16.dp.toPx()
                        }
                        drawLine(
                            color = if (isInch) majorTickColor else tickColor,
                            start = Offset(0f, y),
                            end = Offset(tickLen, y),
                            strokeWidth = if (isInch) 2.dp.toPx() else 1.dp.toPx()
                        )
                        drawLine(
                            color = if (isInch) majorTickColor else tickColor,
                            start = Offset(size.width - tickLen, y),
                            end = Offset(size.width, y),
                            strokeWidth = if (isInch) 2.dp.toPx() else 1.dp.toPx()
                        )
                        step++
                    }
                }
            }

            // Draggable Caliper Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = with(density) { caliperY.toDp() })
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                    drawLine(
                        color = EmeraldLime,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
