package com.example.helpyutilities.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.ui.theme.tabularNumberStyle
import com.example.helpyutilities.util.AccurateSpeedTestEngine
import com.example.helpyutilities.util.HapticFeedbackHelper
import com.example.helpyutilities.util.LocalNetworkDetails
import com.example.helpyutilities.util.NetworkDetailsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.min

enum class SpeedTestStage {
    IDLE, PING, DOWNLOAD, UPLOAD, COMPLETE
}

@Composable
fun NetworkSpeedTestScreen() {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var stage by remember { mutableStateOf(SpeedTestStage.IDLE) }
    var connectionLabel by remember { mutableStateOf("Checking network...") }
    var isMobileData by remember { mutableStateOf(false) }
    var isDataSaver by remember { mutableStateOf(false) }

    var liveMbps by remember { mutableDoubleStateOf(0.0) }
    var downloadMbps by remember { mutableDoubleStateOf(0.0) }
    var uploadMbps by remember { mutableDoubleStateOf(0.0) }
    var pingMs by remember { mutableDoubleStateOf(0.0) }
    var jitterMs by remember { mutableDoubleStateOf(0.0) }
    val speedSamples = remember { mutableStateListOf<Double>() }

    var testJob by remember { mutableStateOf<Job?>(null) }

    fun detectConnection() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(network)
        if (caps != null) {
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    connectionLabel = "Wi-Fi Connected"
                    isMobileData = false
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    connectionLabel = "Mobile Data • Quick Mode Auto-Selected"
                    isMobileData = true
                    isDataSaver = true
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    connectionLabel = "Ethernet Connected"
                    isMobileData = false
                }
                else -> {
                    connectionLabel = "Online (Active Network)"
                    isMobileData = false
                }
            }
        } else {
            connectionLabel = "Offline — No Internet"
            isMobileData = false
        }
    }

    LaunchedEffect(Unit) {
        detectConnection()
    }

    DisposableEffect(Unit) {
        onDispose {
            testJob?.cancel()
        }
    }

    fun stopTest() {
        testJob?.cancel()
        testJob = null
        stage = SpeedTestStage.IDLE
        liveMbps = 0.0
    }

    fun startSpeedTest() {
        HapticFeedbackHelper.trigger(context)
        detectConnection()
        liveMbps = 0.0
        downloadMbps = 0.0
        uploadMbps = 0.0
        pingMs = 0.0
        jitterMs = 0.0
        speedSamples.clear()
        stage = SpeedTestStage.PING

        testJob = coroutineScope.launch {
            try {
                // 1. PING & JITTER (Discards DNS/TLS cold-start probe)
                val (measuredPing, measuredJitter) = AccurateSpeedTestEngine.measureLatency(
                    isCancelled = { !isActive },
                    isDataSaver = isDataSaver
                )
                if (!isActive) return@launch

                withContext(Dispatchers.Main) {
                    pingMs = measuredPing
                    jitterMs = measuredJitter
                    stage = SpeedTestStage.DOWNLOAD
                }

                // 2. MULTI-STREAM DOWNLOAD TEST (Parallel Streams + Warm-Up Discard + Trimmed Mean)
                val finalDownload = AccurateSpeedTestEngine.measureDownload(
                    onProgress = { rawMbps ->
                        val smoothed = if (liveMbps <= 0.0) rawMbps else (liveMbps * 0.7) + (rawMbps * 0.3)
                        liveMbps = smoothed
                        downloadMbps = smoothed
                        if (speedSamples.size > 28) speedSamples.removeAt(0)
                        speedSamples.add(smoothed)
                    },
                    isCancelled = { !isActive },
                    isDataSaver = isDataSaver
                )
                if (!isActive) return@launch

                withContext(Dispatchers.Main) {
                    downloadMbps = if (finalDownload > 0) finalDownload else downloadMbps
                    stage = SpeedTestStage.UPLOAD
                    liveMbps = 0.0
                }

                // 3. MULTI-STREAM UPLOAD TEST (Parallel Streams + Warm-Up Discard + Trimmed Mean)
                val finalUpload = AccurateSpeedTestEngine.measureUpload(
                    onProgress = { rawMbps ->
                        val smoothed = if (liveMbps <= 0.0) rawMbps else (liveMbps * 0.7) + (rawMbps * 0.3)
                        liveMbps = smoothed
                        uploadMbps = smoothed
                    },
                    isCancelled = { !isActive },
                    isDataSaver = isDataSaver
                )
                if (!isActive) return@launch

                withContext(Dispatchers.Main) {
                    uploadMbps = if (finalUpload > 0) finalUpload else uploadMbps
                    stage = SpeedTestStage.COMPLETE
                    liveMbps = downloadMbps
                    HapticFeedbackHelper.trigger(context)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    stage = SpeedTestStage.IDLE
                    Toast.makeText(context, "Speed test interrupted: ${e.message ?: "Network error"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val animatedMbps by animateFloatAsState(
        targetValue = liveMbps.toFloat(),
        animationSpec = tween(
            durationMillis = 550,
            easing = EaseOutCubic
        ),
        label = "liveMbpsAnimation"
    )

    val isRunning = stage != SpeedTestStage.IDLE && stage != SpeedTestStage.COMPLETE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection & Mobile Data Warning Pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(100.dp))
                .background(if (isMobileData) SunsetGold.copy(alpha = 0.16f) else currentTheme.surfaceContainerHigh)
                .border(
                    width = 1.dp,
                    color = if (isMobileData) SunsetGold else currentTheme.borderColor,
                    shape = RoundedCornerShape(100.dp)
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isMobileData) Icons.Default.CellTower else Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (isMobileData) SunsetGold else currentTheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = connectionLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMobileData) SunsetGold else currentTheme.textPrimary
                )
            }
        }

        // Data Saver / Full Test Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(currentTheme.surfaceContainerHigh)
                .border(1.dp, currentTheme.borderColor, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Full Precision (~8.5s)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (!isDataSaver) currentTheme.primary.copy(alpha = 0.16f) else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (!isDataSaver) currentTheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = !isRunning) {
                        HapticFeedbackHelper.trigger(context)
                        isDataSaver = false
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (!isDataSaver) currentTheme.primary else currentTheme.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Full Precision (~8.5s)",
                        fontSize = 12.sp,
                        fontWeight = if (!isDataSaver) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isDataSaver) currentTheme.primary else currentTheme.textSecondary
                    )
                }
            }

            // Quick Saver (~4s)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isDataSaver) currentTheme.primary.copy(alpha = 0.16f) else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDataSaver) currentTheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = !isRunning) {
                        HapticFeedbackHelper.trigger(context)
                        isDataSaver = true
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isDataSaver) currentTheme.primary else currentTheme.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Quick Saver (~4s)",
                        fontSize = 12.sp,
                        fontWeight = if (isDataSaver) FontWeight.Bold else FontWeight.Medium,
                        color = if (isDataSaver) currentTheme.primary else currentTheme.textSecondary
                    )
                }
            }
        }

        // Main Reactive Arc Gauge Card
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Arc Gauge Box
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeColor = currentTheme.primary
                    val trackColor = currentTheme.surfaceContainerHighest

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val radius = (min(size.width, size.height) / 2) - strokeWidth
                        val center = Offset(size.width / 2, size.height / 2)
                        val startAngle = 135f
                        val sweepTotal = 270f

                        // Background track
                        drawArc(
                            color = trackColor,
                            startAngle = startAngle,
                            sweepAngle = sweepTotal,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active sweep
                        val maxScale = 200f
                        val progress = (animatedMbps / maxScale).coerceIn(0f, 1f)
                        if (progress > 0) {
                            drawArc(
                                color = activeColor,
                                startAngle = startAngle,
                                sweepAngle = sweepTotal * progress,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (stage) {
                                SpeedTestStage.IDLE -> "READY TO TEST"
                                SpeedTestStage.PING -> "MEASURING PING..."
                                SpeedTestStage.DOWNLOAD -> "TESTING DOWNLOAD..."
                                SpeedTestStage.UPLOAD -> "TESTING UPLOAD..."
                                SpeedTestStage.COMPLETE -> "TEST COMPLETE"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", animatedMbps),
                            style = tabularNumberStyle(
                                size = 46,
                                fontWeight = FontWeight.ExtraBold,
                                color = currentTheme.textPrimary
                            )
                        )
                        Text(
                            text = "Mbps",
                            fontSize = 13.sp,
                            color = currentTheme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Live Sparkline during download
                if (speedSamples.size > 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .padding(horizontal = 14.dp)
                    ) {
                        val sparklineColor = currentTheme.primary
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxSample = speedSamples.maxOrNull()?.coerceAtLeast(10.0) ?: 100.0
                            val stepX = size.width / (speedSamples.size - 1)
                            val path = Path()

                            speedSamples.forEachIndexed { idx, value ->
                                val x = idx * stepX
                                val y = size.height - (value / maxSample * size.height).toFloat()
                                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = sparklineColor.copy(alpha = 0.75f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }

                // 4 Clean Tonal Metric Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            modifier = Modifier.weight(1f),
                            label = "DOWNLOAD",
                            value = if (downloadMbps > 0) String.format(Locale.US, "%.1f Mbps", downloadMbps) else "--",
                            icon = Icons.Default.Download,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(String.format(Locale.US, "%.1f Mbps", downloadMbps)))
                                Toast.makeText(context, "Download speed copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        MetricStatCard(
                            modifier = Modifier.weight(1f),
                            label = "UPLOAD",
                            value = if (uploadMbps > 0) String.format(Locale.US, "%.1f Mbps", uploadMbps) else "--",
                            icon = Icons.Default.Upload,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(String.format(Locale.US, "%.1f Mbps", uploadMbps)))
                                Toast.makeText(context, "Upload speed copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            modifier = Modifier.weight(1f),
                            label = "PING",
                            value = if (pingMs > 0) String.format(Locale.US, "%.0f ms", pingMs) else "--",
                            icon = Icons.Default.Speed,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(String.format(Locale.US, "%.0f ms", pingMs)))
                                Toast.makeText(context, "Ping copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        MetricStatCard(
                            modifier = Modifier.weight(1f),
                            label = "JITTER",
                            value = if (jitterMs > 0) String.format(Locale.US, "%.1f ms", jitterMs) else "--",
                            icon = Icons.Default.GraphicEq,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(String.format(Locale.US, "%.1f ms", jitterMs)))
                                Toast.makeText(context, "Jitter copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Start / Stop Button
        Button(
            onClick = {
                if (isRunning) stopTest() else startSpeedTest()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) MaterialTheme.colorScheme.error else currentTheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = if (isRunning) "Stop Test" else if (stage == SpeedTestStage.COMPLETE) "Run Again" else "Start Speed Test",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Wi-Fi Band, Gateway IP & DNS Inspector Card
        var isNetworkCardExpanded by remember { mutableStateOf(true) }
        var networkDetails by remember { mutableStateOf(NetworkDetailsHelper.getDetails(context)) }

        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticFeedbackHelper.trigger(context)
                            isNetworkCardExpanded = !isNetworkCardExpanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lan,
                            contentDescription = null,
                            tint = currentTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Network & Gateway Inspector",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.textPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                HapticFeedbackHelper.trigger(context)
                                networkDetails = NetworkDetailsHelper.getDetails(context)
                                Toast.makeText(context, "Network details refreshed", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = currentTheme.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Icon(
                            imageVector = if (isNetworkCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = currentTheme.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (isNetworkCardExpanded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NetworkDetailRow(
                            label = "Transport Type",
                            value = networkDetails.transportType,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(networkDetails.transportType))
                                Toast.makeText(context, "Copied Transport Type", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NetworkDetailRow(
                            label = "Local IPv4",
                            value = networkDetails.localIpv4,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(networkDetails.localIpv4))
                                Toast.makeText(context, "Copied Local IPv4", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NetworkDetailRow(
                            label = "Router / Gateway IP",
                            value = networkDetails.gatewayIp,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(networkDetails.gatewayIp))
                                Toast.makeText(context, "Copied Gateway IP", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NetworkDetailRow(
                            label = "DNS Servers",
                            value = networkDetails.dnsServers,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(networkDetails.dnsServers))
                                Toast.makeText(context, "Copied DNS Servers", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NetworkDetailRow(
                            label = "Wi-Fi Frequency Band",
                            value = networkDetails.wifiBand,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(networkDetails.wifiBand))
                                Toast.makeText(context, "Copied Wi-Fi Band", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NetworkDetailRow(
                            label = "Link Speed",
                            value = if (networkDetails.linkSpeedMbps > 0) "${networkDetails.linkSpeedMbps} Mbps" else "N/A",
                            onCopy = {
                                clipboardManager.setText(AnnotatedString("${networkDetails.linkSpeedMbps} Mbps"))
                                Toast.makeText(context, "Copied Link Speed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkDetailRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(currentTheme.surfaceContainerHigh)
            .clickable {
                HapticFeedbackHelper.trigger(context)
                onCopy()
            }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = currentTheme.textSecondary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = currentTheme.textPrimary,
                style = tabularNumberStyle(size = 12, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = currentTheme.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun MetricStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(currentTheme.surfaceContainerHigh)
            .border(1.dp, currentTheme.borderColor, RoundedCornerShape(14.dp))
            .clickable { onCopy() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = currentTheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = tabularNumberStyle(
                        size = 14,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.textPrimary
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = currentTheme.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
