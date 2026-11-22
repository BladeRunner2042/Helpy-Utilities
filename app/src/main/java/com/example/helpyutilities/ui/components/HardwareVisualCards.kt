package com.example.helpyutilities.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.theme.tabularNumberStyle
import com.example.helpyutilities.util.BatteryMetrics
import com.example.helpyutilities.util.CpuCoreFrequency
import com.example.helpyutilities.util.MemoryAndStorageMetrics
import java.util.Locale

/**
 * 1. Battery Telemetry Sparkline Card
 * Rolling 20-sample sparkline of Current (mA) vs Core Temperature (°C).
 * Uses MaterialTheme.colorScheme tokens for clean adaptation across all 6 themes.
 */
@Composable
fun BatteryTelemetrySparklineCard(
    battery: BatteryMetrics,
    currentHistoryMa: List<Float>,
    tempHistoryCelsius: List<Float>,
    modifier: Modifier = Modifier,
) {
    // If currentMa is 0/Idle while unplugged, default to Battery Temperature so the chart shows a real live curve
    val shouldDefaultToTemp = battery.currentNowMa == 0 && !battery.isCharging
    var selectedIndex by remember(shouldDefaultToTemp) { mutableIntStateOf(if (shouldDefaultToTemp) 1 else 0) } // 0: mA, 1: °C
    val colorScheme = MaterialTheme.colorScheme

    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header with Title on Left and Single-line Status Pill on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Battery & Power",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "${battery.levelPercent}% • ${battery.chargingType}",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            // 2. Clean 3-Column Metric Row: Draw, Temp, Health
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Draw",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = if (battery.currentNowMa != 0) "${battery.currentNowMa} mA" else "Idle",
                        style = tabularNumberStyle(size = 14, fontWeight = FontWeight.Bold, color = colorScheme.onSurface),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Temp",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f°C", battery.tempCelsius),
                        style = tabularNumberStyle(size = 14, fontWeight = FontWeight.Bold, color = colorScheme.primary),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Health",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = if (battery.voltageMv > 0) String.format(Locale.US, "%.2fV • %s", battery.voltageMv / 1000f, battery.healthStatus) else battery.healthStatus,
                        style = tabularNumberStyle(size = 13, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // 3. Equal-Height Segmented Button Toggle: mA vs °C
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                SegmentedButton(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.height(42.dp),
                ) {
                    Text(
                        text = "Current (mA)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                SegmentedButton(
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.height(42.dp),
                ) {
                    Text(
                        text = "Temp (°C)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            // Sparkline Graph
            val activeHistory = if (selectedIndex == 0) currentHistoryMa else tempHistoryCelsius
            val primaryColor = colorScheme.primary
            val fillColor = primaryColor.copy(alpha = 0.14f)
            val gridColor = colorScheme.outline.copy(alpha = 0.25f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                if (activeHistory.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Subtle horizontal reference grid lines
                        drawLine(gridColor, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 1f)
                        drawLine(gridColor, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)
                        drawLine(gridColor, Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 1f)

                        val minVal = activeHistory.minOrNull() ?: 0f
                        val maxVal = activeHistory.maxOrNull() ?: 100f
                        val range = (maxVal - minVal).coerceAtLeast(1f)

                        val stepX = w / (activeHistory.size - 1).coerceAtLeast(1)
                        val linePath = Path()
                        val areaPath = Path()

                        activeHistory.forEachIndexed { i, value ->
                            val x = i * stepX
                            val normalized = ((value - minVal) / range).coerceIn(0f, 1f)
                            val y = h - (normalized * (h - 16f)) - 8f

                            if (i == 0) {
                                linePath.moveTo(x, y)
                                areaPath.moveTo(x, h)
                                areaPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                areaPath.lineTo(x, y)
                            }

                            if (i == activeHistory.size - 1) {
                                areaPath.lineTo(x, h)
                                areaPath.close()
                            }
                        }

                        // Translucent area fill
                        drawPath(path = areaPath, color = fillColor)

                        // Sparkline trace
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Live pulsating dot on newest sample
                        val lastVal = activeHistory.last()
                        val lastNorm = ((lastVal - minVal) / range).coerceIn(0f, 1f)
                        val lastX = (activeHistory.size - 1) * stepX
                        val lastY = h - (lastNorm * (h - 16f)) - 8f

                        drawCircle(color = primaryColor.copy(alpha = 0.35f), radius = 7.dp.toPx(), center = Offset(lastX, lastY))
                        drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
                        drawCircle(color = colorScheme.surface, radius = 1.5.dp.toPx(), center = Offset(lastX, lastY))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Calibrating live telemetry...",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Footer live indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedIndex == 0) "Sampled rolling mA" else "Sampled rolling °C",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Status: ${battery.thermalStatus}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary
                )
            }
        }
    }
}

/**
 * 2. Memory & Storage Dual Circular Progress Rings Card
 * Side-by-side animated circular rings showing System RAM and Internal Storage.
 */
@Composable
fun MemoryAndStorageRingsCard(
    metrics: MemoryAndStorageMetrics,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val animatedRamPercent by animateFloatAsState(
        targetValue = metrics.ramPercent.toFloat(),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "RamPercentAnim"
    )

    val animatedStoragePercent by animateFloatAsState(
        targetValue = metrics.storagePercent.toFloat(),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "StoragePercentAnim"
    )

    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Memory & Internal Storage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Text(
                    text = "Live Allocation",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dual Progress Rings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ring 1: System RAM
                CircularTelemetryGauge(
                    title = "SYSTEM RAM",
                    percent = animatedRamPercent,
                    usedText = String.format(Locale.US, "%.1f", metrics.ramUsedGb),
                    totalText = String.format(Locale.US, "%.1f GB", metrics.ramTotalGb),
                    ringColor = colorScheme.primary,
                    trackColor = colorScheme.outline.copy(alpha = 0.2f),
                    textColor = colorScheme.onSurface,
                    subTextColor = colorScheme.onSurfaceVariant
                )

                // Ring 2: Storage
                CircularTelemetryGauge(
                    title = "STORAGE",
                    percent = animatedStoragePercent,
                    usedText = String.format(Locale.US, "%.1f", metrics.storageUsedGb),
                    totalText = String.format(Locale.US, "%.1f GB", metrics.storageTotalGb),
                    ringColor = colorScheme.secondary,
                    trackColor = colorScheme.outline.copy(alpha = 0.2f),
                    textColor = colorScheme.onSurface,
                    subTextColor = colorScheme.onSurfaceVariant
                )
            }

            // Breakdown Footnote
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val freeRam = (metrics.ramTotalGb - metrics.ramUsedGb).coerceAtLeast(0f)
                val freeStorage = (metrics.storageTotalGb - metrics.storageUsedGb).coerceAtLeast(0f)
                Text(
                    text = "Free RAM: ${String.format(Locale.US, "%.1f GB", freeRam)}",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Free Storage: ${String.format(Locale.US, "%.1f GB", freeStorage)}",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CircularTelemetryGauge(
    title: String,
    percent: Float,
    usedText: String,
    totalText: String,
    ringColor: Color,
    trackColor: Color,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(105.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 9.dp.toPx()

                // Background track arc (270 degrees)
                drawArc(
                    color = trackColor,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                // Active progress arc
                val sweep = (percent / 100f).coerceIn(0f, 1f) * 270f
                drawArc(
                    color = ringColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${percent.toInt()}%",
                    style = tabularNumberStyle(size = 18, fontWeight = FontWeight.ExtraBold, color = textColor)
                )
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = subTextColor,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Text(
            text = "$usedText / $totalText",
            style = tabularNumberStyle(size = 12, fontWeight = FontWeight.SemiBold, color = textColor)
        )
    }
}

/**
 * 3. Live Per-Core CPU Frequency Bars Card
 * 2-column grid showing CPU 0 through CPU N, current MHz, max MHz, and progress bars.
 */
@Composable
fun LiveCpuCoresCard(
    cores: List<CpuCoreFrequency>,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Live CPU Clocks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                Text(
                    text = "${cores.size} Active Cores",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 2-Column Core Grid
            val chunkedCores = remember(cores) { cores.chunked(2) }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                chunkedCores.forEach { rowCores ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCores.forEach { core ->
                            Box(modifier = Modifier.weight(1f)) {
                                CpuCoreTile(core = core)
                            }
                        }
                        if (rowCores.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Summary
            val activeClocks = cores.filter { it.curMhz > 0 }
            val avgClock = if (activeClocks.isNotEmpty()) activeClocks.map { it.curMhz }.average().toInt() else 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Average Active Clock: $avgClock MHz",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Polling: 3s sysfs",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CpuCoreTile(
    core: CpuCoreFrequency,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val animatedRatio by animateFloatAsState(
        targetValue = core.usageRatio,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "CoreRatioAnim_${core.core}"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CPU ${core.core}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = if (core.curMhz > 0) "${core.curMhz} MHz" else "Idle",
                    style = tabularNumberStyle(
                        size = 12,
                        fontWeight = FontWeight.Bold,
                        color = if (core.curMhz > 0) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                )
            }

            // Frequency bar
            LinearProgressIndicator(
                progress = { animatedRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = colorScheme.primary,
                trackColor = colorScheme.outline.copy(alpha = 0.2f),
            )

            Text(
                text = if (core.maxMhz > 0) "Max: ${core.maxMhz} MHz" else "Max: Dynamic",
                fontSize = 10.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}
