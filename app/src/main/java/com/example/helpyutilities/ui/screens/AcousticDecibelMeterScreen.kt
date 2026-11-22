package com.example.helpyutilities.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.components.KeepAwakePill
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.NeonRose
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.ui.theme.tabularNumberStyle
import com.example.helpyutilities.util.HapticFeedbackHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.log10
import kotlin.math.sqrt

@Composable
fun AcousticDecibelMeterScreen(
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    var isMeasuring by remember { mutableStateOf(hasPermission) }
    var currentDb by remember { mutableDoubleStateOf(30.0) }
    var minDb by remember { mutableDoubleStateOf(30.0) }
    var maxDb by remember { mutableDoubleStateOf(30.0) }
    var avgDb by remember { mutableDoubleStateOf(30.0) }

    val historySamples = remember { mutableStateListOf<Float>() }

    // Audio sampling loop
    LaunchedEffect(isMeasuring, hasPermission) {
        if (!hasPermission || !isMeasuring) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
                recorder.startRecording()

                val buffer = ShortArray(bufferSize)
                var totalSum = 0.0
                var sampleCount = 0L

                while (isActive && isMeasuring) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sumSquare = 0.0
                        for (i in 0 until read) {
                            sumSquare += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sumSquare / read)
                        // SPL estimation calibrated for standard mobile mic reference
                        val db = (20 * log10(rms.coerceAtLeast(1.0)) - 10.0).coerceIn(20.0, 110.0)

                        totalSum += db
                        sampleCount++

                        withContext(Dispatchers.Main) {
                            currentDb = db
                            if (db < minDb || minDb == 30.0) minDb = db
                            if (db > maxDb) maxDb = db
                            avgDb = totalSum / sampleCount

                            if (historySamples.size >= 40) {
                                historySamples.removeAt(0)
                            }
                            historySamples.add(db.toFloat())
                        }
                    }
                    delay(120)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    recorder?.stop()
                    recorder?.release()
                } catch (_: Exception) {}
            }
        }
    }

    val dbDescription = when {
        currentDb < 40 -> "Quiet Studio / Whisper" to EmeraldLime
        currentDb < 60 -> "Quiet Office / Normal Home" to ElectricCyan
        currentDb < 75 -> "Normal Conversation / TV" to SunsetGold
        currentDb < 85 -> "Loud Traffic / Busy Street" to SunsetGold
        else -> "Hazardous Noise (>85 dB)" to NeonRose
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        text = "Acoustic Decibel Meter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.textPrimary
                    )
                    Text(
                        text = "Real-time sound pressure level (dB SPL)",
                        fontSize = 12.sp,
                        color = currentTheme.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KeepAwakePill()
                if (onDismiss != null) {
                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh)
                    ) {
                        Text("Close", color = currentTheme.textPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        if (!hasPermission) {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(36.dp))
                    Text(
                        text = "Microphone Permission Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = currentTheme.textPrimary
                    )
                    Text(
                        text = "Sound decibels are measured 100% on-device in RAM. Zero audio is ever recorded, stored, or transmitted.",
                        fontSize = 13.sp,
                        color = currentTheme.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Main Gauge Card
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(currentTheme.surfaceContainerHigh)
                            .border(3.dp, dbDescription.second, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.1f", currentDb),
                                style = tabularNumberStyle(size = 36, fontWeight = FontWeight.ExtraBold, color = currentTheme.textPrimary)
                            )
                            Text(
                                text = "dB SPL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = dbDescription.second
                            )
                        }
                    }

                    Text(
                        text = dbDescription.first,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = dbDescription.second
                    )

                    // Waveform Sparkline
                    if (historySamples.size > 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(currentTheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val lineColor = dbDescription.second
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stepX = size.width / (historySamples.size - 1)
                                val path = Path()

                                historySamples.forEachIndexed { i, db ->
                                    val x = i * stepX
                                    val normalized = ((db - 20f) / 90f).coerceIn(0f, 1f)
                                    val y = size.height - (normalized * size.height)
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    // Stats: Min, Avg, Max
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MIN", fontSize = 11.sp, color = currentTheme.textSecondary)
                            Text(
                                String.format(Locale.US, "%.1f dB", minDb),
                                style = tabularNumberStyle(size = 13, fontWeight = FontWeight.Bold, color = EmeraldLime)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVERAGE", fontSize = 11.sp, color = currentTheme.textSecondary)
                            Text(
                                String.format(Locale.US, "%.1f dB", avgDb),
                                style = tabularNumberStyle(size = 13, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAX", fontSize = 11.sp, color = currentTheme.textSecondary)
                            Text(
                                String.format(Locale.US, "%.1f dB", maxDb),
                                style = tabularNumberStyle(size = 13, fontWeight = FontWeight.Bold, color = NeonRose)
                            )
                        }
                    }

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                HapticFeedbackHelper.trigger(context)
                                isMeasuring = !isMeasuring
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMeasuring) MaterialTheme.colorScheme.error else currentTheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(if (isMeasuring) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isMeasuring) "Pause" else "Resume", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                HapticFeedbackHelper.trigger(context)
                                minDb = currentDb
                                maxDb = currentDb
                                avgDb = currentDb
                                historySamples.clear()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = currentTheme.textPrimary)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
