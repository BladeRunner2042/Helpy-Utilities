package com.example.helpyutilities.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.components.KeepAwakePill
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.ui.theme.tabularNumberStyle
import com.example.helpyutilities.util.HapticFeedbackHelper
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CityZone(
    val cityName: String,
    val country: String,
    val zoneIdString: String,
    val flag: String
)

@Composable
fun WorldTimeScrubberScreen(
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current

    // Current instant updated every minute
    var baseInstant by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            baseInstant = Instant.now()
        }
    }

    // Offset in 15-minute intervals: -48 to +48 (-12h to +12h)
    // 1 step = 15 minutes = 900 seconds
    var sliderStep by remember { mutableFloatStateOf(0f) }

    val cities = remember {
        listOf(
            CityZone("Johannesburg", "South Africa", "Africa/Johannesburg", "ZA"),
            CityZone("London", "United Kingdom", "Europe/London", "GB"),
            CityZone("New York", "United States", "America/New_York", "US"),
            CityZone("Dubai", "United Arab Emirates", "Asia/Dubai", "AE"),
            CityZone("Tokyo", "Japan", "Asia/Tokyo", "JP"),
            CityZone("Sydney", "Australia", "Australia/Sydney", "AU"),
        )
    }

    val offsetMinutes = (sliderStep.toInt()) * 15
    val offsetHoursFraction = offsetMinutes / 60.0
    val offsetDisplay = when {
        offsetMinutes == 0 -> "Now (0h)"
        offsetMinutes > 0 -> String.format(Locale.US, "+%.2fh (+%d min)", offsetHoursFraction, offsetMinutes)
        else -> String.format(Locale.US, "%.2fh (%d min)", offsetHoursFraction, offsetMinutes)
    }

    val targetInstant = remember(baseInstant, offsetMinutes) {
        baseInstant.plusSeconds(offsetMinutes * 60L)
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val timeShortFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "World Time-Zone Scrubber",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.textPrimary
                )
                Text(
                    text = "Offline Interactive 15-Min Time Conversion",
                    fontSize = 12.sp,
                    color = currentTheme.textSecondary
                )
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

        // Time Slider Control Card
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = currentTheme.primary, modifier = Modifier.size(20.dp))
                        Text("Scrub Time Offset", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = currentTheme.textPrimary)
                    }

                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            sliderStep = 0f
                            baseInstant = Instant.now()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sliderStep == 0f) currentTheme.surfaceContainerHigh else currentTheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = if (sliderStep == 0f) currentTheme.textSecondary else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (sliderStep == 0f) currentTheme.textSecondary else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("-12 hours", fontSize = 12.sp, color = currentTheme.textSecondary)
                    Text(
                        text = offsetDisplay,
                        style = tabularNumberStyle(size = 14, fontWeight = FontWeight.Bold, color = if (sliderStep == 0f) EmeraldLime else currentTheme.primary)
                    )
                    Text("+12 hours", fontSize = 12.sp, color = currentTheme.textSecondary)
                }

                Slider(
                    value = sliderStep,
                    onValueChange = {
                        sliderStep = it
                        HapticFeedbackHelper.triggerTick(context)
                    },
                    valueRange = -48f..48f,
                    steps = 95, // 96 steps of 15 min between -48 and +48
                    colors = SliderDefaults.colors(
                        thumbColor = currentTheme.primary,
                        activeTrackColor = currentTheme.primary,
                        inactiveTrackColor = currentTheme.surfaceContainerHighest
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Live Converted Clocks
        Text(
            text = "Converted World Clocks (${cities.size} Major Cities)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = currentTheme.textSecondary
        )

        cities.forEach { city ->
            val zoneId = remember(city.zoneIdString) { ZoneId.of(city.zoneIdString) }
            val cityZonedDateTime = remember(targetInstant, zoneId) {
                targetInstant.atZone(zoneId)
            }
            val timeString = remember(cityZonedDateTime) { cityZonedDateTime.format(timeFormatter) }
            val dateString = remember(cityZonedDateTime) { cityZonedDateTime.format(dateFormatter) }
            val isDay = cityZonedDateTime.hour in 6..18

            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticFeedbackHelper.trigger(context)
                        val copyText = "${city.cityName} (${city.country}): $timeString • $dateString"
                        clipboardManager.setText(AnnotatedString(copyText))
                        Toast.makeText(context, "Copied ${city.cityName} time!", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(currentTheme.surfaceContainerHigh)
                                .border(1.dp, currentTheme.borderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = city.flag,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = currentTheme.primary
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = city.cityName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTheme.textPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDay) SunsetGold.copy(alpha = 0.18f) else currentTheme.surfaceContainerHighest)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isDay) "Day" else "Night",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDay) SunsetGold else currentTheme.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = "$dateString • ${city.country}",
                                fontSize = 11.sp,
                                color = currentTheme.textSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = timeString,
                            style = tabularNumberStyle(size = 18, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                        )
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = currentTheme.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
