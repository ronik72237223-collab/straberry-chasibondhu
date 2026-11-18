package com.example

import com.example.util.BengaliFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun bengaliDigitConversionAndParsing_isCorrect() {
        assertEquals("৳১,২৫০", BengaliFormatters.formatTaka(1250.0))
        assertEquals("৫.৫ কেজি", BengaliFormatters.formatKg(5.5))
        assertEquals(450.0, BengaliFormatters.parseFlexibleDouble("৪৫০") ?: 0.0, 0.001)
        assertEquals(12.5, BengaliFormatters.parseFlexibleDouble("১২.৫") ?: 0.0, 0.001)
        assertEquals(5L, BengaliFormatters.parseFlexibleLong("৫"))
        assertNotNull(BengaliFormatters.formatDateLabel(System.currentTimeMillis()))
    }
}
