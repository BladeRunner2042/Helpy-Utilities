package com.example.helpyutilities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.ui.MainScreen
import com.example.helpyutilities.ui.theme.HelpyUtilitiesTheme

class MainActivity : ComponentActivity() {

    private var initialTab by mutableIntStateOf(0)
    private var prefilledQuery by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.init(this)
        com.example.helpyutilities.util.NotificationHelper.initChannels(this)
        enableEdgeToEdge()
        val (tab, query) = resolveIntentAction(intent)
        initialTab = tab
        prefilledQuery = query

        setContent {
            HelpyUtilitiesTheme {
                MainScreen(
                    initialTab = initialTab,
                    prefilledQuery = prefilledQuery
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val (tab, query) = resolveIntentAction(intent)
        if (tab >= 0) {
            initialTab = tab
            prefilledQuery = query
        }
    }

    private fun resolveIntentAction(intent: Intent?): Pair<Int, String?> {
        if (intent == null) return Pair(0, null)

        // 1. Android Text Selection Context Menu (PROCESS_TEXT)
        if (intent.action == Intent.ACTION_PROCESS_TEXT) {
            val processText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim()
            if (!processText.isNullOrBlank()) {
                // If it looks like a math expression, route to Calculator; otherwise Translate
                val isMath = processText.matches(Regex("^[0-9.+\\-*/()\\s^%]+$"))
                return if (isMath) {
                    Pair(2, processText)
                } else {
                    Pair(4, processText)
                }
            }
        }

        // 2. Tab extra
        if (intent.hasExtra("EXTRA_TAB")) {
            return Pair(intent.getIntExtra("EXTRA_TAB", 0), null)
        }

        // 3. Deep links (e.g. from App Shortcuts or TileServices: helpy://tool/{calculator|currency|speedtest|translate|sensors})
        val data = intent.data
        if (data != null && data.scheme == "helpy" && data.host == "tool") {
            val tab = when (data.lastPathSegment?.lowercase()) {
                "torch", "speedtest", "sensors", "toolbox" -> 1
                "calculator", "math" -> 2
                "currency", "forex" -> 3
                "translate", "ocr" -> 4
                else -> 0
            }
            return Pair(tab, null)
        }

        return Pair(0, null)
    }
}
