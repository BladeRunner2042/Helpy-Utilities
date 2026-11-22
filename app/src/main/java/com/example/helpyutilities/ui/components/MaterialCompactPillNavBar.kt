package com.example.helpyutilities.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.ui.theme.LocalAppTheme
import com.example.helpyutilities.util.HapticFeedbackHelper

data class HelpyNavItem(
    val index: Int,
    val icon: ImageVector,
    val label: String,
)

/**
 * Standardized High-Contrast Material 3 Compact Floating Pill Bottom Navigation Bar:
 * - True compact floating pill (wrapContentWidth, 58dp height, centered 14dp above nav insets, RoundedCornerShape(100))
 * - Inactive tabs display only a 20dp icon with a >= 44dp touch target
 * - Active tab smoothly expands a compact tonal pill (secondaryContainer) showing icon + single-line 11sp label
 * - Solid surface container (cardBackground) with 1dp border and zero blur overhead for 120fps performance
 */
@Composable
fun MaterialCompactPillNavBar(
    selectedIndex: Int,
    items: List<HelpyNavItem>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentTheme = LocalAppTheme.current
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .height(58.dp),
                
            shape = RoundedCornerShape(100),
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 8.dp,
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val isSelected = selectedIndex == item.index
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(100))
                            .background(
                                if (isSelected) colorScheme.secondaryContainer
                                else Color.Transparent
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) {
                                HapticFeedbackHelper.trigger(context)
                                onTabSelected(item.index)
                            }
                            .padding(horizontal = if (isSelected) 14.dp else 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) colorScheme.onSecondaryContainer else currentTheme.textSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                            if (isSelected) {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

