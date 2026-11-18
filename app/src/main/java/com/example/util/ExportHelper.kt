package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.FarmSummary
import com.example.model.LedgerEntry
import java.io.File

object ExportHelper {

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun exportCsvAndShare(
        context: Context,
        entries: List<LedgerEntry>,
        summary: FarmSummary,
        onSavedMessage: (String) -> Unit
    ) {
        try {
            val fileName = "strawberry_hisab_${BengaliFormatters.formatDateTimeForFile()}.csv"
            val csvContent = buildCsvContent(entries, summary)
            val utf8BomBytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
                csvContent.toByteArray(Charsets.UTF_8)

            // Save to Downloads via MediaStore on API 29+ (no storage permission required)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StrawberryChashi")
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { out ->
                            out.write(utf8BomBytes)
                        }
                    }
                } catch (_: Exception) {
                    // Fallback to cache file share below
                }
            }

            // Also write to cache/exports so user can open/share in Excel/Sheets immediately
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            file.writeBytes(utf8BomBytes)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            onSavedMessage("এক্সেল ফাইল তৈরি হয়েছে: $fileName")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "স্ট্রবেরি চাষি বন্ধু - আয় ব্যয়ের খাতা")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "এক্সেল ফাইল শেয়ার বা খুলুন").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            onSavedMessage("এক্সেল তৈরি করা যায়নি: ${e.localizedMessage ?: "ত্রুটি"}")
        }
    }

    fun shareWhatsAppReport(
        context: Context,
        summary: FarmSummary,
        farmerName: String,
        onError: (String) -> Unit
    ) {
        try {
            val reportText = buildString {
                appendLine("🍓 *স্ট্রবেরি চাষি বন্ধু - খামার রিপোর্ট*")
                if (farmerName.isNotBlank()) {
                    appendLine("👨‍🌾 চাষি: $farmerName")
                }
                appendLine("📅 তারিখ: ${BengaliFormatters.formatDateLabel(System.currentTimeMillis())}")
                appendLine("━━━━━━━━━━━━━━")
                appendLine("💰 *মোট বিক্রি (আয়):* ${BengaliFormatters.formatTaka(summary.totalSales)}")
                appendLine("⚖️ মোট ফলন: ${BengaliFormatters.formatKg(summary.totalHarvestKg)}")
                appendLine("")
                appendLine("💸 *মোট খরচ:* ${BengaliFormatters.formatTaka(summary.totalExpense)}")
                appendLine("  • সার কেনা: ${BengaliFormatters.formatTaka(summary.fertilizerTotal)} (বাকি: ${BengaliFormatters.formatTaka(summary.fertilizerDue)})")
                appendLine("  • সেচ খরচ: ${BengaliFormatters.formatTaka(summary.irrigationTotal)}")
                appendLine("  • স্প্রে খরচ: ${BengaliFormatters.formatTaka(summary.sprayTotal)}")
                appendLine("  • শ্রমিক মজুরি: ${BengaliFormatters.formatTaka(summary.laborTotal)}")
                appendLine("━━━━━━━━━━━━━━")
                val netLabel = if (summary.netProfit >= 0) "✅ *নিট লাভ:*" else "⚠️ *নিট ঘাটতি:*"
                appendLine("$netLabel ${BengaliFormatters.formatTaka(kotlin.math.abs(summary.netProfit))}")
                if (summary.nextIrrigationLabel.isNotBlank()) {
                    appendLine("💧 পরবর্তী সম্ভাব্য সেচ: ${summary.nextIrrigationLabel} (${summary.nextIrrigationPlot})")
                }
            }

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, reportText)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(whatsappIntent)
            } catch (_: Exception) {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, reportText)
                }
                val chooser = Intent.createChooser(fallback, "রিপোর্ট শেয়ার করুন").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            onError("শেয়ার করা যায়নি: ${e.localizedMessage ?: "ত্রুটি"}")
        }
    }

    private fun buildCsvContent(entries: List<LedgerEntry>, summary: FarmSummary): String {
        val sb = StringBuilder()
        sb.appendLine("তারিখ,ধরন,বিবরণ,বিস্তারিত,আয়/খরচ,অবস্থা,টাকা")
        for (entry in entries.sortedByDescending { it.dateMillis }) {
            val typeStr = if (entry.isIncome) "আয় (বিক্রি)" else "খরচ"
            val statusStr = when {
                entry.isIncome -> "নগদ আয়"
                entry.isDue -> "বাকি"
                else -> "পরিশোধিত/নগদ"
            }
            sb.appendLine(
                listOf(
                    csvEscape(entry.dateLabel),
                    csvEscape(entry.category.bengaliLabel),
                    csvEscape(entry.title),
                    csvEscape(entry.detail),
                    csvEscape(typeStr),
                    csvEscape(statusStr),
                    entry.amount.toString()
                ).joinToString(",")
            )
        }
        sb.appendLine("")
        sb.appendLine("সারসংক্ষেপ,মান")
        sb.appendLine("মোট সার খরচ,${summary.fertilizerTotal}")
        sb.appendLine("মোট বাকি (সার),${summary.fertilizerDue}")
        sb.appendLine("মোট সেচ খরচ,${summary.irrigationTotal}")
        sb.appendLine("মোট স্প্রে খরচ,${summary.sprayTotal}")
        sb.appendLine("মোট শ্রমিক মজুরি,${summary.laborTotal}")
        sb.appendLine("সর্বমোট খরচ,${summary.totalExpense}")
        sb.appendLine("মোট ফলন (কেজি),${summary.totalHarvestKg}")
        sb.appendLine("সর্বমোট বিক্রি (আয়),${summary.totalSales}")
        sb.appendLine("নিট লাভ/ক্ষতি,${summary.netProfit}")
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
