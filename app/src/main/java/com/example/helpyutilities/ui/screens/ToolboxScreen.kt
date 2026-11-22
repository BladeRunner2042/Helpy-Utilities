package com.example.helpyutilities.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.example.helpyutilities.ui.components.BatteryTelemetrySparklineCard
import com.example.helpyutilities.ui.components.LiveCpuCoresCard
import com.example.helpyutilities.ui.components.MemoryAndStorageRingsCard
import com.example.helpyutilities.util.HardwareTelemetryHelper
import com.example.helpyutilities.util.HardwareTelemetrySnapshot
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.AmberGlow
import com.example.helpyutilities.ui.theme.DarkBg
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.NeonRose
import com.example.helpyutilities.ui.theme.RoyalViolet
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.util.HapticFeedbackHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

data class ToolboxCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScreen() {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val adaptive = com.example.helpyutilities.ui.components.rememberAdaptiveMetrics()
    var selectedCategory by remember { mutableStateOf("security") }

    val categories = listOf(
        ToolboxCategory("security", "Passwords & PIN", Icons.Default.Lock, EmeraldLime),
        ToolboxCategory("text", "Text & Dev", Icons.Default.Code, ElectricCyan),
        ToolboxCategory("device", "Device & Sensors", Icons.Default.Sensors, SunsetGold),
        
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(horizontal = adaptive.horizontalScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Everyday Toolbox",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.textPrimary
                )
                Text(
                    text = "Swiss-army knife of high-utility tools for daily life",
                    fontSize = 13.sp,
                    color = currentTheme.textSecondary
                )
            }

            // Horizontal Category Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.id,
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            selectedCategory = cat.id
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(cat.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = currentTheme.primary,
                            selectedLabelColor = currentTheme.onPrimary,
                            selectedLeadingIconColor = currentTheme.onPrimary,
                            containerColor = currentTheme.surfaceContainerHigh,
                            labelColor = currentTheme.textPrimary,
                            iconColor = if (selectedCategory == cat.id) currentTheme.onPrimary else cat.color
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedCategory) {
                    "security" -> SecurityToolboxView()
                    "text" -> TextDeveloperToolboxView()
                    "device" -> DeviceSensorToolboxView()
                    // Removed PhotoPrivacyAndCompressorScreen
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 1. SECURITY & PASSWORDS TOOLBOX
// -------------------------------------------------------------------------------------------------
@Composable
fun SecurityToolboxView() {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current

    var passwordLength by remember { mutableFloatStateOf(16f) }
    var incUpper by remember { mutableStateOf(true) }
    var incLower by remember { mutableStateOf(true) }
    var incNumbers by remember { mutableStateOf(true) }
    var incSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }

    var pinLength by remember { mutableIntStateOf(6) }
    var generatedPin by remember { mutableStateOf("") }

    fun generatePass() {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val nums = "0123456789"
        val syms = "!@#$%^&*()-_=+[]{}|;:,.<>?"

        var pool = ""
        if (incUpper) pool += upper
        if (incLower) pool += lower
        if (incNumbers) pool += nums
        if (incSymbols) pool += syms
        if (pool.isEmpty()) pool = lower + nums

        val len = passwordLength.toInt()
        val sb = StringBuilder()
        repeat(len) {
            sb.append(pool[Random.nextInt(pool.length)])
        }
        generatedPassword = sb.toString()
    }

    fun generatePinCode() {
        val sb = StringBuilder()
        repeat(pinLength) {
            sb.append(Random.nextInt(10))
        }
        generatedPin = sb.toString()
    }

    LaunchedEffect(Unit) {
        generatePass()
        generatePinCode()
    }

    val strength = when {
        (passwordLength >= 16 && incUpper && incLower && incNumbers && incSymbols) -> "Very Strong 🔒" to EmeraldLime
        (passwordLength >= 12 && (incNumbers || incSymbols)) -> "Strong 🛡️" to ElectricCyan
        (passwordLength >= 8) -> "Moderate ⚠️" to SunsetGold
        else -> "Weak ❌" to NeonRose
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Password Generator Card
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure Password Generator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    Text(strength.first, fontSize = 12.sp, color = strength.second, fontWeight = FontWeight.Bold)
                }

                // Password Display Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(currentTheme.surfaceContainerHigh)
                        .border(1.dp, currentTheme.borderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = generatedPassword,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldLime,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(onClick = {
                                HapticFeedbackHelper.trigger(context)
                                generatePass()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = ElectricCyan)
                            }
                            IconButton(onClick = {
                                HapticFeedbackHelper.trigger(context)
                                clipboardManager.setText(AnnotatedString(generatedPassword))
                                Toast.makeText(context, "Password copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = currentTheme.textPrimary)
                            }
                        }
                    }
                }

                // Length Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Length: ${passwordLength.toInt()} characters", fontSize = 13.sp, color = currentTheme.textSecondary)
                }
                Slider(
                    value = passwordLength,
                    onValueChange = {
                        passwordLength = it
                        generatePass()
                    },
                    valueRange = 6f..36f,
                    steps = 29,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldLime,
                        activeTrackColor = EmeraldLime,
                        inactiveTrackColor = currentTheme.surfaceContainerHighest
                    )
                )

                // Switches
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ToggleOption("Uppercase (A-Z)", incUpper) { incUpper = it; generatePass() }
                    ToggleOption("Lowercase (a-z)", incLower) { incLower = it; generatePass() }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ToggleOption("Numbers (0-9)", incNumbers) { incNumbers = it; generatePass() }
                    ToggleOption("Symbols (!@#$)", incSymbols) { incSymbols = it; generatePass() }
                }
            }
        }

        // Quick PIN Generator Card
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Secure PIN Generator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4, 6, 8).forEach { len ->
                        FilterChip(
                            selected = pinLength == len,
                            onClick = {
                                pinLength = len
                                generatePinCode()
                            },
                            label = { Text("$len Digits") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = currentTheme.primary,
                                selectedLabelColor = currentTheme.onPrimary,
                                containerColor = currentTheme.surfaceContainerHigh,
                                labelColor = currentTheme.textPrimary
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(currentTheme.surfaceContainerHigh)
                        .border(1.dp, currentTheme.borderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = generatedPin.chunked(pinLength / 2).joinToString(" - "),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan
                        )
                        Row {
                            IconButton(onClick = {
                                HapticFeedbackHelper.trigger(context)
                                generatePinCode()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = ElectricCyan)
                            }
                            IconButton(onClick = {
                                HapticFeedbackHelper.trigger(context)
                                clipboardManager.setText(AnnotatedString(generatedPin))
                                Toast.makeText(context, "PIN copied!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = currentTheme.textPrimary)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
fun ToggleOption(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmeraldLime,
                checkedTrackColor = EmeraldLime.copy(alpha = 0.4f),
                uncheckedThumbColor = currentTheme.textSecondary,
                uncheckedTrackColor = currentTheme.surfaceContainerHighest,
            )
        )
        Text(title, fontSize = 12.sp, color = currentTheme.textPrimary)
    }
}

// -------------------------------------------------------------------------------------------------
// 2. TEXT & DEVELOPER UTILITIES
// -------------------------------------------------------------------------------------------------
@Composable
fun TextDeveloperToolboxView() {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("Helpy Utilities makes everyday tasks effortless!") }
    var selectedTool by remember { mutableIntStateOf(0) }

    val tools = listOf("Case Converter", "Word Counter", "Base64 & Hash", "UUID & URL")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
        ) {
            items(tools.indices.toList()) { index ->
                FilterChip(
                    selected = selectedTool == index,
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        selectedTool = index
                    },
                    label = { Text(tools[index], fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentTheme.primary,
                        selectedLabelColor = currentTheme.onPrimary,
                        containerColor = currentTheme.surfaceContainerHigh,
                        labelColor = currentTheme.textPrimary
                    )
                )
            }
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Input Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = currentTheme.borderColor
            )
        )

        when (selectedTool) {
            0 -> {
                // Case Converter
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Case Transformations", fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)

                        CaseItemRow("UPPERCASE", inputText.uppercase()) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("lowercase", inputText.lowercase()) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("Title Case", inputText.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("camelCase", inputText.split(Regex("[\\s_-]+")).mapIndexed { i, s -> if (i == 0) s.lowercase() else s.lowercase().replaceFirstChar { it.uppercase() } }.joinToString("")) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("snake_case", inputText.trim().replace(Regex("[\\s-]+"), "_").lowercase()) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("kebab-case", inputText.trim().replace(Regex("[\\s_]+"), "-").lowercase()) { clipboardManager.setText(AnnotatedString(it)) }
                    }
                }
            }
            1 -> {
                // Word Counter
                val words = if (inputText.isBlank()) 0 else inputText.trim().split(Regex("\\s+")).size
                val chars = inputText.length
                val charsNoSpaces = inputText.count { !it.isWhitespace() }
                val lines = if (inputText.isEmpty()) 0 else inputText.lines().size
                val readTimeSec = (words / 3.3).toInt()

                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Text Analytics & Metrics", fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricBox("Words", "$words", EmeraldLime)
                            MetricBox("Characters", "$chars", ElectricCyan)
                            MetricBox("No Spaces", "$charsNoSpaces", SunsetGold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricBox("Lines", "$lines", RoyalViolet)
                            MetricBox("Reading Time", "${readTimeSec}s", NeonRose)
                            MetricBox("Paragraphs", "${inputText.split(Regex("\n\n+")).filter { it.isNotBlank() }.size}", EmeraldLime)
                        }
                    }
                }
            }
            2 -> {
                // Base64 & Hash + Hasher
                val base64Encoded = android.util.Base64.encodeToString(inputText.toByteArray(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP)
                val base64Decoded = try {
                    String(android.util.Base64.decode(inputText, android.util.Base64.DEFAULT), StandardCharsets.UTF_8)
                } catch (_: Exception) { "Invalid Base64" }

                val md5Hash = hashString("MD5", inputText)
                val sha1Hash = hashString("SHA-1", inputText)
                val sha256Hash = hashString("SHA-256", inputText)

                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Encodings & Cryptographic Hashes", fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)

                        CaseItemRow("Base64 Encoded", base64Encoded) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("Base64 Decoded", base64Decoded) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("MD5 Hash", md5Hash) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("SHA-1 Hash", sha1Hash) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("SHA-256 Hash", sha256Hash) { clipboardManager.setText(AnnotatedString(it)) }
                    }
                }
            }
            3 -> {
                // UUID, URL & Tracking Cleaner
                var generatedUuid by remember { mutableStateOf(UUID.randomUUID().toString()) }
                val urlEncoded = try { URLEncoder.encode(inputText, "UTF-8") } catch (_: Exception) { "" }
                val urlDecoded = try { URLDecoder.decode(inputText, "UTF-8") } catch (_: Exception) { "" }

                // Strip tracking parameters: utm_*, fbclid, gclid, si, igshid
                fun stripTracking(url: String): String {
                    return try {
                        val uri = android.net.Uri.parse(url)
                        if (uri.isHierarchical) {
                            val trackingParams = setOf("fbclid", "gclid", "si", "igshid")
                            val cleanUriBuilder = uri.buildUpon().clearQuery()
                            for (paramName in uri.queryParameterNames) {
                                val lower = paramName.lowercase()
                                if (!lower.startsWith("utm_") && !trackingParams.contains(lower)) {
                                    for (value in uri.getQueryParameters(paramName)) {
                                        cleanUriBuilder.appendQueryParameter(paramName, value)
                                    }
                                }
                            }
                            cleanUriBuilder.build().toString()
                        } else {
                            url
                        }
                    } catch (_: Exception) {
                        url
                    }
                }

                val cleanedUrl = remember(inputText) { stripTracking(inputText) }

                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("URL Cleaner & UUID Tools", fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                            Button(
                                onClick = { generatedUuid = UUID.randomUUID().toString() },
                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh)
                            ) { Text("New UUID", fontSize = 12.sp, color = ElectricCyan) }
                        }

                        if (inputText.startsWith("http://") || inputText.startsWith("https://")) {
                            CaseItemRow("Cleaned URL (Tracking Stripped)", cleanedUrl) { clipboardManager.setText(AnnotatedString(it)) }
                        }

                        CaseItemRow("UUID v4", generatedUuid) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("URL Encoded", urlEncoded) { clipboardManager.setText(AnnotatedString(it)) }
                        CaseItemRow("URL Decoded", urlDecoded) { clipboardManager.setText(AnnotatedString(it)) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(96.dp))
    }
}

private fun hashString(algorithm: String, input: String): String {
    return try {
        val digest = MessageDigest.getInstance(algorithm)
        val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        hash.joinToString("") { "%02x".format(it) }
    } catch (_: Exception) { "Error" }
}

@Composable
fun CaseItemRow(title: String, value: String, onCopy: (String) -> Unit) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(currentTheme.surfaceContainerHigh)
            .clickable {
                HapticFeedbackHelper.trigger(context)
                onCopy(value)
                Toast.makeText(context, "Copied $title!", Toast.LENGTH_SHORT).show()
            }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 11.sp, color = currentTheme.textSecondary)
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = currentTheme.textSecondary, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = currentTheme.textPrimary,
            fontFamily = FontFamily.Monospace,
            maxLines = 2
        )
    }
}

@Composable
fun MetricBox(label: String, value: String, color: Color) {
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(currentTheme.surfaceContainerHigh)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = currentTheme.textSecondary)
    }
}

// -------------------------------------------------------------------------------------------------
// 3. DEVICE & HARDWARE SENSORS TOOLBOX (Live Info & Dynamic Graphs)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSensorToolboxView() {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    var showSpeedTestModal by remember { mutableStateOf(false) }
    var showRulerModal by remember { mutableStateOf(false) }
    var showSoundMeterModal by remember { mutableStateOf(false) }
    var showWorldTimeModal by remember { mutableStateOf(false) }

    // Lifecycle-aware foreground check to pause accelerometer and hardware polling in background
    val isAppForeground = com.example.helpyutilities.ui.components.rememberIsAppForeground()

    // Accelerometer Level
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

    // Dynamic Live RAM sampling & history graph
    val actManager = remember { context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager }
    var ramUsedGb by remember { mutableFloatStateOf(0f) }
    var ramTotalGb by remember { mutableFloatStateOf(1f) }
    var ramPercent by remember { mutableIntStateOf(0) }
    val ramHistory = remember { mutableStateListOf<Float>() }

    // Dynamic Live Battery & Thermal stats
    var batteryPct by remember { mutableIntStateOf(100) }
    var batteryTempC by remember { mutableFloatStateOf(0f) }
    var batteryVoltageMv by remember { mutableIntStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }
    var chargingType by remember { mutableStateOf("On Battery") }
    var thermalStatus by remember { mutableStateOf("Optimal (Normal)") }

    // Enhanced Live Hardware Telemetry State
    var telemetrySnapshot by remember {
        mutableStateOf(HardwareTelemetryHelper.collectSnapshot(context))
    }
    val currentMaHistory = remember { mutableStateListOf<Float>() }
    val tempCelsiusHistory = remember { mutableStateListOf<Float>() }

    // Real-time polling loop (3s telemetry loop + 1s RAM sampling)
    LaunchedEffect(isAppForeground) {
        if (!isAppForeground) return@LaunchedEffect
        var tickCount = 0
        while (true) {
            // Sample RAM
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            val total = memInfo.totalMem / (1024f * 1024f * 1024f)
            val avail = memInfo.availMem / (1024f * 1024f * 1024f)
            val used = (total - avail).coerceAtLeast(0f)
            val pct = if (total > 0) ((used / total) * 100).toInt() else 0

            ramTotalGb = total
            ramUsedGb = used
            ramPercent = pct

            if (ramHistory.size >= 24) {
                ramHistory.removeAt(0)
            }
            ramHistory.add(pct.toFloat())

            // Sample Battery & Voltage
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val rawLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val rawScale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (rawLevel >= 0 && rawScale > 0) {
                batteryPct = ((rawLevel.toFloat() / rawScale.toFloat()) * 100).toInt()
            }
            val rawTemp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (rawTemp > 0) {
                batteryTempC = rawTemp / 10.0f
            }
            batteryVoltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val chargePlug = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            chargingType = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                BatteryManager.BATTERY_PLUGGED_AC -> "Fast AC Charger"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                else -> if (isCharging) "Charging" else "On Battery"
            }

            // Sample Thermal State
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                thermalStatus = when (powerManager?.currentThermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> "Normal (Optimal)"
                    PowerManager.THERMAL_STATUS_LIGHT -> "Light Throttling"
                    PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Throttling"
                    PowerManager.THERMAL_STATUS_SEVERE -> "Severe Throttling"
                    PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Heat"
                    PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Cooldown"
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal Shutdown"
                    else -> "Optimal Conditions"
                }
            }

            // Update 3s telemetry snapshot and rolling 20-sample histories
            if (tickCount % 3 == 0) {
                val snapshot = HardwareTelemetryHelper.collectSnapshot(context)
                telemetrySnapshot = snapshot

                if (currentMaHistory.size >= 20) {
                    currentMaHistory.removeAt(0)
                }
                currentMaHistory.add(snapshot.battery.currentNowMa.toFloat())

                if (tempCelsiusHistory.size >= 20) {
                    tempCelsiusHistory.removeAt(0)
                }
                tempCelsiusHistory.add(snapshot.battery.tempCelsius)
            }
            tickCount++

            delay(1000)
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Honest & Free Cloudflare Network Speed Test Card
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                        Text("Cloudflare Speed Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Zero Cost • Honest", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Measure real-time Ping, Jitter, Download & Upload throughput using free Cloudflare edge CDN endpoints.",
                    fontSize = 12.sp,
                    color = currentTheme.textSecondary
                )
                Button(
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        showSpeedTestModal = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Speed Test", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.sp)
                }
            }
        }

        // Quick Launch Row for Hands-Free Tools: Screen Ruler, Acoustic Meter & World Time Scrubber
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Zero-Permission Hands-Free Utilities", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            showRulerModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(44.dp)
                    ) {
                        Icon(Icons.Default.Straighten, contentDescription = null, tint = EmeraldLime, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ruler", color = currentTheme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            showSoundMeterModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(44.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("dB Meter", color = currentTheme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            showWorldTimeModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(44.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("World Time", color = currentTheme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Morse Code & SOS Torch Transmitter Card
        var morseInputText by remember { mutableStateOf("SOS") }
        var isTransmitting by remember { mutableStateOf(false) }
        var currentMorseSymbol by remember { mutableStateOf(' ') }
        var currentMorseIndex by remember { mutableIntStateOf(-1) }
        var morseJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
        val morseScope = rememberCoroutineScope()

        val morseCodeString = remember(morseInputText) {
            com.example.helpyutilities.util.MorseTorchHelper.textToMorse(morseInputText)
        }

        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = null, tint = SunsetGold, modifier = Modifier.size(20.dp))
                        Text("Morse Code & SOS Torch", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTransmitting) NeonRose.copy(alpha = 0.2f) else SunsetGold.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isTransmitting) "TRANSMITTING" else "Camera + Haptics",
                            fontSize = 11.sp,
                            color = if (isTransmitting) NeonRose else SunsetGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Transmit light flashes and tactile vibrations for SOS (··· --- ···) or any custom text via camera torch.",
                    fontSize = 12.sp,
                    color = currentTheme.textSecondary
                )

                OutlinedTextField(
                    value = morseInputText,
                    onValueChange = { morseInputText = it },
                    label = { Text("Text or Message to Transmit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunsetGold,
                        unfocusedBorderColor = currentTheme.borderColor
                    )
                )

                // Morse Code Representation Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(currentTheme.surfaceContainerHigh)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Morse Sequence:", fontSize = 11.sp, color = currentTheme.textSecondary)
                            Text(
                                text = morseCodeString.ifEmpty { "(Empty)" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunsetGold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        if (isTransmitting) {
                            Text(
                                text = "$currentMorseSymbol",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonRose
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick SOS button
                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            morseInputText = "SOS"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 84.dp).height(44.dp)
                    ) {
                        Text("SOS Preset", color = SunsetGold, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }

                    // Start / Stop Transmission button
                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            if (isTransmitting) {
                                morseJob?.cancel()
                                isTransmitting = false
                                currentMorseIndex = -1
                            } else {
                                if (morseCodeString.isNotBlank()) {
                                    isTransmitting = true
                                    morseJob = morseScope.launch {
                                        try {
                                            com.example.helpyutilities.util.MorseTorchHelper.transmitMorse(
                                                context = context,
                                                morseSequence = morseCodeString,
                                                onSymbolProgress = { sym, idx ->
                                                    currentMorseSymbol = sym
                                                    currentMorseIndex = idx
                                                }
                                            )
                                        } finally {
                                            isTransmitting = false
                                            currentMorseIndex = -1
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTransmitting) MaterialTheme.colorScheme.error else SunsetGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isTransmitting) Icons.Default.Stop else Icons.Default.FlashlightOn,
                            contentDescription = null,
                            tint = if (isTransmitting) Color.White else currentTheme.background,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTransmitting) "Stop Transmission" else "Transmit Torch",
                            fontWeight = FontWeight.Bold,
                            color = if (isTransmitting) Color.White else currentTheme.background,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 1. Live Memory & Storage Dual Progress Rings Card
        MemoryAndStorageRingsCard(metrics = telemetrySnapshot.memoryAndStorage)

        // 2. Battery & Power Dynamics Telemetry Sparkline Card
        BatteryTelemetrySparklineCard(
            battery = telemetrySnapshot.battery,
            currentHistoryMa = currentMaHistory,
            tempHistoryCelsius = tempCelsiusHistory,
        )



        // 3. Live Per-Core CPU Frequency Bars Card
        LiveCpuCoresCard(cores = telemetrySnapshot.cpuCores)

        // 4. Real-Time Bubble Level & Inclinometer Graph
        val isLevel = kotlin.math.abs(pitch) < 1.0f && kotlin.math.abs(roll) < 1.0f
        val circleColor = if (isLevel) EmeraldLime else currentTheme.primary

        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = currentTheme.secondary, modifier = Modifier.size(20.dp))
                        Text("Inclinometer & Surface Level", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    }
                    com.example.helpyutilities.ui.components.KeepAwakePill()
                }
                Text("Gyroscope & Accelerometer live motion tracking", fontSize = 12.sp, color = currentTheme.textSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(currentTheme.surfaceContainerHigh)
                        .border(2.dp, if (isLevel) EmeraldLime else currentTheme.borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(Color(0x33FFFFFF), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1f)
                        drawLine(Color(0x33FFFFFF), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 1f)
                    }

                    val maxOffset = 52f
                    val bubbleX = (-roll / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
                    val bubbleY = (-pitch / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)

                    Box(
                        modifier = Modifier
                            .offset(x = bubbleX.dp, y = bubbleY.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(circleColor.copy(alpha = 0.85f))
                            .border(2.dp, circleColor, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Pitch: ${String.format(Locale.US, "%.1f°", pitch)}", fontWeight = FontWeight.Bold, color = currentTheme.primary, fontSize = 13.sp)
                    Text(if (isLevel) "LEVEL ✓" else "Tilt: ${String.format(Locale.US, "%.1f°", roll)}", fontWeight = FontWeight.Bold, color = if (isLevel) EmeraldLime else currentTheme.textSecondary, fontSize = 13.sp)
                    Text("Roll: ${String.format(Locale.US, "%.1f°", roll)}", fontWeight = FontWeight.Bold, color = SunsetGold, fontSize = 13.sp)
                }
            }
        }

        // 5. CPU & SoC Architecture Card
        val cpuCores = remember { Runtime.getRuntime().availableProcessors() }
        val cpuArch = remember { Build.SUPPORTED_ABIS.joinToString(", ") }
        val socHardware = remember { Build.HARDWARE }
        val socBoard = remember { Build.BOARD }
        val glEsVersion = remember { actManager?.deviceConfigurationInfo?.glEsVersion ?: "OpenGL ES 3.2" }
        val displayMetrics = remember { context.resources.displayMetrics }
        val refreshRate = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.refreshRate?.toInt() ?: 60
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager)?.defaultDisplay?.refreshRate?.toInt() ?: 60
            }
        }

        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = currentTheme.secondary, modifier = Modifier.size(20.dp))
                    Text("Processor & Display Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CPU Architecture:", color = currentTheme.textSecondary)
                    Text("$cpuCores Cores ($cpuArch)", color = currentTheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SoC Hardware / Board:", color = currentTheme.textSecondary)
                    Text("$socHardware ($socBoard)", color = currentTheme.textPrimary, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Display Refresh Rate:", color = currentTheme.textSecondary)
                    Text("$refreshRate Hz High-FPS", color = EmeraldLime, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Resolution & Density:", color = currentTheme.textSecondary)
                    Text("${displayMetrics.widthPixels} × ${displayMetrics.heightPixels} (${displayMetrics.densityDpi} DPI)", color = currentTheme.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Graphics Driver / GL:", color = currentTheme.textSecondary)
                    Text("GL ES $glEsVersion", color = currentTheme.secondary, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Android System:", color = currentTheme.textSecondary)
                    Text("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", color = EmeraldLime, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showSpeedTestModal) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedTestModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                NetworkSpeedTestScreen()
            }
        }
    }

    if (showRulerModal) {
        ModalBottomSheet(
            onDismissRequest = { showRulerModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                ScreenRulerScreen(onDismiss = { showRulerModal = false })
            }
        }
    }

    if (showSoundMeterModal) {
        ModalBottomSheet(
            onDismissRequest = { showSoundMeterModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                AcousticDecibelMeterScreen(onDismiss = { showSoundMeterModal = false })
            }
        }
    }

    if (showWorldTimeModal) {
        ModalBottomSheet(
            onDismissRequest = { showWorldTimeModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                WorldTimeScrubberScreen(onDismiss = { showWorldTimeModal = false })
            }
        }
    }
}
