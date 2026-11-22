package com.example.helpyutilities

import com.example.helpyutilities.util.MathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathEvaluatorTest {

    @Test
    fun testBasicArithmetic() {
        val res1 = MathEvaluator.evaluate("2 + 3 * 4")
        assertTrue(res1 is MathEvaluator.EvaluationResult.Success)
        assertEquals(14.0, (res1 as MathEvaluator.EvaluationResult.Success).value, 1e-6)

        val res2 = MathEvaluator.evaluate("(2 + 3) * 4")
        assertTrue(res2 is MathEvaluator.EvaluationResult.Success)
        assertEquals(20.0, (res2 as MathEvaluator.EvaluationResult.Success).value, 1e-6)

        val res3 = MathEvaluator.evaluate("100 / 4 - 5")
        assertTrue(res3 is MathEvaluator.EvaluationResult.Success)
        assertEquals(20.0, (res3 as MathEvaluator.EvaluationResult.Success).value, 1e-6)
    }

    @Test
    fun testPowersAndScientific() {
        val res1 = MathEvaluator.evaluate("2 ^ 3 + 1")
        assertTrue(res1 is MathEvaluator.EvaluationResult.Success)
        assertEquals(9.0, (res1 as MathEvaluator.EvaluationResult.Success).value, 1e-6)

        val res2 = MathEvaluator.evaluate("sqrt(16) + 6")
        assertTrue(res2 is MathEvaluator.EvaluationResult.Success)
        assertEquals(10.0, (res2 as MathEvaluator.EvaluationResult.Success).value, 1e-6)

        val res3 = MathEvaluator.evaluate("sin(90)", inDegrees = true)
        assertTrue(res3 is MathEvaluator.EvaluationResult.Success)
        assertEquals(1.0, (res3 as MathEvaluator.EvaluationResult.Success).value, 1e-6)
    }

    @Test
    fun testFormatting() {
        assertEquals("14", MathEvaluator.formatResult(14.0))
        assertEquals("3.14159", MathEvaluator.formatResult(3.14159265, 5))
    }
}
