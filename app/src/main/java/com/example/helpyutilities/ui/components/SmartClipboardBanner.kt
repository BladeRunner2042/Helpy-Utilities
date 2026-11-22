package com.example.helpyutilities.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.util.ClipboardSenseService
import com.example.helpyutilities.util.HapticFeedbackHelper
import com.example.helpyutilities.util.SmartClipboardAction

/**
 * Material You Smart Clipboard Suggestion Banner:
 * Appears dynamically when actionable data is detected in clipboard
 */
@Composable
fun SmartClipboardBanner(
    onActionTriggered: (SmartClipboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val isEnabled by AppPreferences.smartClipboardEnabled.collectAsState()

    var currentAction by remember { mutableStateOf<SmartClipboardAction?>(null) }

    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            currentAction = ClipboardSenseService.analyzeClipboard(context)
        } else {
            currentAction = null
        }
    }

    AnimatedVisibility(
        visible = currentAction != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        val action = currentAction ?: return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(currentTheme.cardBackground)
                .border(1.dp, currentTheme.primary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .clickable {
                    HapticFeedbackHelper.trigger(context)
                    onActionTriggered(action)
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentTheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = currentTheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SMART CLIPBOARD SENSE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primary,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = action.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = action.subtitle,
                            fontSize = 11.sp,
                            color = currentTheme.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                IconButton(
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        ClipboardSenseService.dismissText(action.rawValue)
                        currentAction = null
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = currentTheme.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
