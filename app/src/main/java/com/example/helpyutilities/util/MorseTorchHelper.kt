package com.example.helpyutilities.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

object MorseTorchHelper {

    private val MORSE_MAP = mapOf(
        'A' to ".-",    'B' to "-...",  'C' to "-.-.",  'D' to "-..",
        'E' to ".",     'F' to "..-.",  'G' to "--.",   'H' to "....",
        'I' to "..",    'J' to ".---",  'K' to "-.-",   'L' to ".-..",
        'M' to "--",    'N' to "-.",    'O' to "---",   'P' to ".--.",
        'Q' to "--.-",  'R' to ".-.",   'S' to "...",   'T' to "-",
        'U' to "..-",   'V' to "...-",  'W' to ".--",   'X' to "-..-",
        'Y' to "-.--",  'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--",
        '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----."
    )

    fun textToMorse(text: String): String {
        return text.trim().uppercase().map { ch ->
            when (ch) {
                ' ' -> "/"
                in MORSE_MAP -> MORSE_MAP[ch] ?: ""
                else -> ""
            }
        }.filter { it.isNotEmpty() }.joinToString(" ")
    }

    suspend fun transmitMorse(
        context: Context,
        morseSequence: String,
        unitMs: Long = 180L,
        onSymbolProgress: ((Char, Int) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) { null }

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        fun setTorch(on: Boolean) {
            try {
                if (cameraId != null && cameraManager != null) {
                    cameraManager.setTorchMode(cameraId, on)
                }
            } catch (_: Exception) {}
        }

        fun vibrate(durationMs: Long) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            } catch (_: Exception) {}
        }

        var index = 0
        try {
            while (isActive && index < morseSequence.length) {
                val symbol = morseSequence[index]
                onSymbolProgress?.invoke(symbol, index)

                when (symbol) {
                    '.' -> {
                        setTorch(true)
                        vibrate(unitMs)
                        delay(unitMs)
                        setTorch(false)
                        delay(unitMs) // inter-element gap
                    }
                    '-' -> {
                        setTorch(true)
                        vibrate(unitMs * 3)
                        delay(unitMs * 3)
                        setTorch(false)
                        delay(unitMs) // inter-element gap
                    }
                    ' ' -> {
                        // gap between letters = 3 units (1 already passed)
                        delay(unitMs * 2)
                    }
                    '/' -> {
                        // gap between words = 7 units
                        delay(unitMs * 4)
                    }
                }
                index++
            }
        } finally {
            setTorch(false)
        }
    }
}
