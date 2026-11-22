package com.example.helpyutilities.ui.screens

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Widgets
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.ui.components.BentoToolCard
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.components.SmartClipboardBanner
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.util.HapticFeedbackHelper
import java.util.Locale

data class PinnedTool(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val tabIndex: Int,
)

/**
 * Material You Bento Grid Home Dashboard:
 * 1. Single Top Bar (Clean Title + Single Settings button)
 * 2. Stadium-pill Search Bar ("Search 16+ tools...")
 * 3. Smart Clipboard Sense Banner (Auto-detects currencies, math, units, or text)
 * 4. Pinned & Recent Strip (Torch, Level, Translate, Currency, etc.)
 * 5. 2x1 Wide Bento Cards (Scientific Calculator & Universal Translator)
 * 6. Live Interactive Mini-Widgets (Live Spirit Level & Live Currency Pair right on dashboard)
 * 7. Privacy Quick Bar
 */
@Composable
fun HomeScreen(
    onNavigateToTab: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current

    // Request notification permission cleanly on startup (after 1-second delay so UI loads first)
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Handled */ }

    LaunchedEffect(Unit) {
        delay(1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Lifecycle-aware foreground check to pause accelerometer in background
    val isAppForeground = com.example.helpyutilities.ui.components.rememberIsAppForeground()

    // Live accelerometer tilt for dashboard mini-level
    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    val accelerometer = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    DisposableEffect(isAppForeground) {
        if (!isAppForeground) {
            return@DisposableEffect onDispose {}
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    pitch = (Math.atan2(y.toDouble(), kotlin.math.sqrt((x * x + z * z).toDouble())) * 180 / Math.PI).toFloat()
                    roll = (Math.atan2(-x.toDouble(), z.toDouble()) * 180 / Math.PI).toFloat()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val pinnedIds by AppPreferences.pinnedToolIds.collectAsState()

    val allPinnedTools = remember(currentTheme) {
        listOf(
            PinnedTool("level", "Level", Icons.Default.Sensors, currentTheme.secondary, 1),
            PinnedTool("translate", "Translate", Icons.Default.Translate, EmeraldLime, 4),
            PinnedTool("currency", "Currency", Icons.Default.CurrencyExchange, SunsetGold, 3),
            PinnedTool("passwords", "Passwords", Icons.Default.Lock, currentTheme.primary, 1),
            PinnedTool("converter", "Converter", Icons.Default.Calculate, currentTheme.secondary, 2),
            PinnedTool("speedtest", "Speed Test", Icons.Default.Timer, currentTheme.primary, 1),
            PinnedTool("torch", "Torch", Icons.Default.FlashlightOn, SunsetGold, 1),
            PinnedTool("calculator", "Calculator", Icons.Default.Calculate, currentTheme.primary, 2),
        )
    }

    val pinnedTools = remember(pinnedIds, allPinnedTools) {
        val filtered = allPinnedTools.filter { pinnedIds.contains(it.id) }
        if (filtered.isNotEmpty()) filtered else allPinnedTools.take(5)
    }

    val adaptive = com.example.helpyutilities.ui.components.rememberAdaptiveMetrics()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = adaptive.horizontalScreenPadding, vertical = 12.dp)
                .padding(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        // 1. Single Clean Stadium-Pill Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(100.dp))
                .background(currentTheme.cardBackground)
                .border(1.dp, currentTheme.borderColor, RoundedCornerShape(100.dp))
                .clickable {
                    HapticFeedbackHelper.trigger(context)
                    onOpenSearch()
                }
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = currentTheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Search 16+ tools (e.g. loan, unit, pass, level)...",
                    fontSize = 14.sp,
                    color = currentTheme.textSecondary,
                )
            }
        }

        // 2. Smart Clipboard Sense Hero Card
        SmartClipboardBanner(
            onActionTriggered = { action ->
                onNavigateToTab(action.targetTabIndex)
            },
        )

        // 3. Pinned & Recent Strip
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = currentTheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "PINNED & RECENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.textSecondary,
                    letterSpacing = 1.sp,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pinnedTools.forEach { tool ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(currentTheme.cardBackground)
                            .border(1.dp, currentTheme.borderColor, RoundedCornerShape(100.dp))
                            .clickable {
                                HapticFeedbackHelper.trigger(context)
                                onNavigateToTab(tool.tabIndex)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(tool.color.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = tool.icon,
                                    contentDescription = null,
                                    tint = tool.color,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            Text(
                                text = tool.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = currentTheme.textPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        // 4. Bento Grid - Primary Modules
        Text(
            text = "Featured Utilities",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.textPrimary,
            letterSpacing = (-0.3).sp,
        )

        // 2x1 Hero Card 1: Scientific Calculator
        BentoToolCard(
            title = "Scientific Calculator",
            subtitle = "Expressions, loan EMI, tip split & unit conversions",
            icon = Icons.Default.Calculate,
            accentColors = listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
            onClick = { onNavigateToTab(2) },
            badgeText = "Math Studio",
            isWide = true,
        )

        // 2x1 Hero Card 2: Universal Translator
        BentoToolCard(
            title = "Translator",
            subtitle = "110+ languages with Camera OCR & neural AI voice",
            icon = Icons.Default.Translate,
            accentColors = listOf(Color(0xFF10B981), Color(0xFF047857)),
            onClick = { onNavigateToTab(4) },
            badgeText = "Camera & Voice",
            isWide = true,
        )

        // 5. Live Interactive Mini-Widgets on Home (2-Column Bento Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Live Mini Spirit Level Card
            val isLevel = kotlin.math.abs(pitch) < 1.0f && kotlin.math.abs(roll) < 1.0f
            val maxOffset = 22f
            val bubbleX = (-roll / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
            val bubbleY = (-pitch / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)

            FrostedGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        HapticFeedbackHelper.trigger(context)
                        onNavigateToTab(1)
                    },
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Live Level", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                        Text(if (isLevel) "LEVEL ✓" else "${String.format(Locale.US, "%.0f°", roll)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLevel) EmeraldLime else SunsetGold)
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(currentTheme.surfaceContainerHigh)
                            .border(1.dp, if (isLevel) EmeraldLime else currentTheme.borderColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(Color(0x33FFFFFF), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1f)
                            drawLine(Color(0x33FFFFFF), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 1f)
                        }

                        Box(
                            modifier = Modifier
                                .offset(x = bubbleX.dp, y = bubbleY.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isLevel) EmeraldLime else currentTheme.primary),
                        )
                    }
                    Text("Real-time Inclinometer", fontSize = 10.sp, color = currentTheme.textSecondary)
                }
            }

            // Live Pinned Currency Pair Card
            FrostedGlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        HapticFeedbackHelper.trigger(context)
                        onNavigateToTab(3)
                    },
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Live Forex", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(currentTheme.primary.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text("160+", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                        }
                    }

                    Column {
                        Text("1 USD =", fontSize = 11.sp, color = currentTheme.textSecondary)
                        Text(
                            text = "0.92 EUR",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.textPrimary,
                        )
                    }

                    Text("Tap to convert all pairs →", fontSize = 10.sp, color = currentTheme.primary, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 6. Privacy Quick Bar
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = EmeraldLime,
                    modifier = Modifier.size(20.dp),
                )
                Column {
                    Text("100% Offline & Local Device Privacy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    Text("All calculators, clipboard parsing and tools run locally on-device.", fontSize = 11.sp, color = currentTheme.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}
}
