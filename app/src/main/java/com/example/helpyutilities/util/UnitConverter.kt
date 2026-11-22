package com.example.helpyutilities.util

import java.util.Locale

object UnitConverter {

    enum class Category(val displayName: String, val icon: String) {
        LENGTH("Length", "📏"),
        WEIGHT("Weight / Mass", "⚖️"),
        TEMPERATURE("Temperature", "🌡️"),
        SPEED("Speed", "🚗"),
        STORAGE("Digital Storage", "💾"),
        VOLUME("Volume", "🧪"),
        AREA("Area", "📐"),
        TIME("Time", "⏱️")
    }

    data class UnitDef(val id: String, val name: String, val symbol: String, val factorToStandard: Double)

    fun getUnitsForCategory(category: Category): List<UnitDef> {
        return when (category) {
            Category.LENGTH -> listOf(
                UnitDef("m", "Meters", "m", 1.0),
                UnitDef("km", "Kilometers", "km", 1000.0),
                UnitDef("cm", "Centimeters", "cm", 0.01),
                UnitDef("mm", "Millimeters", "mm", 0.001),
                UnitDef("mi", "Miles", "mi", 1609.344),
                UnitDef("yd", "Yards", "yd", 0.9144),
                UnitDef("ft", "Feet", "ft", 0.3048),
                UnitDef("in", "Inches", "in", 0.0254)
            )
            Category.WEIGHT -> listOf(
                UnitDef("kg", "Kilograms", "kg", 1.0),
                UnitDef("g", "Grams", "g", 0.001),
                UnitDef("mg", "Milligrams", "mg", 0.000001),
                UnitDef("lb", "Pounds", "lbs", 0.45359237),
                UnitDef("oz", "Ounces", "oz", 0.02834952),
                UnitDef("t", "Metric Tons", "t", 1000.0)
            )
            Category.TEMPERATURE -> listOf(
                UnitDef("c", "Celsius", "°C", 1.0),
                UnitDef("f", "Fahrenheit", "°F", 1.0),
                UnitDef("k", "Kelvin", "K", 1.0)
            )
            Category.SPEED -> listOf(
                UnitDef("mps", "Meters per second", "m/s", 1.0),
                UnitDef("kph", "Kilometers per hour", "km/h", 0.277778),
                UnitDef("mph", "Miles per hour", "mph", 0.44704),
                UnitDef("knot", "Knots", "kn", 0.514444)
            )
            Category.STORAGE -> listOf(
                UnitDef("b", "Bytes", "B", 1.0),
                UnitDef("kb", "Kilobytes", "KB", 1024.0),
                UnitDef("mb", "Megabytes", "MB", 1024.0 * 1024.0),
                UnitDef("gb", "Gigabytes", "GB", 1024.0 * 1024.0 * 1024.0),
                UnitDef("tb", "Terabytes", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
            )
            Category.VOLUME -> listOf(
                UnitDef("l", "Liters", "L", 1.0),
                UnitDef("ml", "Milliliters", "mL", 0.001),
                UnitDef("gal", "Gallons (US)", "gal", 3.78541),
                UnitDef("qt", "Quarts (US)", "qt", 0.946353),
                UnitDef("cup", "Cups (US)", "cup", 0.24),
                UnitDef("floz", "Fluid Ounces", "fl oz", 0.0295735)
            )
            Category.AREA -> listOf(
                UnitDef("sqm", "Square Meters", "m²", 1.0),
                UnitDef("sqkm", "Square Kilometers", "km²", 1000000.0),
                UnitDef("sqft", "Square Feet", "ft²", 0.092903),
                UnitDef("sqmi", "Square Miles", "mi²", 2589988.11),
                UnitDef("acre", "Acres", "ac", 4046.86),
                UnitDef("ha", "Hectares", "ha", 10000.0)
            )
            Category.TIME -> listOf(
                UnitDef("s", "Seconds", "s", 1.0),
                UnitDef("min", "Minutes", "min", 60.0),
                UnitDef("hr", "Hours", "hr", 3600.0),
                UnitDef("day", "Days", "d", 86400.0),
                UnitDef("wk", "Weeks", "wk", 604800.0)
            )
        }
    }

    fun convert(value: Double, fromUnit: UnitDef, toUnit: UnitDef, category: Category): Double {
        if (category == Category.TEMPERATURE) {
            return convertTemperature(value, fromUnit.id, toUnit.id)
        }
        val inStandard = value * fromUnit.factorToStandard
        return inStandard / toUnit.factorToStandard
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): Double {
        val inCelsius = when (fromId) {
            "c" -> value
            "f" -> (value - 32.0) * (5.0 / 9.0)
            "k" -> value - 273.15
            else -> value
        }

        return when (toId) {
            "c" -> inCelsius
            "f" -> (inCelsius * 9.0 / 5.0) + 32.0
            "k" -> inCelsius + 273.15
            else -> inCelsius
        }
    }

    fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble() && kotlin.math.abs(value) < 1e9) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
    }
}
