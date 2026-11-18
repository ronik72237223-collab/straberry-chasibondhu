package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordCategory
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.StrawberryBorder
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealSoftBg
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters
import com.example.util.ExportHelper

@Composable
fun HistoryExportScreen(
    state: FarmDashboardState,
    farmerName: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onNotify: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf<RecordCategory?>(null) }

    val filteredEntries = remember(state.ledgerEntries, selectedFilter) {
        if (selectedFilter == null) state.ledgerEntries
        else state.ledgerEntries.filter { it.category == selectedFilter }
    }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "খাতা ও রিপোর্ট",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Export & WhatsApp Share Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.5.dp, EmeraldBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "রিপোর্ট ডাউনলোড ও শেয়ার করুন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Button(
                            onClick = {
                                ExportHelper.exportCsvAndShare(
                                    context = context,
                                    entries = state.ledgerEntries,
                                    summary = state.summary,
                                    onSavedMessage = onNotify
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("export_excel_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "এক্সেল ডাউনলোড", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("এক্সেল ডাউনলোড করুন (.csv)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                ExportHelper.shareWhatsAppReport(
                                    context = context,
                                    summary = state.summary,
                                    farmerName = farmerName,
                                    onError = onNotify
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("whatsapp_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "হোয়াটসঅ্যাপ শেয়ার", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("হোয়াটসঅ্যাপে রিপোর্ট পাঠান", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("সব (${BengaliFormatters.formatNumber(state.ledgerEntries.size.toDouble())})", color = TextDarkBlack, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealSoftBg,
                            containerColor = SurfacePureWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == null,
                            borderColor = BorderSlate,
                            selectedBorderColor = TealAccent
                        )
                    )
                    RecordCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedFilter == cat,
                            onClick = { selectedFilter = cat },
                            label = { Text(cat.bengaliLabel, color = TextDarkBlack, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealSoftBg,
                                containerColor = SurfacePureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == cat,
                                borderColor = BorderSlate,
                                selectedBorderColor = TealAccent
                            )
                        )
                    }
                }
            }

            if (filteredEntries.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Assessment,
                        title = "কোনো হিসাব পাওয়া যায়নি",
                        subtitle = "সার, সেচ, স্প্রে, শ্রমিক বা ফসল বিক্রির তথ্য যোগ করলে এখানে সব একসাথে দেখাবে।"
                    )
                }
            } else {
                items(filteredEntries, key = { "${it.category.name}_${it.id}" }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                        border = BorderStroke(1.dp, BorderSoft)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (entry.isIncome) EmeraldSoftBg else StrawberrySoftBg,
                                        border = BorderStroke(1.dp, if (entry.isIncome) EmeraldBorder else StrawberryBorder)
                                    ) {
                                        Text(
                                            text = entry.category.bengaliLabel,
                                            color = TextDarkBlack,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (entry.isDue) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StrawberryRed
                                        ) {
                                            Text(
                                                text = "বাকি",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleSmall.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${entry.dateLabel}${if (entry.detail.isNotBlank()) " • ${entry.detail}" else ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                                )
                            }
                            Text(
                                text = (if (entry.isIncome) "+" else "-") + BengaliFormatters.formatTaka(entry.amount),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = if (entry.isIncome) EmeraldPrimary else StrawberryRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
