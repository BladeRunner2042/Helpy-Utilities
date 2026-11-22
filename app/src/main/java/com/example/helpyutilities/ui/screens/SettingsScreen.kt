package com.example.helpyutilities.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.helpyutilities.widget.HelpyHomeWidgetBridge
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.data.AppThemeMode
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.DarkBg
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.util.HapticFeedbackHelper
import com.example.helpyutilities.util.UpdateChecker
import com.example.helpyutilities.util.UpdateResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedTheme by AppPreferences.themeMode.collectAsState()
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val hapticsEnabled by AppPreferences.hapticsEnabled.collectAsState()
    val decimalPrecision by AppPreferences.decimalPrecision.collectAsState()
    val autoUpdateCheck by AppPreferences.autoUpdateCheck.collectAsState()
    val smartClipboardEnabled by AppPreferences.smartClipboardEnabled.collectAsState()

    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    var showPrivacySheet by remember { mutableStateOf(false) }

    // Multi-Permission Request (Notification, Camera, Microphone)
    val multiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val grantedCount = permissions.values.count { it }
        Toast.makeText(context, "Permissions updated: $grantedCount granted", Toast.LENGTH_SHORT).show()
    }

    // Notification Permission Request (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Update notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Theme Selector Card with 2x3 Visual Swatches
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = currentTheme.primary)
                        Text("Theme & Visual Style", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    }

                    Text("Select from 6 clean Material You themes:", fontSize = 12.sp, color = currentTheme.textSecondary)

                    // 2x3 Grid of Visual Swatches
                    val themeModes = AppThemeMode.values()
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                for (col in 0..1) {
                                    val mode = themeModes[row * 2 + col]
                                    val isSelected = selectedTheme == mode

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(mode.cardBackground)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) currentTheme.primary else mode.borderColor,
                                                shape = RoundedCornerShape(14.dp),
                                            )
                                            .clickable {
                                                HapticFeedbackHelper.trigger(context)
                                                AppPreferences.setTheme(mode)
                                                HelpyHomeWidgetBridge.syncTheme(
                                                    context = context,
                                                    bgHex = String.format("#%08X", mode.background.toArgb()),
                                                    primaryHex = String.format("#%08X", mode.primary.toArgb()),
                                                    textHex = String.format("#%08X", mode.textPrimary.toArgb()),
                                                )
                                            }
                                            .padding(12.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            // Mini Palette Swatch Preview
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(mode.background)
                                                    .border(1.dp, mode.borderColor, CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(mode.primary),
                                                )
                                            }

                                            Text(
                                                text = mode.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) currentTheme.primary else mode.textPrimary,
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Preferences Group (Switches & Segmented Buttons)
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("Preferences", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)

                    // Smart Clipboard Sense Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = currentTheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Smart Clipboard Sense", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                Text("Suggests instant actions for copied values", fontSize = 11.sp, color = currentTheme.textSecondary)
                            }
                        }
                        Switch(
                            checked = smartClipboardEnabled,
                            onCheckedChange = {
                                HapticFeedbackHelper.trigger(context)
                                AppPreferences.setSmartClipboardEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = currentTheme.primary,
                                uncheckedThumbColor = currentTheme.textSecondary,
                                uncheckedTrackColor = currentTheme.surfaceContainerHigh,
                            ),
                        )
                    }

                    // Tactile Haptics Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = currentTheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Tactile Micro-Haptics", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                Text("Light haptic pulse on clicks and keypresses", fontSize = 11.sp, color = currentTheme.textSecondary)
                            }
                        }
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = {
                                HapticFeedbackHelper.trigger(context)
                                AppPreferences.setHapticsEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = currentTheme.primary,
                                uncheckedThumbColor = currentTheme.textSecondary,
                                uncheckedTrackColor = currentTheme.surfaceContainerHigh,
                            ),
                        )
                    }

                    // Decimal Precision Segmented Buttons (2, 4, 8)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Decimal Precision", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                            Text("Maximum decimals in calculations", fontSize = 11.sp, color = currentTheme.textSecondary)
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(currentTheme.surfaceContainerHigh)
                                .border(1.dp, currentTheme.borderColor, RoundedCornerShape(100.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            listOf(2, 4, 8).forEach { prec ->
                                val isSelected = decimalPrecision == prec
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isSelected) currentTheme.primary else Color.Transparent)
                                        .clickable {
                                            HapticFeedbackHelper.trigger(context)
                                            AppPreferences.setDecimalPrecision(prec)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$prec",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else currentTheme.textSecondary,
                                    )
                                }
                            }
                        }
                    }

                    // Release Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = currentTheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Release Notifications", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                Text("Notify when new releases are posted", fontSize = 11.sp, color = currentTheme.textSecondary)
                            }
                        }

                        Switch(
                            checked = autoUpdateCheck,
                            onCheckedChange = { enabled ->
                                HapticFeedbackHelper.trigger(context)
                                AppPreferences.setAutoUpdateCheck(enabled)
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = currentTheme.primary,
                                uncheckedThumbColor = currentTheme.textSecondary,
                                uncheckedTrackColor = currentTheme.surfaceContainerHigh,
                            ),
                        )
                    }
                }
            }
        }

        // 3. Community, Legal & Permissions Card
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Community & Permissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.textPrimary
                    )

                    // Helpy Support (Telegram)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                HapticFeedbackHelper.trigger(context)
                                try {
                                    val telegramIntent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://t.me/+NPNfd7njpN44MDFk")
                                    ).apply {
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(telegramIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open Telegram", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(currentTheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = currentTheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Helpy Support", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                Text("Join our official Telegram community for help & ideas", fontSize = 11.sp, color = currentTheme.textSecondary)
                            }
                        }
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = currentTheme.textSecondary, modifier = Modifier.size(18.dp))
                    }

                    // Zero-Data Privacy Policy Tile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                HapticFeedbackHelper.trigger(context)
                                showPrivacySheet = true
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldLime.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = EmeraldLime, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Privacy Policy", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                Text("Zero data recorded, logged, or shared with third parties", fontSize = 11.sp, color = currentTheme.textSecondary)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = currentTheme.textSecondary, modifier = Modifier.size(20.dp))
                    }

                    // Permissions & Notifications Manager
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                HapticFeedbackHelper.trigger(context)
                                val permissionsToRequest = mutableListOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                multiPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(currentTheme.secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = currentTheme.secondary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("App Permissions & Alerts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                Text("Manage Camera (OCR/QR), Audio (dB Meter) & Alerts", fontSize = 11.sp, color = currentTheme.textSecondary)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                HapticFeedbackHelper.trigger(context)
                                val permissionsToRequest = mutableListOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                multiPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                            },
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = currentTheme.primary)
                        }
                    }
                }
            }
        }

        // 4. Updates & Version Footer
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Helpy Utilities", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                            Text("Version 1.0.0", fontSize = 12.sp, color = currentTheme.textSecondary)
                        }

                        OutlinedButton(
                            onClick = {
                                HapticFeedbackHelper.trigger(context)
                                isCheckingUpdates = true
                                coroutineScope.launch {
                                    val res = UpdateChecker.check("1.0")
                                    updateResult = res
                                    isCheckingUpdates = false
                                    if (res.hasUpdate) {
                                        Toast.makeText(context, "New version ${res.latestVersion} available!", Toast.LENGTH_SHORT).show()
                                        com.example.helpyutilities.util.NotificationHelper.showLocalAlert(
                                            context = context,
                                            title = "Helpy Update: v${res.latestVersion}",
                                            body = "A new release of Helpy Utilities is available to install.",
                                            tabIndex = 0
                                        )
                                    } else {
                                        Toast.makeText(context, "You are using the latest version", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isCheckingUpdates,
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = currentTheme.primary,
                            ),
                        ) {
                            if (isCheckingUpdates) {
                                CircularProgressIndicator(color = currentTheme.primary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check Updates", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldLime, modifier = Modifier.size(16.dp))
                        Text("100% Offline & Local Privacy", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = currentTheme.textSecondary)
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Zero-Data Privacy Policy Bottom Sheet
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldLime, modifier = Modifier.size(24.dp))
                        Text("Privacy Policy — Zero Data Logged", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    }
                    IconButton(onClick = { showPrivacySheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = currentTheme.textSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldLime.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "100% Local Processing & Zero Tracking Guarantee",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLime
                    )
                }

                Text(
                    text = "1. No Tracking or Analytics: Helpy Utilities does not collect, record, log, store on remote servers, or sell any personal data, device identifiers, clipboard contents, or usage analytics.\n\n" +
                            "2. 100% On-Device Processing: Calculations, clipboard checks, camera OCR scans, QR codes, passwords, and microphone decibel readings run exclusively on your phone.\n\n" +
                            "3. Direct Connections Only: Network requests occur strictly when you explicitly fetch live exchange rates (Frankfurter), run a Cloudflare speed test, or check GitHub for app updates.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = currentTheme.textPrimary
                )

                Button(
                    onClick = { showPrivacySheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
