package com.example.helpyutilities.util

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

enum class ClipboardIntentType { CURRENCY, MATH, UNIT, TEXT }

data class SmartClipboardAction(
    val type: ClipboardIntentType,
    val title: String,
    val subtitle: String,
    val rawValue: String,
    val icon: ImageVector,
    val targetTabIndex: Int,
    val targetModalId: Int = 0,
)

/**
 * 100% On-Device Smart Clipboard Analyzer
 * Parses clipboard text locally with zero telemetry/cloud calls
 */
object ClipboardSenseService {

    private var lastDismissedText: String? = null

    fun analyzeClipboard(context: Context): SmartClipboardAction? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        if (!clipboard.hasPrimaryClip()) return null

        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount == 0) return null

        val raw = clip.getItemAt(0)?.text?.toString()?.trim() ?: return null
        if (raw.isBlank() || raw.length > 250 || raw == lastDismissedText) {
            return null
        }

        // 1. Detect Currency (e.g. "$450", "120.50 USD", "R 1,500", "€99", "100 EUR")
        val currencyRegex = Regex(
            """^([$€£¥R₹₩]\s?\d[\d,.]*|\d[\d,.]*\s?(USD|EUR|GBP|ZAR|JPY|AUD|CAD|BTC|INR|ETH|CHF|CNY))$""",
            RegexOption.IGNORE_CASE,
        )
        if (currencyRegex.matches(raw)) {
            return SmartClipboardAction(
                type = ClipboardIntentType.CURRENCY,
                title = "Convert \"$raw\"",
                subtitle = "Open in Live Currency Converter",
                rawValue = raw,
                icon = Icons.Default.CurrencyExchange,
                targetTabIndex = 3,
            )
        }

        // 2. Detect Math Expression (e.g., "145 * 18.5", "(420 + 80) / 4", "50 + 20")
        val mathRegex = Regex("""^[\d.\s()]++[+\-*x×÷/^%][\d.\s()+\-*x×÷/^%]+$""")
        if (mathRegex.matches(raw)) {
            return SmartClipboardAction(
                type = ClipboardIntentType.MATH,
                title = "Solve \"$raw\"",
                subtitle = "Calculate in Smart Calculator",
                rawValue = raw,
                icon = Icons.Default.Calculate,
                targetTabIndex = 2,
            )
        }

        // 3. Detect Unit Measurements (e.g., "75 kg", "12.5 miles", "32 C", "180 cm", "500 ml")
        val unitRegex = Regex(
            """^\d+(\.\d+)?\s?(kg|g|lb|lbs|oz|km|m|cm|mm|mi|miles|ft|in|c|f|°c|°f|l|ml|gal)$""",
            RegexOption.IGNORE_CASE,
        )
        if (unitRegex.matches(raw)) {
            return SmartClipboardAction(
                type = ClipboardIntentType.UNIT,
                title = "Convert unit \"$raw\"",
                subtitle = "Open in Unit Converter",
                rawValue = raw,
                icon = Icons.Default.Straighten,
                targetTabIndex = 2,
                targetModalId = 1,
            )
        }

        // 4. Detect Multi-Word Text / Non-ASCII for Quick Translation
        val hasNonAscii = raw.any { it.code > 127 }
        val wordCount = raw.split(Regex("""\s+""")).size
        if (hasNonAscii || wordCount >= 3) {
            val preview = if (raw.length > 28) "${raw.take(28)}..." else raw
            return SmartClipboardAction(
                type = ClipboardIntentType.TEXT,
                title = "Translate \"$preview\"",
                subtitle = "Open in Universal Translator",
                rawValue = raw,
                icon = Icons.Default.Translate,
                targetTabIndex = 4,
            )
        }

        return null
    }

    fun dismissText(rawValue: String) {
        lastDismissedText = rawValue
    }
}
