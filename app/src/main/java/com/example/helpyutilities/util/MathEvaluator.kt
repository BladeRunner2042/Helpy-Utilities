package com.example.helpyutilities.util

import java.util.Locale
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Robust mathematical expression parser and evaluator.
 * Supports: +, -, *, /, %, ^, parentheses, negative numbers,
 * functions (sin, cos, tan, sqrt, cbrt, ln, log, abs), constants (pi, e).
 */
object MathEvaluator {

    fun evaluate(expression: String, inDegrees: Boolean = true): EvaluationResult {
        if (expression.isBlank()) return EvaluationResult.Error("Empty expression")

        try {
            val tokens = tokenize(expression)
            if (tokens.isEmpty()) return EvaluationResult.Error("No valid tokens")
            val parser = Parser(tokens, inDegrees)
            val result = parser.parseExpression()
            if (parser.hasMoreTokens()) {
                return EvaluationResult.Error("Unexpected symbol at end")
            }
            if (result.isNaN() || result.isInfinite()) {
                return EvaluationResult.Error("Result undefined")
            }
            return EvaluationResult.Success(result)
        } catch (e: Exception) {
            return EvaluationResult.Error(e.message ?: "Invalid expression")
        }
    }

    fun formatResult(value: Double, maxDecimals: Int = 8): String {
        if (value == value.toLong().toDouble() && !value.isInfinite() && abs(value) < 1e12) {
            return value.toLong().toString()
        }
        val formatted = String.format(Locale.US, "%.${maxDecimals}f", value)
            .trimEnd('0')
            .trimEnd('.')
        return if (formatted == "-0") "0" else formatted
    }

    sealed class EvaluationResult {
        data class Success(val value: Double) : EvaluationResult()
        data class Error(val message: String) : EvaluationResult()
    }

    private enum class TokenType {
        NUMBER, PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, POWER,
        LPAREN, RPAREN, IDENTIFIER
    }

    private data class Token(val type: TokenType, val value: String)

    private fun tokenize(input: String): List<Token> {
        val sanitized = input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("π", "pi")

        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < sanitized.length) {
            val c = sanitized[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    var hasDot = false
                    while (i < sanitized.length && (sanitized[i].isDigit() || (!hasDot && sanitized[i] == '.'))) {
                        if (sanitized[i] == '.') hasDot = true
                        sb.append(sanitized[i])
                        i++
                    }
                    tokens.add(Token(TokenType.NUMBER, sb.toString()))
                }
                c == '+' -> { tokens.add(Token(TokenType.PLUS, "+")); i++ }
                c == '-' -> { tokens.add(Token(TokenType.MINUS, "-")); i++ }
                c == '*' -> { tokens.add(Token(TokenType.MULTIPLY, "*")); i++ }
                c == '/' -> { tokens.add(Token(TokenType.DIVIDE, "/")); i++ }
                c == '%' -> { tokens.add(Token(TokenType.MODULO, "%")); i++ }
                c == '^' -> { tokens.add(Token(TokenType.POWER, "^")); i++ }
                c == '(' -> { tokens.add(Token(TokenType.LPAREN, "(")); i++ }
                c == ')' -> { tokens.add(Token(TokenType.RPAREN, ")")); i++ }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < sanitized.length && sanitized[i].isLetter()) {
                        sb.append(sanitized[i])
                        i++
                    }
                    tokens.add(Token(TokenType.IDENTIFIER, sb.toString().lowercase()))
                }
                else -> i++
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<Token>, private val inDegrees: Boolean) {
        private var pos = 0

        fun hasMoreTokens() = pos < tokens.size

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null

        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): Double {
            var value = parseTerm()
            while (hasMoreTokens()) {
                val next = peek() ?: break
                if (next.type == TokenType.PLUS) {
                    consume()
                    value += parseTerm()
                } else if (next.type == TokenType.MINUS) {
                    consume()
                    value -= parseTerm()
                } else {
                    break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parsePower()
            while (hasMoreTokens()) {
                val next = peek() ?: break
                when (next.type) {
                    TokenType.MULTIPLY -> {
                        consume()
                        value *= parsePower()
                    }
                    TokenType.DIVIDE -> {
                        consume()
                        val divisor = parsePower()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        value /= divisor
                    }
                    TokenType.MODULO -> {
                        consume()
                        val mod = parsePower()
                        if (mod == 0.0) throw ArithmeticException("Modulo by zero")
                        value %= mod
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePower(): Double {
            var value = parseFactor()
            if (hasMoreTokens() && peek()?.type == TokenType.POWER) {
                consume()
                val exponent = parsePower() // Right-associative
                value = value.pow(exponent)
            }
            return value
        }

        private fun parseFactor(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            // Unary + or -
            if (token.type == TokenType.PLUS) {
                consume()
                return parseFactor()
            }
            if (token.type == TokenType.MINUS) {
                consume()
                return -parseFactor()
            }

            return parsePrimary()
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            when (token.type) {
                TokenType.NUMBER -> {
                    consume()
                    return token.value.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: ${token.value}")
                }
                TokenType.LPAREN -> {
                    consume()
                    val result = parseExpression()
                    if (!hasMoreTokens() || consume().type != TokenType.RPAREN) {
                        throw IllegalArgumentException("Missing closing parenthesis")
                    }
                    return result
                }
                TokenType.IDENTIFIER -> {
                    consume()
                    val name = token.value
                    when (name) {
                        "pi" -> return Math.PI
                        "e" -> Math.E
                        "sin", "cos", "tan", "sqrt", "cbrt", "ln", "log", "abs" -> {
                            val arg = if (hasMoreTokens() && peek()?.type == TokenType.LPAREN) {
                                consume() // (
                                val inner = parseExpression()
                                if (!hasMoreTokens() || consume().type != TokenType.RPAREN) {
                                    throw IllegalArgumentException("Missing closing parenthesis for $name")
                                }
                                inner
                            } else {
                                parsePrimary()
                            }

                            return when (name) {
                                "sin" -> {
                                    val rad = if (inDegrees) Math.toRadians(arg) else arg
                                    sin(rad)
                                }
                                "cos" -> {
                                    val rad = if (inDegrees) Math.toRadians(arg) else arg
                                    cos(rad)
                                }
                                "tan" -> {
                                    val rad = if (inDegrees) Math.toRadians(arg) else arg
                                    tan(rad)
                                }
                                "sqrt" -> {
                                    if (arg < 0) throw IllegalArgumentException("Negative square root")
                                    sqrt(arg)
                                }
                                "cbrt" -> cbrt(arg)
                                "ln" -> {
                                    if (arg <= 0) throw IllegalArgumentException("Log of non-positive")
                                    ln(arg)
                                }
                                "log" -> {
                                    if (arg <= 0) throw IllegalArgumentException("Log of non-positive")
                                    log10(arg)
                                }
                                "abs" -> abs(arg)
                                else -> arg
                            }
                        }
                        else -> throw IllegalArgumentException("Unknown identifier: $name")
                    }
                }
                else -> throw IllegalArgumentException("Unexpected token: ${token.value}")
            }
            return 0.0
        }
    }
}
