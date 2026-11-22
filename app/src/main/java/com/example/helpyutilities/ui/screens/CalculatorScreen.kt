package com.example.helpyutilities.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.tabularNumberStyle
import com.example.helpyutilities.util.HapticFeedbackHelper
import com.example.helpyutilities.util.MathEvaluator

data class CalcHistoryItem(val expression: String, val result: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(initialExpression: String? = null) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current

    val persistedIsSci by AppPreferences.workspaceCalcIsScientific.collectAsState()

    var displayExpression by remember(initialExpression) {
        mutableStateOf(if (!initialExpression.isNullOrBlank()) initialExpression else "0")
    }
    var expressionHistory by remember { mutableStateOf("") }
    var isScientificMode by remember(persistedIsSci) { mutableStateOf(persistedIsSci) }

    val historyList = remember { mutableStateListOf<CalcHistoryItem>() }
    var showHistorySheet by remember { mutableStateOf(false) }
    var activeToolModal by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Compute live preview
    val livePreview: String = remember(displayExpression) {
        if (displayExpression.isNotEmpty() && displayExpression != "0" &&
            displayExpression.any { it in "+-×÷*/^" } &&
            !displayExpression.endsWith("+") && !displayExpression.endsWith("-") &&
            !displayExpression.endsWith("×") && !displayExpression.endsWith("÷") &&
            !displayExpression.endsWith("*") && !displayExpression.endsWith("/")
        ) {
            when (val eval = MathEvaluator.evaluate(displayExpression)) {
                is MathEvaluator.EvaluationResult.Success -> "= " + MathEvaluator.formatResult(eval.value)
                is MathEvaluator.EvaluationResult.Error -> ""
            }
        } else ""
    }

    val toolPills = listOf(
        1 to "Unit Converter",
        2 to "Loan & EMI",
        3 to "Tip & Split",
        4 to "Discount & Tax",
        5 to "BMI Health",
        6 to "Date & Age",
        7 to "Percentage",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Calculator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = currentTheme.textPrimary,
                letterSpacing = (-0.5).sp,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                com.example.helpyutilities.ui.components.KeepAwakePill()

                IconButton(
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        val newMode = !isScientificMode
                        isScientificMode = newMode
                        AppPreferences.setWorkspaceCalcIsScientific(newMode)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Scientific Mode",
                        tint = if (isScientificMode) currentTheme.primary else currentTheme.textSecondary,
                    )
                }

                IconButton(
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        showHistorySheet = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = currentTheme.textSecondary,
                    )
                }
            }
        }

        // Horizontal Mode Switcher Chips (Clean Material 3 Stadium-Pills, Zero Emoji)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(toolPills) { (id, label) ->
                FilterChip(
                    selected = activeToolModal == id,
                    onClick = {
                        HapticFeedbackHelper.trigger(context)
                        activeToolModal = id
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = currentTheme.primary.copy(alpha = 0.16f),
                        selectedLabelColor = currentTheme.primary,
                        containerColor = currentTheme.surfaceContainerHigh,
                        labelColor = currentTheme.textPrimary,
                    ),
                    shape = RoundedCornerShape(100.dp),
                )
            }
        }

        // Display Area (surfaceContainerLow, clean tabular numbers)
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(displayExpression))
                        HapticFeedbackHelper.trigger(context)
                    }
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = expressionHistory.ifEmpty { " " },
                    fontSize = 13.sp,
                    color = currentTheme.textSecondary,
                    textAlign = TextAlign.End,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayExpression,
                    style = tabularNumberStyle(size = if (displayExpression.length > 9) 32 else 44, color = currentTheme.textPrimary),
                    maxLines = 1,
                    textAlign = TextAlign.End,
                )

                if (livePreview.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = livePreview,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.primary,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }

        // Keypad with Material 3 Tonal Color Hierarchy
        val rows = if (isScientificMode) {
            listOf(
                listOf("sin", "cos", "tan", "sqrt", "^"),
                listOf("C", "(", ")", "%", "÷"),
                listOf("7", "8", "9", "⌫", "×"),
                listOf("4", "5", "6", "pi", "-"),
                listOf("1", "2", "3", "e", "+"),
                listOf("0", ".", "="),
            )
        } else {
            listOf(
                listOf("C", "(", ")", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("⌫", "0", ".", "="),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowKeys.forEach { key ->
                        val isOp = key in listOf("+", "-", "×", "÷", "%", "^")
                        val isSci = key in listOf("sin", "cos", "tan", "sqrt", "pi", "e", "(", ")")
                        val isEquals = key == "="
                        val isUtility = key in listOf("C", "⌫")

                        // Material 3 Tonal Palette
                        val containerColor = when {
                            isEquals -> currentTheme.primary
                            isOp -> currentTheme.surfaceContainerHighest
                            isUtility -> currentTheme.surfaceContainerHighest
                            isSci -> currentTheme.surfaceContainerHigh
                            else -> currentTheme.surfaceContainerHigh
                        }

                        val contentColor = when {
                            isEquals -> androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            isOp -> currentTheme.primary
                            isUtility -> currentTheme.textPrimary
                            else -> currentTheme.textPrimary
                        }

                        Button(
                            onClick = {
                                HapticFeedbackHelper.trigger(context)
                                when (key) {
                                    "C" -> {
                                        displayExpression = "0"
                                        expressionHistory = ""
                                    }
                                    "⌫" -> {
                                        displayExpression = if (displayExpression.length <= 1) {
                                            "0"
                                        } else {
                                            displayExpression.dropLast(1)
                                        }
                                    }
                                    "=" -> {
                                        when (val res = MathEvaluator.evaluate(displayExpression)) {
                                            is MathEvaluator.EvaluationResult.Success -> {
                                                val formatted = MathEvaluator.formatResult(res.value)
                                                historyList.add(0, CalcHistoryItem(displayExpression, formatted))
                                                expressionHistory = "$displayExpression ="
                                                displayExpression = formatted
                                            }
                                            is MathEvaluator.EvaluationResult.Error -> {
                                                displayExpression = "Error"
                                            }
                                        }
                                    }
                                    else -> {
                                        if (displayExpression == "0" && key in "0123456789") {
                                            displayExpression = key
                                        } else if (displayExpression == "Error") {
                                            displayExpression = key
                                        } else {
                                            displayExpression += key
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(if (key == "0" && !isScientificMode && rowKeys.size == 3) 2f else 1f)
                                .height(if (isScientificMode) 48.dp else 56.dp),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                contentColor = contentColor,
                            ),
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = if (isSci) 13.sp else 18.sp,
                                    fontWeight = if (isEquals || isOp) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Calculator Tools
    if (activeToolModal != 0) {
        MathToolsModal(
            activeTool = activeToolModal,
            sheetState = sheetState,
            onDismiss = { activeToolModal = 0 },
        )
    }

    // Modal Sheet for History
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState,
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Calculation History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                    if (historyList.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.primary,
                            modifier = Modifier.clickable {
                                HapticFeedbackHelper.trigger(context)
                                historyList.clear()
                            },
                        )
                    }
                }

                if (historyList.isEmpty()) {
                    Text("No calculations yet.", color = currentTheme.textSecondary, fontSize = 13.sp)
                } else {
                    historyList.forEach { item ->
                        FrostedGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedbackHelper.trigger(context)
                                    displayExpression = item.result
                                    showHistorySheet = false
                                },
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(item.expression, color = currentTheme.textSecondary, fontSize = 13.sp)
                                Text("= ${item.result}", color = currentTheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
