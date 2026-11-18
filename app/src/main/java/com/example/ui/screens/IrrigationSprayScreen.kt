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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.IrrigationLog
import com.example.model.SprayLog
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSoftBg
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SkyBorder
import com.example.ui.theme.SkySoftBg
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealSoftBg
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun IrrigationSprayScreen(
    state: FarmDashboardState,
    copiedCalculatorAmount: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onSaveIrrigation: (IrrigationLog?, IrrigationLog) -> Unit,
    onTrashIrrigation: (IrrigationLog) -> Unit,
    onSaveSpray: (SprayLog?, SprayLog) -> Unit,
    onTrashSpray: (SprayLog) -> Unit,
    onClearCopiedAmount: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showIrrigationDialog by remember { mutableStateOf(false) }
    var editingIrrigation by remember { mutableStateOf<IrrigationLog?>(null) }
    var showSprayDialog by remember { mutableStateOf(false) }
    var editingSpray by remember { mutableStateOf<SprayLog?>(null) }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "সেচ ও স্প্রে খাতা",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        editingIrrigation = null
                        showIrrigationDialog = true
                    } else {
                        editingSpray = null
                        showSprayDialog = true
                    }
                },
                containerColor = if (selectedTab == 0) SkyBlueAccent else TealAccent,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_irrigation_spray_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "যোগ করুন")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedTab == 0) "সেচ যোগ করুন" else "স্প্রে যোগ করুন",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfacePureWhite,
                contentColor = TextDarkBlack,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 0) SkyBlueAccent else TealAccent,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.heightIn(min = 52.dp).testTag("tab_irrigation"),
                    text = { Text("সেচ দেওয়া", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.heightIn(min = 52.dp).testTag("tab_spray"),
                    text = { Text("ওষুধ স্প্রে", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SkySoftBg),
                            border = BorderStroke(1.5.dp, SkyBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("মোট সেচ খরচ", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = BengaliFormatters.formatTaka(state.summary.irrigationTotal),
                                        color = TextDarkBlack,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (state.summary.nextIrrigationLabel.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AmberSoftBg,
                                        border = BorderStroke(1.dp, AmberBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "রিমাইন্ডার", tint = TextDarkBlack, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text("পরবর্তী সম্ভাব্য সেচ", fontSize = 11.sp, color = TextSecondaryDark)
                                                Text(state.summary.nextIrrigationLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.irrigations.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.WaterDrop,
                                title = "কোনো সেচের তথ্য নেই",
                                subtitle = "জমিতে পানি দেওয়ার তারিখ ও খরচ লিখলে পরবর্তী সেচের সম্ভাব্য তারিখ স্বয়ংক্রিয়ভাবে দেখাবে।",
                                actionLabel = "সেচ যোগ করুন",
                                onAction = {
                                    editingIrrigation = null
                                    showIrrigationDialog = true
                                }
                            )
                        }
                    } else {
                        items(state.irrigations, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                                border = BorderStroke(1.5.dp, SkyBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.plotName,
                                                style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "সেচের তারিখ: ${item.dateLabel}",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                                            )
                                        }
                                        Text(
                                            text = BengaliFormatters.formatTaka(item.cost),
                                            style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AmberSoftBg,
                                        border = BorderStroke(1.dp, AmberBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "পরবর্তী সেচ", tint = TextDarkBlack, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "পরবর্তী সেচের সম্ভাব্য তারিখ: ${item.nextIrrigationLabel}",
                                                style = MaterialTheme.typography.labelMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                    if (item.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("নোট: ${item.notes}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        InlineEditDeleteRow(
                                            onEdit = { editingIrrigation = item; showIrrigationDialog = true },
                                            onDelete = { onTrashIrrigation(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = TealSoftBg),
                            border = BorderStroke(1.5.dp, TealBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("মোট স্প্রে ও ওষুধ খরচ", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = BengaliFormatters.formatTaka(state.summary.sprayTotal),
                                    color = TextDarkBlack,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (state.sprays.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Science,
                                title = "কোনো স্প্রে রেকর্ড নেই",
                                subtitle = "গাছে কীটনাশক, ছত্রাকনাশক বা হরলিক্স স্প্রে করলে এখানে লিখে রাখুন।",
                                actionLabel = "স্প্রে যোগ করুন",
                                onAction = {
                                    editingSpray = null
                                    showSprayDialog = true
                                }
                            )
                        }
                    } else {
                        items(state.sprays, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                                border = BorderStroke(1.5.dp, TealBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.medicineName,
                                                style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${item.dateLabel}${if (item.dose.isNotBlank()) " • মাত্রা: ${item.dose}" else ""}",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                                            )
                                        }
                                        Text(
                                            text = BengaliFormatters.formatTaka(item.cost),
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
                                            onEdit = { editingSpray = item; showSprayDialog = true },
                                            onDelete = { onTrashSpray(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showIrrigationDialog) {
        IrrigationFormDialog(
            initial = editingIrrigation,
            defaultIntervalDays = state.appConfig.irrigationIntervalDays.toInt().coerceIn(1, 30),
            copiedCalculatorAmount = copiedCalculatorAmount,
            onClearCopiedAmount = onClearCopiedAmount,
            onDismiss = { showIrrigationDialog = false },
            onSave = { newItem ->
                onSaveIrrigation(editingIrrigation, newItem)
                showIrrigationDialog = false
            }
        )
    }

    if (showSprayDialog) {
        SprayFormDialog(
            initial = editingSpray,
            presets = state.appConfig.sprayPresets,
            copiedCalculatorAmount = copiedCalculatorAmount,
            onClearCopiedAmount = onClearCopiedAmount,
            onDismiss = { showSprayDialog = false },
            onSave = { newItem ->
                onSaveSpray(editingSpray, newItem)
                showSprayDialog = false
            }
        )
    }
}

@Composable
private fun IrrigationFormDialog(
    initial: IrrigationLog?,
    defaultIntervalDays: Int,
    copiedCalculatorAmount: String?,
    onClearCopiedAmount: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (IrrigationLog) -> Unit
) {
    var dateMillis by remember { mutableLongStateOf(initial?.dateMillis ?: BengaliFormatters.startOfTodayMillis()) }
    var plotName by remember { mutableStateOf(initial?.plotName ?: "১ নং স্ট্রবেরি জমি") }
    var costText by remember { mutableStateOf(initial?.cost?.let { BengaliFormatters.formatNumber(it, false) } ?: "0") }
    var intervalDaysText by remember { mutableStateOf(defaultIntervalDays.toString()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val interval = (BengaliFormatters.parseFlexibleLong(intervalDaysText)?.toInt() ?: defaultIntervalDays).coerceIn(1, 60)
    val nextMillis = BengaliFormatters.addDays(dateMillis, interval)
    val nextLabel = BengaliFormatters.formatDateLabel(nextMillis)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "সেচ দেওয়ার হিসাব" else "সেচের তথ্য সম্পাদনা",
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
                HighContrastTextField(
                    value = plotName,
                    onValueChange = { plotName = it; errorText = null },
                    label = "জমি / প্লটের নাম *",
                    placeholder = "যেমন: পূর্বের জমি / ১ নং প্লট",
                    testTag = "irr_plot_input"
                )
                PasteFromCalculatorChip(
                    copiedAmount = copiedCalculatorAmount,
                    onPaste = {
                        costText = it
                        onClearCopiedAmount()
                    }
                )
                HighContrastTextField(
                    value = costText,
                    onValueChange = { costText = it; errorText = null },
                    label = "সেচের খরচ (টাকা)",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "irr_cost_input"
                )
                HighContrastTextField(
                    value = intervalDaysText,
                    onValueChange = { intervalDaysText = it },
                    label = "কত দিন পর আবার সেচ দেবেন?",
                    keyboardType = KeyboardType.Number
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberSoftBg,
                    border = BorderStroke(1.dp, AmberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "পরবর্তী সেচের সম্ভাব্য তারিখ: $nextLabel",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(10.dp)
                    )
                }
                HighContrastTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "নোট (ঐচ্ছিক)",
                    singleLine = false,
                    maxLines = 3
                )
                if (!errorText.isNullOrBlank()) {
                    Text(errorText!!, color = StrawberryRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (plotName.isBlank()) {
                        errorText = "জমি বা প্লটের নাম লিখুন।"
                        return@Button
                    }
                    val cost = BengaliFormatters.parseFlexibleDouble(costText) ?: 0.0
                    onSave(
                        IrrigationLog(
                            dateMillis = dateMillis,
                            dateLabel = BengaliFormatters.formatDateLabel(dateMillis),
                            plotName = plotName.trim(),
                            cost = cost.coerceAtLeast(0.0),
                            nextIrrigationMillis = nextMillis,
                            nextIrrigationLabel = nextLabel,
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

@Composable
private fun SprayFormDialog(
    initial: SprayLog?,
    presets: List<String>,
    copiedCalculatorAmount: String?,
    onClearCopiedAmount: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (SprayLog) -> Unit
) {
    var dateMillis by remember { mutableLongStateOf(initial?.dateMillis ?: BengaliFormatters.startOfTodayMillis()) }
    var medicine by remember { mutableStateOf(initial?.medicineName.orEmpty()) }
    var dose by remember { mutableStateOf(initial?.dose.orEmpty()) }
    var costText by remember { mutableStateOf(initial?.cost?.let { BengaliFormatters.formatNumber(it, false) }.orEmpty()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "ওষুধ স্প্রে খাতা" else "স্প্রে তথ্য সম্পাদনা",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = medicine == preset,
                            onClick = { medicine = preset; errorText = null },
                            label = { Text(preset, color = TextDarkBlack) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealSoftBg,
                                containerColor = BgOffWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = medicine == preset,
                                borderColor = BorderSlate,
                                selectedBorderColor = TealAccent
                            )
                        )
                    }
                }
                HighContrastTextField(
                    value = medicine,
                    onValueChange = { medicine = it; errorText = null },
                    label = "ওষুধ / ছত্রাকনাশকের নাম *",
                    placeholder = "যেমন: ম্যানকোজেব / হরলিক্স স্প্রে"
                )
                HighContrastTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = "মাত্রা / পরিমাণ",
                    placeholder = "যেমন: ২ গ্রাম / লিটার"
                )
                PasteFromCalculatorChip(
                    copiedAmount = copiedCalculatorAmount,
                    onPaste = {
                        costText = it
                        onClearCopiedAmount()
                    }
                )
                HighContrastTextField(
                    value = costText,
                    onValueChange = { costText = it; errorText = null },
                    label = "খরচ (টাকা) *",
                    keyboardType = KeyboardType.Decimal
                )
                HighContrastTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "নোট (ঐচ্ছিক)",
                    singleLine = false,
                    maxLines = 3
                )
                if (!errorText.isNullOrBlank()) {
                    Text(errorText!!, color = StrawberryRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = BengaliFormatters.parseFlexibleDouble(costText) ?: 0.0
                    if (medicine.isBlank()) {
                        errorText = "ওষুধের নাম লিখুন।"
                        return@Button
                    }
                    onSave(
                        SprayLog(
                            dateMillis = dateMillis,
                            dateLabel = BengaliFormatters.formatDateLabel(dateMillis),
                            medicineName = medicine.trim(),
                            dose = dose.trim(),
                            cost = cost.coerceAtLeast(0.0),
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
