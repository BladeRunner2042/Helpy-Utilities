package com.example.helpyutilities

import com.example.helpyutilities.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testLengthConversion() {
        val lengthUnits = UnitConverter.getUnitsForCategory(UnitConverter.Category.LENGTH)
        val meters = lengthUnits.first { it.id == "m" }
        val centimeters = lengthUnits.first { it.id == "cm" }
        val kilometers = lengthUnits.first { it.id == "km" }
        val feet = lengthUnits.first { it.id == "ft" }
        val inches = lengthUnits.first { it.id == "in" }

        // 1 meter = 100 cm
        val resultCm = UnitConverter.convert(1.0, meters, centimeters, UnitConverter.Category.LENGTH)
        assertEquals(100.0, resultCm, 0.001)

        // 1 kilometer = 1000 meters
        val resultMeters = UnitConverter.convert(1.0, kilometers, meters, UnitConverter.Category.LENGTH)
        assertEquals(1000.0, resultMeters, 0.001)

        // 1 foot = 12 inches
        val resultInches = UnitConverter.convert(1.0, feet, inches, UnitConverter.Category.LENGTH)
        assertEquals(12.0, resultInches, 0.01)
    }

    @Test
    fun testWeightConversion() {
        val weightUnits = UnitConverter.getUnitsForCategory(UnitConverter.Category.WEIGHT)
        val kg = weightUnits.first { it.id == "kg" }
        val g = weightUnits.first { it.id == "g" }
        val lb = weightUnits.first { it.id == "lb" }

        // 1 kilogram = 1000 grams
        val resultG = UnitConverter.convert(1.0, kg, g, UnitConverter.Category.WEIGHT)
        assertEquals(1000.0, resultG, 0.001)

        // 1 kilogram ~ 2.20462 pounds
        val resultLb = UnitConverter.convert(1.0, kg, lb, UnitConverter.Category.WEIGHT)
        assertEquals(2.20462, resultLb, 0.01)
    }

    @Test
    fun testTemperatureConversion() {
        val tempUnits = UnitConverter.getUnitsForCategory(UnitConverter.Category.TEMPERATURE)
        val c = tempUnits.first { it.id == "c" }
        val f = tempUnits.first { it.id == "f" }
        val k = tempUnits.first { it.id == "k" }

        // 0 Celsius = 32 Fahrenheit
        val resultF = UnitConverter.convert(0.0, c, f, UnitConverter.Category.TEMPERATURE)
        assertEquals(32.0, resultF, 0.01)

        // 100 Celsius = 212 Fahrenheit
        val resultBoil = UnitConverter.convert(100.0, c, f, UnitConverter.Category.TEMPERATURE)
        assertEquals(212.0, resultBoil, 0.01)

        // 0 Celsius = 273.15 Kelvin
        val resultK = UnitConverter.convert(0.0, c, k, UnitConverter.Category.TEMPERATURE)
        assertEquals(273.15, resultK, 0.01)
    }

    @Test
    fun testDigitalStorageConversion() {
        val storageUnits = UnitConverter.getUnitsForCategory(UnitConverter.Category.STORAGE)
        val gb = storageUnits.first { it.id == "gb" }
        val mb = storageUnits.first { it.id == "mb" }
        val kb = storageUnits.first { it.id == "kb" }

        // 1 Gigabyte = 1024 Megabytes
        val resultMb = UnitConverter.convert(1.0, gb, mb, UnitConverter.Category.STORAGE)
        assertEquals(1024.0, resultMb, 0.01)

        // 1 Megabyte = 1024 Kilobytes
        val resultKb = UnitConverter.convert(1.0, mb, kb, UnitConverter.Category.STORAGE)
        assertEquals(1024.0, resultKb, 0.01)
    }
}
