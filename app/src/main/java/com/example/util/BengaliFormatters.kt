package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

object BengaliFormatters {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun normalizeDigitsToEnglish(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = banglaDigits.indexOf(ch)
            if (idx >= 0) {
                sb.append(('0'.code + idx).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun parseFlexibleDouble(raw: String): Double? {
        val cleaned = normalizeDigitsToEnglish(raw)
            .replace(",", "")
            .replace("৳", "")
            .trim()
        return cleaned.toDoubleOrNull()
    }

    fun parseFlexibleLong(raw: String): Long? {
        val cleaned = normalizeDigitsToEnglish(raw)
            .replace(",", "")
            .trim()
        return cleaned.toLongOrNull()
    }

    fun formatTaka(amount: Double, useBengaliDigits: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            minimumFractionDigits = 0
        }
        val formatted = "৳${formatter.format(amount)}"
        return if (useBengaliDigits) toBengaliDigits(formatted) else formatted
    }

    fun formatNumber(value: Double, useBengaliDigits: Boolean = true): String {
        val str = if (value % 1.0 == 0.0) {
            value.roundToLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
        }
        return if (useBengaliDigits) toBengaliDigits(str) else str
    }

    fun formatKg(weightKg: Double): String {
        return "${formatNumber(weightKg)} কেজি"
    }

    fun formatDateLabel(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return toBengaliDigits(sdf.format(Date(millis)))
    }

    fun formatDateTimeForFile(millis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
        return sdf.format(Date(millis))
    }

    fun addDays(millis: Long, days: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }

    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
