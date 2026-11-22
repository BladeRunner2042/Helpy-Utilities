package com.example.helpyutilities.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineTranslatorTest {

    @Test
    fun testDirectDictionaryTranslation() {
        val resultEs = OfflineTranslator.translateOffline("hello", "es")
        assertEquals("Hola", resultEs)

        val resultFr = OfflineTranslator.translateOffline("thank you", "fr")
        assertEquals("Merci", resultFr)

        val resultDe = OfflineTranslator.translateOffline("water", "de")
        assertEquals("Wasser", resultDe)

        val resultJa = OfflineTranslator.translateOffline("hello", "ja")
        assertEquals("こんにちは", resultJa)
    }

    @Test
    fun testSentencePatternTemplateTranslation() {
        val patternResult = OfflineTranslator.translateOffline("where is the bathroom", "es")
        assertNotNull(patternResult)
        assertTrue(patternResult!!.contains("Dónde está"))

        val patternResultFr = OfflineTranslator.translateOffline("where is the hotel", "fr")
        assertNotNull(patternResultFr)
        assertTrue(patternResultFr!!.contains("Où se trouve"))
    }

    @Test
    fun testWordByWordTranslation() {
        val phraseResult = OfflineTranslator.translateOffline("hello water", "es")
        assertNotNull(phraseResult)
        assertEquals("Hola Agua", phraseResult)
    }
}
