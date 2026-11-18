package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaborLog
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.PurpleSoftBg
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun LaborScreen(
    state: FarmDashboardState,
    copiedCalculatorAmount: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onSaveLabor: (LaborLog?, LaborLog) -> Unit,
    onTrashLabor: (LaborLog) -> Unit,
    onClearCopiedAmount: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<LaborLog?>(null) }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "শ্রমিক ও নিড়ানি হিসাব",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingItem = null; showDialog = true },
                containerColor = PurpleAccent,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_labor_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "শ্রমিক যোগ")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("শ্রমিক যোগ করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleSoftBg),
                    border = BorderStroke(1.5.dp, PurpleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("মোট শ্রমিক মজুরি", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = BengaliFormatters.formatTaka(state.summary.laborTotal),
                                color = TextDarkBlack,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfacePureWhite,
                            border = BorderStroke(1.dp, PurpleBorder)
                        ) {
                            val totalWorkers = state.labors.sumOf { it.workerCount }
                            Text(
                                text = "মোট ${BengaliFormatters.formatNumber(totalWorkers.toDouble())} জন-দিন",
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            if (state.labors.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Engineering,
                        title = "এখনো কোনো শ্রমিকের হিসাব নেই",
                        subtitle = "নিড়ানি, সার দেওয়া, মালচিং বা ফল তোলার শ্রমিক সংখ্যা ও হাজিরা এখানে লিখুন।",
                        actionLabel = "শ্রমিক হিসাব যোগ করুন",
                        onAction = { editingItem = null; showDialog = true }
                    )
                }
            } else {
                items(state.labors, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                        border = BorderStroke(1.5.dp, PurpleBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PurpleSoftBg
                                    ) {
                                        Text(
                                            text = item.workType,
                                            color = TextDarkBlack,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${BengaliFormatters.formatNumber(item.workerCount.toDouble())} জন শ্রমিক × ${BengaliFormatters.formatTaka(item.dailyWage)} হাজিরা",
                                        style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "তারিখ: ${item.dateLabel}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                                    )
                                }
                                Text(
                                    text = BengaliFormatters.formatTaka(item.totalCost),
                                    style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                )
                            }
                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("নোট: ${item.notes}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                InlineEditDeleteRow(
                                    onEdit = { editingItem = item; showDialog = true },
                                    onDelete = { onTrashLabor(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        LaborFormDialog(
            initial = editingItem,
            workTypes = state.appConfig.workTypes,
            copiedCalculatorAmount = copiedCalculatorAmount,
            onClearCopiedAmount = onClearCopiedAmount,
            onDismiss = { showDialog = false },
            onSave = { newItem ->
                onSaveLabor(editingItem, newItem)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LaborFormDialog(
    initial: LaborLog?,
    workTypes: List<String>,
    copiedCalculatorAmount: String?,
    onClearCopiedAmount: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (LaborLog) -> Unit
) {
    var dateMillis by remember { mutableLongStateOf(initial?.dateMillis ?: BengaliFormatters.startOfTodayMillis()) }
    var workerCountText by remember { mutableStateOf(initial?.workerCount?.toString() ?: "1") }
    var dailyWageText by remember { mutableStateOf(initial?.dailyWage?.let { BengaliFormatters.formatNumber(it, false) } ?: "500") }
    var selectedWorkType by remember { mutableStateOf(initial?.workType ?: workTypes.firstOrNull().orEmpty().ifBlank { "নিড়ানি দেওয়া" }) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val workers = (BengaliFormatters.parseFlexibleLong(workerCountText) ?: 1L).coerceIn(1L, 1000L)
    val wage = (BengaliFormatters.parseFlexibleDouble(dailyWageText) ?: 0.0).coerceAtLeast(0.0)
    val autoTotal = workers * wage

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "শ্রমিকের হিসাব যোগ" else "শ্রমিকের হিসাব সম্পাদনা",
                style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DateSelectorRow(selectedMillis = dateMillis, onDateSelected = { dateMillis = it })

                Text("কাজের ধরন:", style = MaterialTheme.typography.titleSmall.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    workTypes.forEach { work ->
                        FilterChip(
                            selected = selectedWorkType == work,
                            onClick = { selectedWorkType = work },
                            label = { Text(work, color = TextDarkBlack, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleSoftBg,
                                containerColor = BgOffWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedWorkType == work,
                                borderColor = BorderSlate,
                                selectedBorderColor = PurpleAccent
                            )
                        )
                    }
                }

                // Worker count with big - / + buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val next = (workers - 1).coerceAtLeast(1L)
                            workerCountText = next.toString()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSlate),
                        modifier = Modifier.heightIn(min = 52.dp)
                    ) {
                        Text("-", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack)
                    }
                    HighContrastTextField(
                        value = workerCountText,
                        onValueChange = { workerCountText = it; errorText = null },
                        label = "কতজন শ্রমিক",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "labor_workers_input"
                    )
                    OutlinedButton(
                        onClick = {
                            val next = (workers + 1).coerceAtMost(1000L)
                            workerCountText = next.toString()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSlate),
                        modifier = Modifier.heightIn(min = 52.dp)
                    ) {
                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack)
                    }
                }

                PasteFromCalculatorChip(
                    copiedAmount = copiedCalculatorAmount,
                    onPaste = {
                        dailyWageText = it
                        onClearCopiedAmount()
                    }
                )
                HighContrastTextField(
                    value = dailyWageText,
                    onValueChange = { dailyWageText = it; errorText = null },
                    label = "জনপ্রতি হাজিরা (টাকা)",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "labor_wage_input"
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurpleSoftBg,
                    border = BorderStroke(1.5.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("মোট মজুরি:", color = TextDarkBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = BengaliFormatters.formatTaka(autoTotal),
                            color = PurpleAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }

                HighContrastTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "নোট (ঐচ্ছিক)",
                    singleLine = false,
                    maxLines = 2
                )

                if (!errorText.isNullOrBlank()) {
                    Text(errorText!!, color = StrawberryRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = BengaliFormatters.parseFlexibleLong(workerCountText)
                    val daily = BengaliFormatters.parseFlexibleDouble(dailyWageText)
                    if (count == null || count < 1L) {
                        errorText = "কমপক্ষে ১ জন শ্রমিক দিন।"
                        return@Button
                    }
                    if (daily == null || daily < 0.0) {
                        errorText = "সঠিক হাজিরা (টাকা) লিখুন।"
                        return@Button
                    }
                    onSave(
                        LaborLog(
                            dateMillis = dateMillis,
                            dateLabel = BengaliFormatters.formatDateLabel(dateMillis),
                            workerCount = count.coerceIn(1L, 1000L),
                            dailyWage = daily,
                            totalCost = count * daily,
                            workType = selectedWorkType.ifBlank { "অন্যান্য" },
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("বাতিল", color = TextDarkBlack)
            }
        }
    )
}
