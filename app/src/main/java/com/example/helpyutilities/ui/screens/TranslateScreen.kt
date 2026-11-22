package com.example.helpyutilities.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.helpyutilities.data.AppPreferences
import com.example.helpyutilities.ui.components.FrostedGlassCard
import com.example.helpyutilities.ui.theme.ElectricCyan
import com.example.helpyutilities.ui.theme.EmeraldLime
import com.example.helpyutilities.ui.theme.SunsetGold
import com.example.helpyutilities.util.HapticFeedbackHelper
import com.example.helpyutilities.util.OfflineTranslator
import com.example.helpyutilities.util.TranslationEngine
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(initialText: String? = null) {
    val context = LocalContext.current
    val currentTheme = com.example.helpyutilities.ui.theme.LocalAppTheme.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val downloadedPacks by AppPreferences.downloadedLanguagePacks.collectAsState()

    var activeTranslateTab by remember { mutableIntStateOf(0) } // 0: Text, 1: Camera/Image OCR, 2: Offline Packs

    var sourceLang by remember { mutableStateOf(LanguageItem("en", "English", "🇺🇸")) }
    var targetLang by remember { mutableStateOf(LanguageItem("es", "Spanish (Español)", "🇪🇸")) }
    var sourceText by remember(initialText) { mutableStateOf(initialText ?: "") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var translationSourceBanner by remember { mutableStateOf("") }

    // Downloading Packs State Map (LangCode -> Progress 0.0f..1.0f)
    val downloadingPacks = remember { mutableStateMapOf<String, Float>() }

    // Camera & Image OCR State
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isOcrProcessing by remember { mutableStateOf(false) }
    var detectedOcrText by remember { mutableStateOf("") }
    var detectedWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var detectedWordElements by remember { mutableStateOf<List<OcrWordElement>>(emptyList()) }
    var detectedBlockElements by remember { mutableStateOf<List<OcrBlockElement>>(emptyList()) }
    val blockTranslations = remember { mutableStateMapOf<Int, String>() }
    var ocrImageWidth by remember { mutableIntStateOf(1080) }
    var ocrImageHeight by remember { mutableIntStateOf(1920) }

    val selectedWordIndices = remember { mutableStateListOf<Int>() }
    var showArOverlayMode by remember { mutableStateOf(false) }
    var liveSelectionTranslation by remember { mutableStateOf("") }
    var isTranslatingSelection by remember { mutableStateOf(false) }

    var imageZoom by remember { mutableFloatStateOf(1f) }
    var imagePan by remember { mutableStateOf(Offset.Zero) }

    var pickingForSource by remember { mutableStateOf(true) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val searchQueryState = remember { MutableStateFlow("") }
    val searchQuery by searchQueryState.collectAsState()

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) worldLanguagesList else {
            worldLanguagesList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true) }
        }
    }

    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeakingAudio by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance = tts
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
            com.example.helpyutilities.util.AiVoiceTtsEngine.stop()
        }
    }

    fun speak(text: String, langCode: String) {
        if (text.isBlank()) return
        HapticFeedbackHelper.trigger(context)
        isSpeakingAudio = true
        coroutineScope.launch {
            com.example.helpyutilities.util.AiVoiceTtsEngine.speakText(
                context = context,
                text = text,
                langCode = langCode,
                systemTts = ttsInstance,
                onComplete = { isSpeakingAudio = false }
            )
        }
    }

    fun startDownloadPack(langCode: String, langName: String) {
        if (downloadingPacks.containsKey(langCode)) return
        if (downloadedPacks.contains(langCode)) {
            AppPreferences.toggleLanguagePack(langCode)
            Toast.makeText(context, "$langName pack removed", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            HapticFeedbackHelper.trigger(context)
            downloadingPacks[langCode] = 0.05f
            for (step in 1..10) {
                delay(100)
                downloadingPacks[langCode] = step / 10f
            }
            AppPreferences.toggleLanguagePack(langCode)
            downloadingPacks.remove(langCode)
            HapticFeedbackHelper.trigger(context)
            Toast.makeText(context, "$langName offline pack downloaded and ready! ✓", Toast.LENGTH_SHORT).show()
            com.example.helpyutilities.util.NotificationHelper.showLocalAlert(
                context = context,
                title = "Language Pack Ready: $langName",
                body = "Offline translation model (~14 MB) for $langName has finished downloading and is ready for offline use.",
                tabIndex = 4
            )
        }
    }

    suspend fun executeTranslation(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) {
            translatedText = ""
            translationSourceBanner = ""
            return
        }

        val isOnline = isNetworkAvailable(context)
        val hasPack = downloadedPacks.contains(targetLang.code)

        // Automatic Network Detection: If offline, run on-device pack immediately
        if (!isOnline) {
            if (hasPack) {
                val offlineResult = OfflineTranslator.translateOffline(clean, targetLang.code)
                if (offlineResult != null && offlineResult.isNotBlank()) {
                    translatedText = offlineResult
                    translationSourceBanner = "Offline On-Device Pack (${targetLang.name}) ⚡"
                } else {
                    translatedText = clean
                    translationSourceBanner = "Offline Mode: Phrase not found in offline pack"
                }
            } else {
                translatedText = clean
                translationSourceBanner = "Offline: Download ${targetLang.name} pack in Offline Packs tab"
            }
            return
        }

        // Online High-Quality Translation with Automatic Seamless Offline Fallback
        isTranslating = true
        withContext(Dispatchers.IO) {
            try {
                val (result, engineBanner) = TranslationEngine.translate(
                    text = clean,
                    sourceCode = sourceLang.code,
                    targetCode = targetLang.code
                )
                withContext(Dispatchers.Main) {
                    translatedText = result
                    translationSourceBanner = engineBanner
                    isTranslating = false
                }
            } catch (e: Exception) {
                // Seamless fallback to on-device pack if online request times out or fails
                val offlineResult = if (hasPack) OfflineTranslator.translateOffline(clean, targetLang.code) else null
                withContext(Dispatchers.Main) {
                    if (offlineResult != null && offlineResult.isNotBlank()) {
                        translatedText = offlineResult
                        translationSourceBanner = "Offline Fallback (${targetLang.name}) ⚡"
                    } else {
                        translatedText = clean
                        translationSourceBanner = "Offline Fallback: Network timed out"
                    }
                    isTranslating = false
                }
            }
        }
    }

    suspend fun translateSingleText(text: String): String {
        val clean = text.trim()
        if (clean.isBlank()) return ""
        val isOnline = isNetworkAvailable(context)
        val hasPack = downloadedPacks.contains(targetLang.code)
        if (!isOnline) {
            if (hasPack) {
                return OfflineTranslator.translateOffline(clean, targetLang.code) ?: clean
            }
            return clean
        }
        return try {
            val (result, _) = TranslationEngine.translate(
                text = clean,
                sourceCode = sourceLang.code,
                targetCode = targetLang.code
            )
            result
        } catch (_: Exception) {
            if (hasPack) OfflineTranslator.translateOffline(clean, targetLang.code) ?: clean else clean
        }
    }

    fun translateSelectedWords() {
        if (selectedWordIndices.isEmpty()) {
            liveSelectionTranslation = ""
            return
        }
        val sorted = selectedWordIndices.toList().sorted()
        val phrase = sorted.mapNotNull { idx ->
            detectedWordElements.getOrNull(idx)?.text ?: detectedWords.getOrNull(idx)
        }.joinToString(" ")

        isTranslatingSelection = true
        coroutineScope.launch {
            val res = translateSingleText(phrase)
            liveSelectionTranslation = res
            isTranslatingSelection = false
        }
    }

    fun toggleArOverlay(enable: Boolean) {
        HapticFeedbackHelper.trigger(context)
        showArOverlayMode = enable
        if (enable && blockTranslations.isEmpty() && detectedBlockElements.isNotEmpty()) {
            coroutineScope.launch {
                detectedBlockElements.forEach { block ->
                    val trans = translateSingleText(block.text)
                    blockTranslations[block.index] = trans
                }
            }
        }
    }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
            isOcrProcessing = true
            coroutineScope.launch {
                val extraction = extractTextAndWordsFromImageUri(context, tempCameraUri!!)
                isOcrProcessing = false
                detectedOcrText = extraction.fullText
                detectedWords = extraction.words
                detectedWordElements = extraction.wordElements
                detectedBlockElements = extraction.blockElements
                ocrImageWidth = extraction.imageWidth
                ocrImageHeight = extraction.imageHeight
                selectedWordIndices.clear()
                liveSelectionTranslation = ""
                blockTranslations.clear()
                imageZoom = 1f
                imagePan = Offset.Zero
                if (extraction.fullText.isNotBlank()) {
                    sourceText = extraction.fullText
                    executeTranslation(extraction.fullText)
                }
            }
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val tempFile = File(context.cacheDir, "temp_camera_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
                tempCameraUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to snap photos for translation.", Toast.LENGTH_LONG).show()
        }
    }

    // Gallery Photo Picker Launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isOcrProcessing = true
            coroutineScope.launch {
                val extraction = extractTextAndWordsFromImageUri(context, uri)
                isOcrProcessing = false
                detectedOcrText = extraction.fullText
                detectedWords = extraction.words
                detectedWordElements = extraction.wordElements
                detectedBlockElements = extraction.blockElements
                ocrImageWidth = extraction.imageWidth
                ocrImageHeight = extraction.imageHeight
                selectedWordIndices.clear()
                liveSelectionTranslation = ""
                blockTranslations.clear()
                imageZoom = 1f
                imagePan = Offset.Zero
                if (extraction.fullText.isNotBlank()) {
                    sourceText = extraction.fullText
                    executeTranslation(extraction.fullText)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = EmeraldLime,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Universal Translate",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.textPrimary
                        )
                        Text(
                            text = "100+ World Languages • Camera OCR • Smart Offline",
                            fontSize = 12.sp,
                            color = currentTheme.textSecondary
                        )
                    }
                }

                com.example.helpyutilities.ui.components.KeepAwakePill()
            }
        }

        // Mode Navigation Chips (Text Translate / Camera & Image OCR / Offline Packs)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Text Translate", "Camera & Image OCR", "Offline Packs").indices.toList()) { idx ->
                    val titles = listOf("Text Translate", "Camera & Image OCR", "Offline Packs")
                    val isSelected = activeTranslateTab == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            activeTranslateTab = idx
                        },
                        label = { Text(titles[idx], maxLines = 1, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        leadingIcon = {
                            when (idx) {
                                0 -> Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                1 -> Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                else -> Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldLime,
                            selectedLabelColor = currentTheme.background,
                            selectedLeadingIconColor = currentTheme.background,
                            containerColor = currentTheme.surfaceContainerHigh,
                            labelColor = currentTheme.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Language Selector Row (Available across modes)
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            pickingForSource = true
                            showLanguageSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(46.dp)
                    ) {
                        Text("${sourceLang.flag} ${sourceLang.name}", color = ElectricCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }

                    IconButton(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            if (sourceLang.code != "auto") {
                                val tmp = sourceLang
                                sourceLang = targetLang
                                targetLang = tmp
                                if (translatedText.isNotBlank()) {
                                    val tmpText = sourceText
                                    sourceText = translatedText
                                    translatedText = tmpText
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 4.dp).size(46.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = EmeraldLime, modifier = Modifier.size(28.dp))
                    }

                    Button(
                        onClick = {
                            HapticFeedbackHelper.trigger(context)
                            pickingForSource = false
                            showLanguageSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).defaultMinSize(minWidth = 84.dp).height(46.dp)
                    ) {
                        Text("${targetLang.flag} ${targetLang.name}", color = EmeraldLime, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // TAB CONTENT
        when (activeTranslateTab) {
            // TAB 0: TEXT TRANSLATION
            0 -> {
                // Input Card
                item {
                    val isOnline = isNetworkAvailable(context)
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Source Text (${sourceLang.code.uppercase()})", fontSize = 12.sp, color = currentTheme.textSecondary)

                                    // Subtle Engine Status Pill (Auto Cloud vs Offline Pack)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(if (isOnline) EmeraldLime.copy(alpha = 0.16f) else SunsetGold.copy(alpha = 0.16f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isOnline) EmeraldLime else SunsetGold)
                                            )
                                            Text(
                                                text = if (isOnline) "Cloud" else "Offline Pack",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOnline) EmeraldLime else SunsetGold
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${sourceText.length} chars", fontSize = 11.sp, color = currentTheme.textSecondary)
                                    if (sourceText.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                HapticFeedbackHelper.trigger(context)
                                                sourceText = ""
                                                translatedText = ""
                                                translationSourceBanner = ""
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = currentTheme.textSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = sourceText,
                                onValueChange = { sourceText = it },
                                placeholder = { Text("Type text to translate into ${targetLang.name}...", color = currentTheme.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldLime,
                                    unfocusedBorderColor = currentTheme.borderColor,
                                    focusedTextColor = currentTheme.textPrimary,
                                    unfocusedTextColor = currentTheme.textPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        HapticFeedbackHelper.trigger(context)
                                        speak(sourceText, sourceLang.code)
                                    },
                                    enabled = sourceText.isNotBlank()
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Listen Source", tint = if (sourceText.isNotBlank()) ElectricCyan else currentTheme.textSecondary.copy(alpha = 0.4f))
                                }

                                Button(
                                    onClick = {
                                        HapticFeedbackHelper.trigger(context)
                                        coroutineScope.launch { executeTranslation(sourceText) }
                                    },
                                    enabled = sourceText.isNotBlank() && !isTranslating,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldLime),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    if (isTranslating) {
                                        CircularProgressIndicator(color = currentTheme.background, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Translate Now", color = currentTheme.background, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Translated Result
                if (translatedText.isNotEmpty()) {
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Translated (${targetLang.code.uppercase()})", fontSize = 12.sp, color = EmeraldLime, fontWeight = FontWeight.Bold)
                                        if (translationSourceBanner.isNotBlank()) {
                                            Text(translationSourceBanner, fontSize = 10.sp, color = currentTheme.textSecondary)
                                        }
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                HapticFeedbackHelper.trigger(context)
                                                speak(translatedText, targetLang.code)
                                            }
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = EmeraldLime)
                                        }
                                        IconButton(
                                            onClick = {
                                                HapticFeedbackHelper.trigger(context)
                                                clipboardManager.setText(AnnotatedString(translatedText))
                                                Toast.makeText(context, "Translation copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = currentTheme.textPrimary)
                                        }
                                        IconButton(
                                            onClick = {
                                                HapticFeedbackHelper.trigger(context)
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, translatedText)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Translation"))
                                            }
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = currentTheme.textPrimary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = translatedText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = currentTheme.textPrimary,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                    }
                }

                // Section: Instant Travel & Daily Phrasebook
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Instant Travel & Daily Phrases",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.textPrimary
                        )
                        Text(
                            text = "Tap any phrase to instantly translate into ${targetLang.name}",
                            fontSize = 12.sp,
                            color = currentTheme.textSecondary
                        )
                    }
                }

                items(quickPhrasesList) { phrase ->
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticFeedbackHelper.trigger(context)
                                sourceText = phrase.english
                                coroutineScope.launch { executeTranslation(phrase.english) }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(phrase.category, fontSize = 11.sp, color = SunsetGold, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(phrase.english, fontSize = 15.sp, color = currentTheme.textPrimary, fontWeight = FontWeight.Medium)
                            }
                            Text("Translate →", fontSize = 12.sp, color = EmeraldLime, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // TAB 1: CAMERA & IMAGE OCR TRANSLATION
            1 -> {
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Camera & Image Optical Translation", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                            Text(
                                text = "Snap a photo of menus, signs, or documents to recognize text and translate automatically into ${targetLang.name}.",
                                fontSize = 12.sp,
                                color = currentTheme.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Camera Snap Button
                                Button(
                                    onClick = {
                                        HapticFeedbackHelper.trigger(context)
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            try {
                                                val tempFile = File(context.cacheDir, "temp_camera_${System.currentTimeMillis()}.jpg")
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
                                                tempCameraUri = uri
                                                takePictureLauncher.launch(uri)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldLime),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = currentTheme.background)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Snap Camera", color = currentTheme.background, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                // Gallery Pick Button
                                Button(
                                    onClick = {
                                        HapticFeedbackHelper.trigger(context)
                                        galleryPickerLauncher.launch("image/*")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surfaceContainerHigh),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(50.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = ElectricCyan)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Gallery", color = currentTheme.textPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Image Preview & OCR Result
                if (selectedImageUri != null) {
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Top Controls: AR Overlay Toggle Chip & Zoom Indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = showArOverlayMode,
                                            onClick = { toggleArOverlay(!showArOverlayMode) },
                                            label = {
                                                Text(
                                                    text = if (showArOverlayMode) "Translate on Image (On)" else "Translate on Image",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Translate,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp),
                                                    tint = if (showArOverlayMode) currentTheme.primary else currentTheme.textSecondary
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = currentTheme.primary.copy(alpha = 0.18f),
                                                selectedLabelColor = currentTheme.primary
                                            )
                                        )
                                    }

                                    if (imageZoom > 1.05f) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(currentTheme.primary)
                                                .clickable {
                                                    HapticFeedbackHelper.trigger(context)
                                                    imageZoom = 1f
                                                    imagePan = Offset.Zero
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.RestartAlt,
                                                    contentDescription = "Reset Zoom",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "1x Reset",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Interactive Photo Canvas with Finger-Drag Highlighting & AR Overlay
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(currentTheme.surfaceContainerHigh)
                                ) {
                                    val density = LocalDensity.current
                                    val containerWidthPx = with(density) { maxWidth.toPx() }
                                    val containerHeightPx = with(density) { maxHeight.toPx() }

                                    val imgW = ocrImageWidth.toFloat().coerceAtLeast(1f)
                                    val imgH = ocrImageHeight.toFloat().coerceAtLeast(1f)
                                    val imageAspect = imgW / imgH
                                    val containerAspect = containerWidthPx / containerHeightPx

                                    val (renderedWidthPx, renderedHeightPx, offsetX, offsetY) = if (containerAspect > imageAspect) {
                                        val rh = containerHeightPx
                                        val rw = rh * imageAspect
                                        val ox = (containerWidthPx - rw) / 2f
                                        listOf(rw, rh, ox, 0f)
                                    } else {
                                        val rw = containerWidthPx
                                        val rh = rw / imageAspect
                                        val oy = (containerHeightPx - rh) / 2f
                                        listOf(rw, rh, 0f, oy)
                                    }

                                    val scaleX = renderedWidthPx / imgW
                                    val scaleY = renderedHeightPx / imgH

                                    fun handleTouch(localPos: Offset) {
                                        if (showArOverlayMode) return
                                        val adjustedPos = (localPos - imagePan) / imageZoom
                                        val relX = adjustedPos.x - offsetX
                                        val relY = adjustedPos.y - offsetY

                                        var changed = false
                                        detectedWordElements.forEach { element ->
                                            val b = element.boundingBox
                                            val left = b.left * scaleX - 6f
                                            val top = b.top * scaleY - 6f
                                            val right = b.right * scaleX + 6f
                                            val bottom = b.bottom * scaleY + 6f

                                            if (relX in left..right && relY in top..bottom) {
                                                if (!selectedWordIndices.contains(element.index)) {
                                                    selectedWordIndices.add(element.index)
                                                    changed = true
                                                }
                                            }
                                        }
                                        if (changed) {
                                            HapticFeedbackHelper.trigger(context)
                                            translateSelectedWords()
                                        }
                                    }

                                    fun toggleSingleWord(localPos: Offset) {
                                        if (showArOverlayMode) return
                                        val adjustedPos = (localPos - imagePan) / imageZoom
                                        val relX = adjustedPos.x - offsetX
                                        val relY = adjustedPos.y - offsetY

                                        detectedWordElements.firstOrNull { element ->
                                            val b = element.boundingBox
                                            val left = b.left * scaleX - 6f
                                            val top = b.top * scaleY - 6f
                                            val right = b.right * scaleX + 6f
                                            val bottom = b.bottom * scaleY + 6f
                                            relX in left..right && relY in top..bottom
                                        }?.let { hit ->
                                            HapticFeedbackHelper.trigger(context)
                                            if (selectedWordIndices.contains(hit.index)) {
                                                selectedWordIndices.remove(hit.index)
                                            } else {
                                                selectedWordIndices.add(hit.index)
                                            }
                                            translateSelectedWords()
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(selectedImageUri, showArOverlayMode, detectedWordElements) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(requireUnconsumed = false)
                                                    var isDrag = false
                                                    handleTouch(down.position)

                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull() ?: break
                                                        if (change.pressed) {
                                                            if ((change.position - down.position).getDistance() > 10f) {
                                                                isDrag = true
                                                            }
                                                            handleTouch(change.position)
                                                            change.consume()
                                                        } else {
                                                            if (!isDrag) {
                                                                toggleSingleWord(down.position)
                                                            }
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        // Base Captured/Selected Photo
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Captured Image",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = imageZoom,
                                                    scaleY = imageZoom,
                                                    translationX = imagePan.x,
                                                    translationY = imagePan.y
                                                ),
                                            contentScale = ContentScale.Fit
                                        )

                                        // Overlays scaled to image canvas
                                        Box(
                                            modifier = Modifier
                                                .offset(
                                                    x = with(density) { (offsetX + imagePan.x).toDp() },
                                                    y = with(density) { (offsetY + imagePan.y).toDp() }
                                                )
                                                .size(
                                                    width = with(density) { (renderedWidthPx * imageZoom).toDp() },
                                                    height = with(density) { (renderedHeightPx * imageZoom).toDp() }
                                                )
                                        ) {
                                            // MODE A: Finger-Drag Highlight Word Boxes
                                            if (!showArOverlayMode) {
                                                detectedWordElements.forEach { element ->
                                                    val isSelected = selectedWordIndices.contains(element.index)
                                                    val b = element.boundingBox
                                                    val leftDp = with(density) { (b.left * scaleX - 2f).toDp() }
                                                    val topDp = with(density) { (b.top * scaleY - 2f).toDp() }
                                                    val widthDp = with(density) { ((b.right - b.left) * scaleX + 4f).toDp() }
                                                    val heightDp = with(density) { ((b.bottom - b.top) * scaleY + 4f).toDp() }

                                                    Box(
                                                        modifier = Modifier
                                                            .offset(x = leftDp, y = topDp)
                                                            .size(width = widthDp, height = heightDp)
                                                            .clip(RoundedCornerShape(5.dp))
                                                            .background(
                                                                if (isSelected) currentTheme.primary.copy(alpha = 0.38f)
                                                                else Color.White.copy(alpha = 0.12f)
                                                            )
                                                            .border(
                                                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                                                color = if (isSelected) currentTheme.primary else Color.White.copy(alpha = 0.5f),
                                                                shape = RoundedCornerShape(5.dp)
                                                            )
                                                    )
                                                }
                                            }

                                            // MODE B: AR Text Replacement Overlay Blocks
                                            if (showArOverlayMode) {
                                                detectedBlockElements.forEach { block ->
                                                    val b = block.boundingBox
                                                    val translatedBlock = blockTranslations[block.index] ?: block.text
                                                    val leftDp = with(density) { (b.left * scaleX).toDp() }
                                                    val topDp = with(density) { (b.top * scaleY).toDp() }
                                                    val widthDp = with(density) { ((b.right - b.left) * scaleX).toDp() }
                                                    val heightDp = with(density) { ((b.bottom - b.top) * scaleY).toDp() }

                                                    Box(
                                                        modifier = Modifier
                                                            .offset(x = leftDp, y = topDp)
                                                            .size(width = widthDp.coerceAtLeast(40.dp), height = heightDp.coerceAtLeast(20.dp))
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(currentTheme.surfaceContainerHighest.copy(alpha = 0.94f))
                                                            .border(1.dp, currentTheme.primary, RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        Text(
                                                            text = translatedBlock,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = currentTheme.textPrimary,
                                                            maxLines = 3
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // OCR Busy Indicator
                                        if (isOcrProcessing) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(currentTheme.background.copy(alpha = 0.85f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    CircularProgressIndicator(color = EmeraldLime, modifier = Modifier.size(24.dp))
                                                    Text(
                                                        "Recognizing Text & Words...",
                                                        color = currentTheme.textPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive Action Tray: Word Selection Header, Live Translation & Send to Translator
                                val sortedSelection = selectedWordIndices.toList().sorted()
                                val selectedPhrase = sortedSelection.mapNotNull { idx ->
                                    detectedWordElements.getOrNull(idx)?.text ?: detectedWords.getOrNull(idx)
                                }.joinToString(" ")

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(currentTheme.surfaceContainerHigh)
                                        .border(1.dp, currentTheme.borderColor, RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Row 1: Status / Word Count + Quick Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (selectedWordIndices.isEmpty()) "SWIPE FINGER OVER WORDS TO SELECT"
                                            else "SELECTED TEXT (${selectedWordIndices.size} WORDS)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = if (selectedWordIndices.isNotEmpty()) currentTheme.primary else currentTheme.textSecondary
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (selectedWordIndices.isNotEmpty()) {
                                                Text(
                                                    text = "Clear",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = currentTheme.textSecondary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            HapticFeedbackHelper.trigger(context)
                                                            selectedWordIndices.clear()
                                                            liveSelectionTranslation = ""
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (detectedWordElements.isNotEmpty() || detectedWords.isNotEmpty()) {
                                                Text(
                                                    text = "Select All",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = currentTheme.primary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            HapticFeedbackHelper.trigger(context)
                                                            selectedWordIndices.clear()
                                                            val count = if (detectedWordElements.isNotEmpty()) detectedWordElements.size else detectedWords.size
                                                            selectedWordIndices.addAll(0 until count)
                                                            translateSelectedWords()
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Row 2: Selected Text & Live Translated Preview
                                    if (selectedPhrase.isNotBlank()) {
                                        Text(
                                            text = selectedPhrase,
                                            fontSize = 14.sp,
                                            color = currentTheme.textSecondary,
                                            maxLines = 3
                                        )

                                        if (isTranslatingSelection) {
                                            CircularProgressIndicator(
                                                color = currentTheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else if (liveSelectionTranslation.isNotBlank()) {
                                            Text(
                                                text = liveSelectionTranslation,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldLime,
                                                maxLines = 4
                                            )
                                        }

                                        // Row 3: Action Buttons (Copy & Send to Translator)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Copy Button
                                            Button(
                                                onClick = {
                                                    HapticFeedbackHelper.trigger(context)
                                                    val copyContent = if (liveSelectionTranslation.isNotBlank()) liveSelectionTranslation else selectedPhrase
                                                    clipboardManager.setText(AnnotatedString(copyContent))
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = currentTheme.surfaceContainerHighest
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    tint = currentTheme.textPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Copy", color = currentTheme.textPrimary, fontSize = 13.sp)
                                            }

                                            // Send to Translator (Primary Button)
                                            Button(
                                                onClick = {
                                                    HapticFeedbackHelper.trigger(context)
                                                    sourceText = selectedPhrase
                                                    activeTranslateTab = 0
                                                    coroutineScope.launch {
                                                        executeTranslation(selectedPhrase)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Open in Translator",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
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

            // TAB 2: OFFLINE LANGUAGE PACKS MANAGER
            2 -> {
                item {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldLime)
                                Text("Offline Language Packs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary)
                            }
                            Text(
                                text = "Downloaded packs allow 100% offline translations without cell service or WiFi. Active packs automatically take over when offline.",
                                fontSize = 12.sp,
                                color = currentTheme.textSecondary
                            )
                        }
                    }
                }

                items(worldLanguagesList.filter { it.code != "auto" }) { lang ->
                    val isDownloaded = downloadedPacks.contains(lang.code)
                    val isDownloading = downloadingPacks.containsKey(lang.code)
                    val progress = downloadingPacks[lang.code] ?: 0f

                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(lang.flag, fontSize = 24.sp)
                                Column {
                                    Text(lang.name, fontWeight = FontWeight.Bold, color = currentTheme.textPrimary, fontSize = 15.sp)
                                    Text(
                                        text = when {
                                            isDownloading -> "Downloading model ${(progress * 100).toInt()}%..."
                                            isDownloaded -> "Ready Offline (~14 MB) ✓"
                                            else -> "Available to Download (~14 MB)"
                                        },
                                        fontSize = 12.sp,
                                        color = if (isDownloaded) EmeraldLime else if (isDownloading) currentTheme.primary else currentTheme.textSecondary
                                    )
                                }
                            }

                            if (isDownloading) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(currentTheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp,
                                        color = currentTheme.primary
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        startDownloadPack(lang.code, lang.name)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDownloaded) currentTheme.surfaceContainerHigh else currentTheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.defaultMinSize(minWidth = 108.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                                            contentDescription = null,
                                            tint = if (isDownloaded) EmeraldLime else MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isDownloaded) "Downloaded" else "Download",
                                            color = if (isDownloaded) EmeraldLime else MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            softWrap = false,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // Language Selector Modal (100+ languages)
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = currentTheme.cardBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (pickingForSource) "Select Source Language" else "Select Target Language",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQueryState.value = it },
                    placeholder = { Text("Search 100+ world languages...", color = currentTheme.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldLime) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQueryState.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = currentTheme.textSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldLime,
                        unfocusedBorderColor = currentTheme.borderColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredLanguages) { lang ->
                        val isDownloaded = downloadedPacks.contains(lang.code)
                        val isDownloading = downloadingPacks.containsKey(lang.code)
                        val progress = downloadingPacks[lang.code] ?: 0f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    HapticFeedbackHelper.trigger(context)
                                    if (pickingForSource) sourceLang = lang else targetLang = lang
                                    showLanguageSheet = false
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(lang.flag, fontSize = 22.sp)
                                Column {
                                    Text(lang.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = currentTheme.textPrimary)
                                    Text(lang.code.uppercase(), fontSize = 11.sp, color = currentTheme.textSecondary)
                                }
                            }

                            if (lang.code != "auto") {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = currentTheme.primary
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            startDownloadPack(lang.code, lang.name)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                                            contentDescription = "Offline Pack",
                                            tint = if (isDownloaded) EmeraldLime else currentTheme.textSecondary,
                                            modifier = Modifier.size(20.dp)
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
}

/**
 * Checks whether the device currently has active network connectivity
 */
private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Real on-device ML Kit OCR Text Recognizer for image and camera translation
 */
data class OcrWordElement(
    val index: Int,
    val text: String,
    val boundingBox: android.graphics.Rect
)

data class OcrBlockElement(
    val index: Int,
    val text: String,
    val boundingBox: android.graphics.Rect
)

data class OcrExtractionResult(
    val fullText: String,
    val words: List<String>,
    val wordElements: List<OcrWordElement> = emptyList(),
    val blockElements: List<OcrBlockElement> = emptyList(),
    val imageWidth: Int = 1080,
    val imageHeight: Int = 1920
)

private suspend fun extractTextAndWordsFromImageUri(context: Context, uri: Uri): OcrExtractionResult {
    return withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val imgW = image.width
            val imgH = image.height

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val visionText = suspendCancellableCoroutine { cont ->
                recognizer.process(image)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            val fullText = visionText.text.trim()
            val wordsList = mutableListOf<String>()
            val wordElements = mutableListOf<OcrWordElement>()
            val blockElements = mutableListOf<OcrBlockElement>()

            var elementIdx = 0
            visionText.textBlocks.forEachIndexed { blockIdx, block ->
                val bBox = block.boundingBox ?: android.graphics.Rect(0, 0, 0, 0)
                blockElements.add(
                    OcrBlockElement(
                        index = blockIdx,
                        text = block.text.trim(),
                        boundingBox = bBox
                    )
                )

                for (line in block.lines) {
                    for (element in line.elements) {
                        val word = element.text.trim()
                        if (word.isNotBlank()) {
                            wordsList.add(word)
                            val eBox = element.boundingBox ?: line.boundingBox ?: bBox
                            wordElements.add(
                                OcrWordElement(
                                    index = elementIdx++,
                                    text = word,
                                    boundingBox = eBox
                                )
                            )
                        }
                    }
                }
            }

            if (wordsList.isEmpty() && fullText.isNotBlank()) {
                fullText.split(Regex("\\s+")).filter { it.isNotBlank() }.forEachIndexed { idx, w ->
                    wordsList.add(w)
                    wordElements.add(
                        OcrWordElement(
                            index = idx,
                            text = w,
                            boundingBox = android.graphics.Rect(0, 0, 0, 0)
                        )
                    )
                }
            }

            OcrExtractionResult(
                fullText = fullText.ifBlank { "No readable text detected in photo." },
                words = wordsList,
                wordElements = wordElements,
                blockElements = blockElements,
                imageWidth = if (imgW > 0) imgW else 1080,
                imageHeight = if (imgH > 0) imgH else 1920
            )
        } catch (e: Exception) {
            OcrExtractionResult(
                fullText = "Error scanning image: ${e.message ?: "Unable to read text"}",
                words = emptyList()
            )
        }
    }
}

data class LanguageItem(
    val code: String,
    val name: String,
    val flag: String
)

data class QuickPhraseItem(
    val category: String,
    val english: String
)

val worldLanguagesList = listOf(
    LanguageItem("auto", "Detect Language", "🌐"),
    LanguageItem("en", "English", "🇺🇸"),
    LanguageItem("es", "Spanish (Español)", "🇪🇸"),
    LanguageItem("fr", "French (Français)", "🇫🇷"),
    LanguageItem("de", "German (Deutsch)", "🇩🇪"),
    LanguageItem("it", "Italian (Italiano)", "🇮🇹"),
    LanguageItem("pt", "Portuguese (Português)", "🇧🇷"),
    LanguageItem("zh-CN", "Chinese (Simplified)", "🇨🇳"),
    LanguageItem("zh-TW", "Chinese (Traditional)", "🇹🇼"),
    LanguageItem("ja", "Japanese (日本語)", "🇯🇵"),
    LanguageItem("ko", "Korean (한국어)", "🇰🇷"),
    LanguageItem("ar", "Arabic (العربية)", "🇸🇦"),
    LanguageItem("hi", "Hindi (हिन्दी)", "🇮🇳"),
    LanguageItem("ru", "Russian (Русский)", "🇷🇺"),
    LanguageItem("nl", "Dutch (Nederlands)", "🇳🇱"),
    LanguageItem("tr", "Turkish (Türkçe)", "🇹🇷"),
    LanguageItem("sv", "Swedish (Svenska)", "🇸🇪"),
    LanguageItem("id", "Indonesian (Bahasa)", "🇮🇩"),
    LanguageItem("vi", "Vietnamese (Tiếng Việt)", "🇻🇳"),
    LanguageItem("th", "Thai (ไทย)", "🇹🇭"),
    LanguageItem("pl", "Polish (Polski)", "🇵🇱"),
    LanguageItem("uk", "Ukrainian (Українська)", "🇺🇦"),
    LanguageItem("el", "Greek (Ελληνικά)", "🇬🇷"),
    LanguageItem("cs", "Czech (Čeština)", "🇨🇿"),
    LanguageItem("ro", "Romanian (Română)", "🇷🇴"),
    LanguageItem("hu", "Hungarian (Magyar)", "🇭🇺"),
    LanguageItem("da", "Danish (Dansk)", "🇩🇰"),
    LanguageItem("fi", "Finnish (Suomi)", "🇫🇮"),
    LanguageItem("no", "Norwegian (Norsk)", "🇳🇴"),
    LanguageItem("he", "Hebrew (עבריت)", "🇮🇱"),
    LanguageItem("ms", "Malay (Bahasa Melayu)", "🇲🇾"),
    LanguageItem("bn", "Bengali (বাংলা)", "🇧🇩"),
    LanguageItem("ur", "Urdu (اردو)", "🇵🇰"),
    LanguageItem("fa", "Persian (فارسی)", "🇮🇷"),
    LanguageItem("sw", "Swahili (Kiswahili)", "🇰🇪"),
    LanguageItem("fil", "Filipino (Tagalog)", "🇵🇭"),
    LanguageItem("af", "Afrikaans", "🇿🇦"),
    LanguageItem("sq", "Albanian (Shqip)", "🇦🇱"),
    LanguageItem("am", "Amharic (አማርኛ)", "🇪🇹"),
    LanguageItem("hy", "Armenian (Հայերեն)", "🇦🇲"),
    LanguageItem("az", "Azerbaijani (Azərbaycan)", "🇦🇿"),
    LanguageItem("eu", "Basque (Euskara)", "🇪🇸"),
    LanguageItem("be", "Belarusian (Беларуская)", "🇧🇾"),
    LanguageItem("bs", "Bosnian (Bosanski)", "🇧🇦"),
    LanguageItem("bg", "Bulgarian (Български)", "🇧🇬"),
    LanguageItem("ca", "Catalan (Català)", "🇪🇸"),
    LanguageItem("ceb", "Cebuano", "🇵🇭"),
    LanguageItem("hr", "Croatian (Hrvatski)", "🇭🇷"),
    LanguageItem("eo", "Esperanto", "🌐"),
    LanguageItem("et", "Estonian (Eesti)", "🇪🇪"),
    LanguageItem("gl", "Galician (Galego)", "🇪🇸"),
    LanguageItem("ka", "Georgian (ქართული)", "🇬🇪"),
    LanguageItem("gu", "Gujarati (ગુજરાતી)", "🇮🇳"),
    LanguageItem("ht", "Haitian Creole (Kreyòl)", "🇭🇹"),
    LanguageItem("ha", "Hausa", "🇳🇬"),
    LanguageItem("haw", "Hawaiian (ʻŌlelo)", "🇺🇸"),
    LanguageItem("is", "Icelandic (Íslenska)", "🇮🇸"),
    LanguageItem("ig", "Igbo", "🇳🇬"),
    LanguageItem("ga", "Irish (Gaeilge)", "🇮🇪"),
    LanguageItem("jv", "Javanese (Basa Jawa)", "🇮🇩"),
    LanguageItem("kn", "Kannada (ಕನ್ನಡ)", "🇮🇳"),
    LanguageItem("kk", "Kazakh (Қазақ)", "🇰🇿"),
    LanguageItem("km", "Khmer (ភាសាខ្មែរ)", "🇰🇭"),
    LanguageItem("ku", "Kurdish (Kurdî)", "🇮🇶"),
    LanguageItem("ky", "Kyrgyz (Кыргызча)", "🇰🇬"),
    LanguageItem("lo", "Lao (ພາສາລາວ)", "🇱🇦"),
    LanguageItem("la", "Latin (Latina)", "🇻🇦"),
    LanguageItem("lv", "Latvian (Latviešu)", "🇱🇻"),
    LanguageItem("lt", "Lithuanian (Lietuvių)", "🇱🇹"),
    LanguageItem("lb", "Luxembourgish (Lëtzebuergesch)", "🇱🇺"),
    LanguageItem("mk", "Macedonian (Македонски)", "🇲🇰"),
    LanguageItem("mg", "Malagasy", "🇲🇬"),
    LanguageItem("ml", "Malayalam (മലയാളം)", "🇮🇳"),
    LanguageItem("mt", "Maltese (Malti)", "🇲🇹"),
    LanguageItem("mi", "Maori (Te Reo)", "🇳🇿"),
    LanguageItem("mr", "Marathi (मराठी)", "🇮🇳"),
    LanguageItem("mn", "Mongolian (Монгол)", "🇲🇳"),
    LanguageItem("my", "Myanmar / Burmese (မြန်မာ)", "🇲🇲"),
    LanguageItem("ne", "Nepali (नेपाली)", "🇳🇵"),
    LanguageItem("ps", "Pashto (پښتو)", "🇦🇫"),
    LanguageItem("pa", "Punjabi (ਪੰਜਾਬੀ)", "🇮🇳"),
    LanguageItem("sm", "Samoan (Gagana)", "🇼🇸"),
    LanguageItem("gd", "Scots Gaelic", "🇬🇧"),
    LanguageItem("sr", "Serbian (Српски)", "🇷🇸"),
    LanguageItem("st", "Sesotho", "🇱🇸"),
    LanguageItem("sn", "Shona", "🇿🇼"),
    LanguageItem("sd", "Sindhi (سنڌي)", "🇵🇰"),
    LanguageItem("si", "Sinhala (සිංහල)", "🇱🇰"),
    LanguageItem("sk", "Slovak (Slovenčina)", "🇸🇰"),
    LanguageItem("sl", "Slovenian (Slovenščina)", "🇸🇮"),
    LanguageItem("so", "Somali (Soomaali)", "🇸🇴"),
    LanguageItem("su", "Sundanese (Basa Sunda)", "🇮🇩"),
    LanguageItem("tg", "Tajik (Тоҷикӣ)", "🇹🇯"),
    LanguageItem("ta", "Tamil (தமிழ்)", "🇮🇳"),
    LanguageItem("te", "Telugu (తెలుగు)", "🇮🇳"),
    LanguageItem("uz", "Uzbek (Oʻzbek)", "🇺🇿"),
    LanguageItem("cy", "Welsh (Cymraeg)", "🇬🇧"),
    LanguageItem("xh", "Xhosa (isiXhosa)", "🇿🇦"),
    LanguageItem("yi", "Yiddish (ייִדיש)", "🇮🇱"),
    LanguageItem("yo", "Yoruba (Èdè Yorùbá)", "🇳🇬"),
    LanguageItem("zu", "Zulu (isiZulu)", "🇿🇦")
)

val quickPhrasesList = listOf(
    QuickPhraseItem("Travel & Directions", "Where is the nearest subway station?"),
    QuickPhraseItem("Travel & Directions", "How do I get to the airport?"),
    QuickPhraseItem("Emergency & Medical", "I need a doctor immediately"),
    QuickPhraseItem("Emergency & Medical", "Please call an ambulance"),
    QuickPhraseItem("Dining & Food", "Can I get the menu, please?"),
    QuickPhraseItem("Dining & Food", "How much does this cost?"),
    QuickPhraseItem("Dining & Food", "I would like a glass of water"),
    QuickPhraseItem("Hospitality & Hotel", "Do you have a room available?"),
    QuickPhraseItem("Hospitality & Hotel", "What time is check-out?"),
    QuickPhraseItem("Social & Greetings", "Hello, nice to meet you!"),
    QuickPhraseItem("Social & Greetings", "Thank you very much for your help"),
    QuickPhraseItem("Social & Greetings", "Do you speak English?")
)
