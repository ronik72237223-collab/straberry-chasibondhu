package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSoftBg
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.util.BengaliFormatters
import com.example.util.ExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorBottomSheet(
    onDismiss: () -> Unit,
    onCopyResult: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }

    fun evaluate(expr: String): String {
        if (expr.isBlank()) return "0"
        return try {
            val evaluated = evalSimpleMath(expr)
            if (evaluated % 1.0 == 0.0) {
                evaluated.toLong().toString()
            } else {
                String.format(java.util.Locale.US, "%.2f", evaluated).trimEnd('0').trimEnd('.')
            }
        } catch (_: Exception) {
            resultText
        }
    }

    fun appendToken(token: String) {
        val next = when {
            token in listOf("+", "-", "×", "÷") && expression.isEmpty() -> ""
            token in listOf("+", "-", "×", "÷") && expression.lastOrNull() in listOf('+', '-', '×', '÷') -> {
                expression.dropLast(1) + token
            }
            else -> expression + token
        }
        expression = next
        resultText = evaluate(next)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgOffWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "বড় বাটনের সহজ ক্যালকুলেটর",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextDarkBlack,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = TextDarkBlack)
                }
            }

            // Quick Farmer Hint Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { expression = "5×250"; resultText = evaluate("5×250") },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.dp, BorderSlate),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("উদাহরণ: ৫ কেজি × ২৫০", color = TextDarkBlack, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { expression = "4×500"; resultText = evaluate("4×500") },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.dp, BorderSlate),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("উদাহরণ: ৪ জন × ৫০০", color = TextDarkBlack, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SurfacePureWhite,
                border = BorderStroke(1.5.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = BengaliFormatters.toBengaliDigits(expression.ifBlank { "০" }),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextSecondaryDark
                        ),
                        maxLines = 2,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "= ৳${BengaliFormatters.toBengaliDigits(resultText)}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 34.sp
                        ),
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("calculator_result_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1-Tap Copy & Use in Expense Form Button
            Button(
                onClick = {
                    val cleanNumeric = resultText
                    ExportHelper.copyToClipboard(context, "Calculator Result", cleanNumeric)
                    onCopyResult(cleanNumeric)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .testTag("calculator_copy_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "কপি করুন")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "হিসাব কপি করে খরচে বসান (${BengaliFormatters.toBengaliDigits(resultText)})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val rows = listOf(
                listOf("C", "⌫", "÷", "×"),
                listOf("7", "8", "9", "-"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "="),
                listOf("0", "00", ".", "=")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEachIndexed { rowIndex, rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEachIndexed { colIndex, key ->
                            val isEquals = key == "="
                            if (rowIndex == 3 && colIndex == 3) {
                                // Show 00 or %? Let's keep = in row 4 and put "%" or "×100" or clear. Actually clean 4x4 + bottom row is super easy!
                            }
                            val bgColor = when (key) {
                                "C" -> StrawberrySoftBg
                                "⌫" -> AmberSoftBg
                                "÷", "×", "-", "+" -> EmeraldSoftBg
                                "=" -> StrawberryRed
                                else -> SurfacePureWhite
                            }
                            val txtColor = when (key) {
                                "=" -> Color.White
                                "C" -> StrawberryRed
                                else -> TextDarkBlack
                            }
                            val borderColor = when (key) {
                                "=" -> StrawberryRed
                                "÷", "×", "-", "+" -> EmeraldBorder
                                else -> BorderSlate
                            }
                            val displayLabel = BengaliFormatters.toBengaliDigits(key)

                            Surface(
                                onClick = {
                                    when (key) {
                                        "C" -> {
                                            expression = ""
                                            resultText = "0"
                                        }
                                        "⌫" -> {
                                            expression = expression.dropLast(1)
                                            resultText = evaluate(expression)
                                        }
                                        "=" -> {
                                            val finalVal = evaluate(expression)
                                            resultText = finalVal
                                            expression = finalVal
                                        }
                                        else -> appendToken(key)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = bgColor,
                                border = BorderStroke(1.dp, borderColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .testTag("calc_btn_$key")
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (key == "⌫") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "মুছুন",
                                            tint = TextDarkBlack,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = displayLabel,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txtColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun evalSimpleMath(expr: String): Double {
    val sanitized = expr.replace("×", "*").replace("÷", "/")
    val tokens = mutableListOf<String>()
    var numBuf = StringBuilder()
    for (ch in sanitized) {
        if (ch.isDigit() || ch == '.') {
            numBuf.append(ch)
        } else if (ch in listOf('+', '-', '*', '/')) {
            if (numBuf.isNotEmpty()) {
                tokens.add(numBuf.toString())
                numBuf = StringBuilder()
            }
            tokens.add(ch.toString())
        }
    }
    if (numBuf.isNotEmpty()) tokens.add(numBuf.toString())
    if (tokens.isEmpty()) return 0.0

    // Trim trailing operator
    if (tokens.last() in listOf("+", "-", "*", "/")) {
        tokens.removeAt(tokens.lastIndex)
    }
    if (tokens.isEmpty()) return 0.0

    // Pass 1: * and /
    val pass1 = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val tok = tokens[i]
        if ((tok == "*" || tok == "/") && pass1.isNotEmpty() && i + 1 < tokens.size) {
            val left = pass1.removeAt(pass1.lastIndex).toDoubleOrNull() ?: 0.0
            val right = tokens[i + 1].toDoubleOrNull() ?: 0.0
            val res = if (tok == "*") left * right else if (right != 0.0) left / right else 0.0
            pass1.add(res.toString())
            i += 2
        } else {
            pass1.add(tok)
            i += 1
        }
    }

    // Pass 2: + and -
    var total = pass1.firstOrNull()?.toDoubleOrNull() ?: 0.0
    var j = 1
    while (j + 1 < pass1.size) {
        val op = pass1[j]
        val right = pass1[j + 1].toDoubleOrNull() ?: 0.0
        if (op == "+") total += right
        else if (op == "-") total -= right
        j += 2
    }
    return total
}
