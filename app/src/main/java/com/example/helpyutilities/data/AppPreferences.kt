package com.example.helpyutilities.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 6-Theme Material You Engine:
 * 1. SYSTEM: Adaptive Material You
 * 2. LIGHT: Warm Alabaster Canvas with pure white elevated cards
 * 3. DARK: Slate Charcoal Canvas (#111318) with tonal elevated cards
 * 4. OLED: True Pitch-Black (#000000) canvas with hairline borders
 * 5. MONOCHROME: Editorial E-Ink Grayscale (zero color saturation)
 * 6. MONOCHROME_OLED: Pitch-black (#000000) with stark white & neutral grays
 */
enum class AppThemeMode(
    val displayName: String,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val background: Color,
    val cardBackground: Color,
    val borderColor: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val isLight: Boolean = false,
) {
    SYSTEM(
        displayName = "Follow System",
        primary = Color(0xFF2563EB), // Cobalt / Slate Blue
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF0D9488), // Teal
        background = Color(0xFF111318),
        cardBackground = Color(0xFF1C2026),
        borderColor = Color(0xFF2B303A),
        surfaceContainerHigh = Color(0xFF262B36),
        surfaceContainerHighest = Color(0xFF303644),
        textPrimary = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
    ),
    LIGHT(
        displayName = "Light",
        primary = Color(0xFF2563EB),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF0D9488),
        background = Color(0xFFF8F9FA),
        cardBackground = Color(0xFFFFFFFF),
        borderColor = Color(0xFFE5E7EB),
        surfaceContainerHigh = Color(0xFFEEF2F6),
        surfaceContainerHighest = Color(0xFFE2E8F0),
        textPrimary = Color(0xFF111827),
        textSecondary = Color(0xFF6B7280),
        isLight = true,
    ),
    DARK(
        displayName = "Dark",
        primary = Color(0xFF3B82F6),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF14B8A6),
        background = Color(0xFF111318),
        cardBackground = Color(0xFF1C2026),
        borderColor = Color(0xFF2B303A),
        surfaceContainerHigh = Color(0xFF262B36),
        surfaceContainerHighest = Color(0xFF303644),
        textPrimary = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
    ),
    OLED(
        displayName = "OLED",
        primary = Color(0xFF3B82F6),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF10B981),
        background = Color(0xFF000000),
        cardBackground = Color(0xFF121417),
        borderColor = Color(0xFF242830),
        surfaceContainerHigh = Color(0xFF1D2129),
        surfaceContainerHighest = Color(0xFF272C36),
        textPrimary = Color(0xFFF9FAFB),
        textSecondary = Color(0xFF9CA3AF),
    ),
    MONOCHROME(
        displayName = "Monochrome",
        primary = Color(0xFF18181B),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF3F3F46),
        background = Color(0xFFF4F4F5),
        cardBackground = Color(0xFFFFFFFF),
        borderColor = Color(0xFFD4D4D8),
        surfaceContainerHigh = Color(0xFFE4E4E7),
        surfaceContainerHighest = Color(0xFFD4D4D8),
        textPrimary = Color(0xFF09090B),
        textSecondary = Color(0xFF52525B),
        isLight = true,
    ),
    MONOCHROME_OLED(
        displayName = "Mono OLED",
        primary = Color(0xFFFFFFFF),
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFFD4D4D4),
        background = Color(0xFF000000),
        cardBackground = Color(0xFF121212),
        borderColor = Color(0xFF262626),
        surfaceContainerHigh = Color(0xFF222222),
        surfaceContainerHighest = Color(0xFF2E2E2E),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFA3A3A3),
    );

    fun resolved(isDark: Boolean): AppThemeMode = when (this) {
        SYSTEM -> if (isDark) DARK else LIGHT
        else -> this
    }
}

object AppPreferences {
    private const val PREFS_NAME = "helpy_app_prefs"
    private const val KEY_THEME = "app_theme_mode"
    private const val KEY_HAPTICS = "app_haptics_enabled"
    private const val KEY_PRECISION = "app_decimal_precision"
    private const val KEY_AUTO_UPDATE = "app_auto_update_check"
    private const val KEY_OFFLINE_TRANSLATE = "app_offline_translate"
    private const val KEY_DOWNLOADED_PACKS = "app_downloaded_lang_packs"
    private const val KEY_SMART_CLIPBOARD = "app_smart_clipboard_enabled"
    private const val KEY_PINNED_TOOLS = "app_pinned_tool_ids"
    private const val KEY_CURRENCY_FROM = "app_workspace_currency_from"
    private const val KEY_CURRENCY_TO = "app_workspace_currency_to"
    private const val KEY_CALCULATOR_MODE = "app_workspace_calc_is_sci"
    private const val KEY_CONVERTER_CAT = "app_workspace_converter_cat"

    private var prefs: SharedPreferences? = null

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _decimalPrecision = MutableStateFlow(4)
    val decimalPrecision: StateFlow<Int> = _decimalPrecision.asStateFlow()

    private val _autoUpdateCheck = MutableStateFlow(true)
    val autoUpdateCheck: StateFlow<Boolean> = _autoUpdateCheck.asStateFlow()

    private val _offlineTranslateEnabled = MutableStateFlow(false)
    val offlineTranslateEnabled: StateFlow<Boolean> = _offlineTranslateEnabled.asStateFlow()

    private val _smartClipboardEnabled = MutableStateFlow(true)
    val smartClipboardEnabled: StateFlow<Boolean> = _smartClipboardEnabled.asStateFlow()

    private val _downloadedLanguagePacks = MutableStateFlow(setOf("en", "es", "fr", "de", "zh-CN", "ja", "ar"))
    val downloadedLanguagePacks: StateFlow<Set<String>> = _downloadedLanguagePacks.asStateFlow()

    private val _pinnedToolIds = MutableStateFlow(setOf("level", "translate", "currency", "passwords", "converter"))
    val pinnedToolIds: StateFlow<Set<String>> = _pinnedToolIds.asStateFlow()

    private val _workspaceCurrencyFrom = MutableStateFlow("USD")
    val workspaceCurrencyFrom: StateFlow<String> = _workspaceCurrencyFrom.asStateFlow()

    private val _workspaceCurrencyTo = MutableStateFlow("EUR")
    val workspaceCurrencyTo: StateFlow<String> = _workspaceCurrencyTo.asStateFlow()

    private val _workspaceCalcIsScientific = MutableStateFlow(false)
    val workspaceCalcIsScientific: StateFlow<Boolean> = _workspaceCalcIsScientific.asStateFlow()

    private val _workspaceConverterCategory = MutableStateFlow("Length")
    val workspaceConverterCategory: StateFlow<String> = _workspaceConverterCategory.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        val themeName = sp.getString(KEY_THEME, AppThemeMode.DARK.name)
        _themeMode.value = try {
            AppThemeMode.valueOf(themeName ?: AppThemeMode.DARK.name)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }

        _hapticsEnabled.value = sp.getBoolean(KEY_HAPTICS, true)
        _decimalPrecision.value = sp.getInt(KEY_PRECISION, 4)
        _autoUpdateCheck.value = sp.getBoolean(KEY_AUTO_UPDATE, true)
        _offlineTranslateEnabled.value = sp.getBoolean(KEY_OFFLINE_TRANSLATE, false)
        _smartClipboardEnabled.value = sp.getBoolean(KEY_SMART_CLIPBOARD, true)
        _downloadedLanguagePacks.value = sp.getStringSet(KEY_DOWNLOADED_PACKS, setOf("en", "es", "fr", "de", "zh-CN", "ja", "ar")) ?: setOf("en", "es")
        _pinnedToolIds.value = sp.getStringSet(KEY_PINNED_TOOLS, setOf("level", "translate", "currency", "passwords", "converter")) ?: setOf("level", "translate", "currency")
        _workspaceCurrencyFrom.value = sp.getString(KEY_CURRENCY_FROM, "USD") ?: "USD"
        _workspaceCurrencyTo.value = sp.getString(KEY_CURRENCY_TO, "EUR") ?: "EUR"
        _workspaceCalcIsScientific.value = sp.getBoolean(KEY_CALCULATOR_MODE, false)
        _workspaceConverterCategory.value = sp.getString(KEY_CONVERTER_CAT, "Length") ?: "Length"
    }

    fun setTheme(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs?.edit()?.putString(KEY_THEME, mode.name)?.apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_HAPTICS, enabled)?.apply()
    }

    fun setDecimalPrecision(precision: Int) {
        _decimalPrecision.value = precision
        prefs?.edit()?.putInt(KEY_PRECISION, precision)?.apply()
    }

    fun setAutoUpdateCheck(enabled: Boolean) {
        _autoUpdateCheck.value = enabled
        prefs?.edit()?.putBoolean(KEY_AUTO_UPDATE, enabled)?.apply()
    }

    fun setOfflineTranslateEnabled(enabled: Boolean) {
        _offlineTranslateEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_OFFLINE_TRANSLATE, enabled)?.apply()
    }

    fun setSmartClipboardEnabled(enabled: Boolean) {
        _smartClipboardEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_SMART_CLIPBOARD, enabled)?.apply()
    }

    fun toggleLanguagePack(langCode: String) {
        val current = _downloadedLanguagePacks.value.toMutableSet()
        if (current.contains(langCode)) {
            current.remove(langCode)
        } else {
            current.add(langCode)
        }
        _downloadedLanguagePacks.value = current
        prefs?.edit()?.putStringSet(KEY_DOWNLOADED_PACKS, current)?.apply()
    }

    fun togglePinnedTool(toolId: String) {
        val current = _pinnedToolIds.value.toMutableSet()
        if (current.contains(toolId)) {
            current.remove(toolId)
        } else {
            current.add(toolId)
        }
        _pinnedToolIds.value = current
        prefs?.edit()?.putStringSet(KEY_PINNED_TOOLS, current)?.apply()
    }

    fun setWorkspaceCurrencyPair(from: String, to: String) {
        _workspaceCurrencyFrom.value = from
        _workspaceCurrencyTo.value = to
        prefs?.edit()?.putString(KEY_CURRENCY_FROM, from)?.putString(KEY_CURRENCY_TO, to)?.apply()
    }

    fun setWorkspaceCalcIsScientific(isScientific: Boolean) {
        _workspaceCalcIsScientific.value = isScientific
        prefs?.edit()?.putBoolean(KEY_CALCULATOR_MODE, isScientific)?.apply()
    }

    fun setWorkspaceConverterCategory(category: String) {
        _workspaceConverterCategory.value = category
        prefs?.edit()?.putString(KEY_CONVERTER_CAT, category)?.apply()
    }
}
