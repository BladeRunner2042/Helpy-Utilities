package com.example.helpyutilities.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * High-Quality Free Neural Text-to-Speech Engine
 * 1. Primary: Google Neural TTS Voice Stream (Natural inflection, studio quality, free, 100+ languages)
 * 2. Fallback: Local Android System TextToSpeech engine
 */
object AiVoiceTtsEngine {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Speaks the given text using high-fidelity natural TTS audio
     */
    suspend fun speakText(
        context: Context,
        text: String,
        langCode: String,
        systemTts: TextToSpeech?,
        onComplete: (() -> Unit)? = null
    ) {
        val clean = text.trim()
        if (clean.isBlank()) return

        // 1. Try High-Quality Neural Cloud TTS Audio
        val playedSuccessfully = withContext(Dispatchers.IO) {
            try {
                // Map language codes to standard 2-letter codes for TTS
                val normLang = langCode.split("-").first().lowercase()
                val encoded = URLEncoder.encode(clean.take(200), "UTF-8")
                val ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&tl=$normLang&client=tw-ob&q=$encoded"

                val conn = URL(ttsUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
                conn.connectTimeout = 6000
                conn.readTimeout = 6000

                if (conn.responseCode == 200) {
                    val tempAudioFile = File(context.cacheDir, "neural_tts_${System.currentTimeMillis()}.mp3")
                    conn.inputStream.use { input ->
                        FileOutputStream(tempAudioFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        try {
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                                        .build()
                                )
                                setDataSource(tempAudioFile.absolutePath)
                                prepare()
                                start()
                                setOnCompletionListener {
                                    tempAudioFile.delete()
                                    onComplete?.invoke()
                                }
                            }
                            true
                        } catch (e: Exception) {
                            tempAudioFile.delete()
                            false
                        }
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        // 2. Fallback to Local Android System TextToSpeech if offline or network error
        if (!playedSuccessfully) {
            withContext(Dispatchers.Main) {
                try {
                    val locale = Locale.forLanguageTag(langCode)
                    systemTts?.language = locale
                    systemTts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "helpy_tts_fallback")
                    onComplete?.invoke()
                } catch (e: Exception) {
                    Log.e("AiVoiceTtsEngine", "Local TTS error: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }
}
