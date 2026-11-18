package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HarvestSale
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.StrawberryBorder
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun HarvestProfitScreen(
    state: FarmDashboardState,
    initialTab: Int = 0,
    copiedCalculatorAmount: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onSaveHarvest: (HarvestSale?, HarvestSale) -> Unit,
    onTrashHarvest: (HarvestSale) -> Unit,
    onClearCopiedAmount: () -> Unit
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<HarvestSale?>(null) }

    val summary = state.summary

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "ফসল বিক্রি ও লাভ-ক্ষতি",
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
                containerColor = StrawberryRed,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_harvest_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "বিক্রি যোগ")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("বিক্রি যোগ করুন", fontWeight = FontWeight.Bold)
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
                        color = StrawberryRed,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.heightIn(min = 52.dp).testTag("tab_sales_log"),
                    text = { Text("বিক্রির হিসাব", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.heightIn(min = 52.dp).testTag("tab_profit_summary"),
                    text = { Text("লাভ-ক্ষতি দেখুন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTab == 1) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                            border = BorderStroke(1.5.dp, if (summary.netProfit >= 0) EmeraldBorder else StrawberryBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "মোট লাভ-ক্ষতির হিসাব",
                                    style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = EmeraldSoftBg,
                                        border = BorderStroke(1.dp, EmeraldBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("মোট বিক্রি (আয়)", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = BengaliFormatters.formatTaka(summary.totalSales),
                                                color = EmeraldPrimary,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "মোট ফলন: ${BengaliFormatters.formatKg(summary.totalHarvestKg)}",
                                                color = TextDarkBlack,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = StrawberrySoftBg,
                                        border = BorderStroke(1.dp, StrawberryBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("মোট খরচ", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = BengaliFormatters.formatTaka(summary.totalExpense),
                                                color = StrawberryRed,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "সার+সেচ+স্প্রে+শ্রমিক",
                                                color = TextDarkBlack,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = BorderSoft)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (summary.netProfit >= 0) "নিট লাভ:" else "নিট ঘাটতি:",
                                        style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = BengaliFormatters.formatTaka(kotlin.math.abs(summary.netProfit)),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            color = if (summary.netProfit >= 0) EmeraldPrimary else StrawberryRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "খরচের খাতসমূহ:",
                                    style = MaterialTheme.typography.titleSmall.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val maxExp = maxOf(summary.totalExpense, 1.0)
                                ExpenseBarRow("সার কেনা", summary.fertilizerTotal, maxExp, EmeraldPrimary)
                                ExpenseBarRow("সেচ খরচ", summary.irrigationTotal, maxExp, SkyBlueAccent)
                                ExpenseBarRow("ওষুধ স্প্রে", summary.sprayTotal, maxExp, TealAccent)
                                ExpenseBarRow("শ্রমিক মজুরি", summary.laborTotal, maxExp, PurpleAccent)
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                            border = BorderStroke(1.5.dp, EmeraldBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("মোট বিক্রি (${BengaliFormatters.formatKg(summary.totalHarvestKg)})", color = TextSecondaryDark, fontSize = 13.sp)
                                    Text(
                                        text = BengaliFormatters.formatTaka(summary.totalSales),
                                        color = EmeraldPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (summary.netProfit >= 0) "নিট লাভ" else "ঘাটতি", color = TextSecondaryDark, fontSize = 13.sp)
                                    Text(
                                        text = BengaliFormatters.formatTaka(kotlin.math.abs(summary.netProfit)),
                                        color = if (summary.netProfit >= 0) EmeraldPrimary else StrawberryRed,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "দৈনিক স্ট্রবেরি বিক্রির খাতা",
                        style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                    )
                }

                if (state.harvests.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.TrendingUp,
                            title = "এখনো কোনো ফসল বিক্রির হিসাব নেই",
                            subtitle = "আজকে কত কেজি স্ট্রবেরি কত টাকায় বিক্রি করলেন তা এখানে লিখে রাখুন।",
                            actionLabel = "বিক্রি যোগ করুন",
                            onAction = { editingItem = null; showDialog = true }
                        )
                    }
                } else {
                    items(state.harvests, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                            border = BorderStroke(1.5.dp, StrawberryBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${BengaliFormatters.formatKg(item.weightKg)} স্ট্রবেরি বিক্রি",
                                            style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${item.dateLabel}${if (item.buyerName.isNotBlank()) " • ${item.buyerName}" else ""}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                                        )
                                        if (item.pricePerKg > 0) {
                                            Text(
                                                text = "দর: ${BengaliFormatters.formatTaka(item.pricePerKg)} / কেজি",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                                            )
                                        }
                                    }
                                    Text(
                                        text = BengaliFormatters.formatTaka(item.totalAmount),
                                        style = MaterialTheme.typography.titleLarge.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold)
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
                                        onDelete = { onTrashHarvest(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        HarvestFormDialog(
            initial = editingItem,
            copiedCalculatorAmount = copiedCalculatorAmount,
            onClearCopiedAmount = onClearCopiedAmount,
            onDismiss = { showDialog = false },
            onSave = { newItem ->
                onSaveHarvest(editingItem, newItem)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ExpenseBarRow(label: String, value: Double, maxTotal: Double, barColor: Color) {
    val fraction = (value / maxTotal).toFloat().coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextDarkBlack, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(BengaliFormatters.formatTaka(value), color = TextDarkBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun HarvestFormDialog(
    initial: HarvestSale?,
    copiedCalculatorAmount: String?,
    onClearCopiedAmount: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (HarvestSale) -> Unit
) {
    var dateMillis by remember { mutableLongStateOf(initial?.dateMillis ?: BengaliFormatters.startOfTodayMillis()) }
    var weightText by remember { mutableStateOf(initial?.weightKg?.let { BengaliFormatters.formatNumber(it, false) }.orEmpty()) }
    var rateText by remember { mutableStateOf(initial?.pricePerKg?.takeIf { it > 0 }?.let { BengaliFormatters.formatNumber(it, false) }.orEmpty()) }
    var totalText by remember { mutableStateOf(initial?.totalAmount?.let { BengaliFormatters.formatNumber(it, false) }.orEmpty()) }
    var buyerName by remember { mutableStateOf(initial?.buyerName.orEmpty()) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun autoComputeTotal(wStr: String, rStr: String) {
        val w = BengaliFormatters.parseFlexibleDouble(wStr)
        val r = BengaliFormatters.parseFlexibleDouble(rStr)
        if (w != null && r != null && w > 0 && r > 0) {
            totalText = BengaliFormatters.formatNumber(w * r, false)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "ফসল বিক্রির হিসাব" else "বিক্রির হিসাব সম্পাদনা",
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
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        errorText = null
                        autoComputeTotal(it, rateText)
                    },
                    label = "ওজন (কেজি) *",
                    placeholder = "যেমন: ১২.৫",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "harvest_weight_input"
                )
                HighContrastTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it
                        autoComputeTotal(weightText, it)
                    },
                    label = "প্রতি কেজির দর (ঐচ্ছিক)",
                    placeholder = "যেমন: ২৫০",
                    keyboardType = KeyboardType.Decimal
                )
                PasteFromCalculatorChip(
                    copiedAmount = copiedCalculatorAmount,
                    onPaste = {
                        totalText = it
                        onClearCopiedAmount()
                    }
                )
                HighContrastTextField(
                    value = totalText,
                    onValueChange = { totalText = it; errorText = null },
                    label = "মোট বিক্রির টাকা *",
                    placeholder = "যেমন: ৩১২৫",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "harvest_total_input"
                )
                HighContrastTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = "ক্রেতা / বাজারের নাম (ঐচ্ছিক)",
                    placeholder = "যেমন: রাজশাহী আড়ত"
                )
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
                    val weight = BengaliFormatters.parseFlexibleDouble(weightText)
                    val total = BengaliFormatters.parseFlexibleDouble(totalText)
                    val rate = BengaliFormatters.parseFlexibleDouble(rateText) ?: 0.0
                    if (weight == null || weight <= 0) {
                        errorText = "সঠিক ওজন (কেজি) লিখুন।"
                        return@Button
                    }
                    if (total == null || total < 0) {
                        errorText = "মোট বিক্রির টাকা লিখুন।"
                        return@Button
                    }
                    onSave(
                        HarvestSale(
                            dateMillis = dateMillis,
                            dateLabel = BengaliFormatters.formatDateLabel(dateMillis),
                            weightKg = weight,
                            pricePerKg = rate.coerceAtLeast(0.0),
                            totalAmount = total,
                            buyerName = buyerName.trim(),
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
