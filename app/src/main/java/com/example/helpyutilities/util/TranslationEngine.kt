package com.example.helpyutilities.util

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Universal High-Reliability Translation Engine
 * 1. Primary: Google Translate Client API (free, instant, 100+ languages, real grammatical sentences)
 * 2. Secondary: MyMemory Translation API with User-Agent
 * 3. Fallback: On-Device Extensive Phrase & Vocabulary Dictionary
 */
object TranslationEngine {

    suspend fun translate(
        text: String,
        sourceCode: String,
        targetCode: String
    ): Pair<String, String> {
        val clean = text.trim()
        if (clean.isBlank()) return "" to ""

        // Normalize source code (auto -> auto)
        val src = if (sourceCode.isBlank() || sourceCode == "auto") "auto" else sourceCode.split("-").first().lowercase()
        val tgt = targetCode.split("-").first().lowercase()

        // 1. Try Google Translate Client API
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val gUrl = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$src&tl=$tgt&dt=t&q=$encoded"
            val conn = URL(gUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            conn.connectTimeout = 6000
            conn.readTimeout = 6000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val responseStr = reader.readText()
                reader.close()

                val rootArray = JSONArray(responseStr)
                val sentencesArray = rootArray.getJSONArray(0)
                val sb = StringBuilder()
                for (i in 0 until sentencesArray.length()) {
                    val sentence = sentencesArray.getJSONArray(i)
                    sb.append(sentence.getString(0))
                }
                val translated = sb.toString().trim()
                if (translated.isNotBlank()) {
                    return translated to "Google Translate Engine ✓"
                }
            }
        } catch (_: Exception) {
            // Fall through to secondary
        }

        // 2. Try MyMemory API with proper User-Agent
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val langPair = "${if (src == "auto") "" else src}|$tgt"
            val mmUrl = "https://api.mymemory.translated.net/get?q=$encoded&langpair=$langPair"
            val conn = URL(mmUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "HelpyUtilitiesApp/1.0")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val responseStr = reader.readText()
                reader.close()

                val json = JSONObject(responseStr)
                if (json.has("responseData")) {
                    val result = json.getJSONObject("responseData").getString("translatedText")
                    if (result.isNotBlank() && !result.startsWith("MYMEMORY WARNING")) {
                        return result to "MyMemory Cloud Engine ✓"
                    }
                }
            }
        } catch (_: Exception) {
            // Fall through to offline
        }

        // 3. Fallback to On-Device Offline Dictionary
        val offline = OfflineTranslator.translateOffline(clean, targetCode)
        if (offline != null && offline.isNotBlank()) {
            return offline to "Offline On-Device Engine ⚡"
        }

        // 4. Return formatted original
        return clean to "Offline Fallback"
    }
}
