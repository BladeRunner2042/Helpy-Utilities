package com.example.helpyutilities.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.wrapContentWidth
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.util.HapticFeedbackHelper

@Composable
fun KeepAwakePill(
    modifier: Modifier = Modifier,
    initialState: Boolean = false,
    onStateChanged: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    var isAwake by remember { mutableStateOf(initialState) }

    DisposableEffect(isAwake) {
        val window = (context as? Activity)?.window
        if (isAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (isAwake) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (isAwake) EmeraldLime.copy(alpha = 0.2f) else currentTheme.surfaceContainerHigh
            )
            .clickable {
                HapticFeedbackHelper.trigger(context)
                val newState = !isAwake
                isAwake = newState
                onStateChanged?.invoke(newState)
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (isAwake) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = if (isAwake) "Keep Awake On" else "Keep Awake Off",
                tint = if (isAwake) EmeraldLime else currentTheme.textSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isAwake) "Awake" else "Normal",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAwake) EmeraldLime else currentTheme.textSecondary,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}
