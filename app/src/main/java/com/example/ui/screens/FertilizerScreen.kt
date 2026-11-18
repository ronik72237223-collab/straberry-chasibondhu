package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FeedingRecipe
import com.example.model.FertilizerPurchase
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSoftBg
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
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun FertilizerScreen(
    state: FarmDashboardState,
    copiedCalculatorAmount: String?,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onSavePurchase: (FertilizerPurchase?, FertilizerPurchase) -> Unit,
    onTrashPurchase: (FertilizerPurchase) -> Unit,
    onPayDue: (FertilizerPurchase) -> Unit,
    onClearCopiedAmount: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFormDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<FertilizerPurchase?>(null) }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "সার ও বিশেষ খাবার",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        editingItem = null
                        showFormDialog = true
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 72.dp)
                        .testTag("add_fertilizer_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "নতুন সার")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সার কেনা যোগ করুন", fontWeight = FontWeight.Bold)
                    }
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
                        color = EmeraldPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .testTag("tab_feeding_guide"),
                    text = {
                        Text(
                            text = "সার দেওয়ার নিয়ম",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkBlack
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .testTag("tab_purchase_ledger"),
                    text = {
                        Text(
                            text = "সার কেনার খাতা",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkBlack
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recipes, key = { it.id }) { recipe ->
                        FeedingRecipeCard(recipe = recipe)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                                border = BorderStroke(1.5.dp, EmeraldBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("মোট সার খরচ", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = BengaliFormatters.formatTaka(state.summary.fertilizerTotal),
                                        color = TextDarkBlack,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.summary.fertilizerDue > 0) StrawberrySoftBg else EmeraldSoftBg
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (state.summary.fertilizerDue > 0) StrawberryBorder else EmeraldBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("মোট বাকি (Due)", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = BengaliFormatters.formatTaka(state.summary.fertilizerDue),
                                        color = if (state.summary.fertilizerDue > 0) StrawberryRed else EmeraldPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                editingItem = null
                                showFormDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "যোগ করুন")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("নতুন সার কেনার হিসাব লিখুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (state.fertilizers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.ReceiptLong,
                                title = "এখনো কোনো সার কেনার হিসাব নেই",
                                subtitle = "দোকান থেকে সার কিনলে নগদ বা বাকি হিসেবে এখানে লিখে রাখুন।",
                                actionLabel = "প্রথম সার কেনা যোগ করুন",
                                onAction = {
                                    editingItem = null
                                    showFormDialog = true
                                }
                            )
                        }
                    } else {
                        items(state.fertilizers, key = { it.id }) { item ->
                            FertilizerPurchaseCard(
                                item = item,
                                onEdit = {
                                    editingItem = item
                                    showFormDialog = true
                                },
                                onDelete = { onTrashPurchase(item) },
                                onPayDue = { onPayDue(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        FertilizerPurchaseDialog(
            initial = editingItem,
            presets = state.appConfig.fertilizerPresets,
            copiedCalculatorAmount = copiedCalculatorAmount,
            onClearCopiedAmount = onClearCopiedAmount,
            onDismiss = { showFormDialog = false },
            onSave = { newItem ->
                onSavePurchase(editingItem, newItem)
                showFormDialog = false
            }
        )
    }
}

@Composable
private fun FeedingRecipeCard(recipe: FeedingRecipe) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.5.dp, EmeraldBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldSoftBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = recipe.category,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberSoftBg
                    ) {
                        Text(
                            text = recipe.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "বিস্তারিত",
                    tint = TextDarkBlack
                )
            }

            if (recipe.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = recipe.summary,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                )
            }

            if (recipe.dosage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSoftBg,
                    border = BorderStroke(1.dp, EmeraldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "সঠিক মাপ: ${recipe.dosage}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "প্রয়োগের ধাপসমূহ:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = BengaliFormatters.toBengaliDigits((index + 1).toString()),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextDarkBlack),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (recipe.tip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AmberSoftBg,
                            border = BorderStroke(1.dp, AmberBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "টিপস",
                                    tint = TextDarkBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = recipe.tip,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextDarkBlack,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FertilizerPurchaseCard(
    item: FertilizerPurchase,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayDue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.5.dp, if (item.due) StrawberryBorder else BorderSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fertilizerName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.dateLabel}${if (item.quantity.isNotBlank()) " • ${item.quantity}" else ""}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                    )
                    if (item.shopName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "দোকান",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.shopName,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = BengaliFormatters.formatTaka(item.totalPrice),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (item.due) StrawberrySoftBg else EmeraldSoftBg,
                        border = BorderStroke(1.dp, if (item.due) StrawberryBorder else EmeraldBorder)
                    ) {
                        Text(
                            text = if (item.due) "বাকি (Due)" else "নগদ (Cash)",
                            color = if (item.due) StrawberryRed else EmeraldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "নোট: ${item.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.due) {
                    Button(
                        onClick = onPayDue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("pay_due_button_${item.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "পরিশোধ", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("বাকি পরিশোধ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                InlineEditDeleteRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun FertilizerPurchaseDialog(
    initial: FertilizerPurchase?,
    presets: List<String>,
    copiedCalculatorAmount: String?,
    onClearCopiedAmount: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (FertilizerPurchase) -> Unit
) {
    var dateMillis by remember { mutableLongStateOf(initial?.dateMillis ?: BengaliFormatters.startOfTodayMillis()) }
    var name by remember { mutableStateOf(initial?.fertilizerName.orEmpty()) }
    var shopName by remember { mutableStateOf(initial?.shopName.orEmpty()) }
    var quantity by remember { mutableStateOf(initial?.quantity.orEmpty()) }
    var priceText by remember { mutableStateOf(initial?.totalPrice?.let { BengaliFormatters.formatNumber(it, false) }.orEmpty()) }
    var isDue by remember { mutableStateOf(initial?.due ?: false) }
    var notes by remember { mutableStateOf(initial?.notes.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "সার কেনার হিসাব" else "সারের হিসাব সম্পাদনা",
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

                Text(
                    text = "দ্রুত সার বাছাই করুন:",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = name == preset,
                            onClick = { name = preset; errorText = null },
                            label = { Text(preset, color = TextDarkBlack, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSoftBg,
                                containerColor = BgOffWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = name == preset,
                                borderColor = BorderSlate,
                                selectedBorderColor = EmeraldPrimary
                            )
                        )
                    }
                }

                HighContrastTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = "সারের নাম *",
                    placeholder = "যেমন: ইউরিয়া / টিএসপি / সরিষার খৈল",
                    testTag = "fert_name_input"
                )
                HighContrastTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = "দোকানের নাম (কোথা থেকে কিনলেন)",
                    placeholder = "যেমন: করিম ট্রেডার্স",
                    testTag = "fert_shop_input"
                )
                HighContrastTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = "পরিমাণ",
                    placeholder = "যেমন: ৫ কেজি / ১ বস্তা",
                    testTag = "fert_quantity_input"
                )
                PasteFromCalculatorChip(
                    copiedAmount = copiedCalculatorAmount,
                    onPaste = {
                        priceText = it
                        onClearCopiedAmount()
                    }
                )
                HighContrastTextField(
                    value = priceText,
                    onValueChange = { priceText = it; errorText = null },
                    label = "মোট দাম (টাকা) *",
                    placeholder = "যেমন: ৪৫০",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "fert_price_input"
                )

                // Big Cash vs Due Toggle
                Text(
                    text = "পরিশোধের ধরন:",
                    style = MaterialTheme.typography.titleSmall.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { isDue = false },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!isDue) EmeraldSoftBg else SurfacePureWhite,
                            contentColor = TextDarkBlack
                        ),
                        border = BorderStroke(if (!isDue) 2.dp else 1.dp, if (!isDue) EmeraldPrimary else BorderSlate),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("toggle_cash_button")
                    ) {
                        Text("নগদ (Cash)", fontWeight = FontWeight.Bold, color = TextDarkBlack)
                    }
                    OutlinedButton(
                        onClick = { isDue = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDue) StrawberrySoftBg else SurfacePureWhite,
                            contentColor = TextDarkBlack
                        ),
                        border = BorderStroke(if (isDue) 2.dp else 1.dp, if (isDue) StrawberryRed else BorderSlate),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("toggle_due_button")
                    ) {
                        Text("বাকি (Due)", fontWeight = FontWeight.Bold, color = TextDarkBlack)
                    }
                }

                HighContrastTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "অতিরিক্ত নোট (ঐচ্ছিক)",
                    singleLine = false,
                    maxLines = 3
                )

                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText!!,
                        color = StrawberryRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = BengaliFormatters.parseFlexibleDouble(priceText)
                    if (name.isBlank()) {
                        errorText = "দয়া করে সারের নাম লিখুন।"
                        return@Button
                    }
                    if (price == null || price < 0) {
                        errorText = "সঠিক দাম (সংখ্যা) লিখুন।"
                        return@Button
                    }
                    onSave(
                        FertilizerPurchase(
                            dateMillis = dateMillis,
                            dateLabel = BengaliFormatters.formatDateLabel(dateMillis),
                            fertilizerName = name.trim(),
                            shopName = shopName.trim(),
                            quantity = quantity.trim(),
                            totalPrice = price,
                            due = isDue,
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("save_fertilizer_button")
            ) {
                Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("বাতিল", color = TextDarkBlack, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
