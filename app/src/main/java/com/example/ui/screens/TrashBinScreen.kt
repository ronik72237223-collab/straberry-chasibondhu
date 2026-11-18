package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrashItem
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeBorder
import com.example.ui.theme.OrangeSoftBg
import com.example.ui.theme.StrawberryBorder
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun TrashBinScreen(
    state: FarmDashboardState,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onRestore: (TrashItem) -> Unit,
    onDeletePermanently: (TrashItem) -> Unit,
    onEmptyTrash: () -> Unit
) {
    var showEmptyConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "রিসাইকেল বিন",
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
            if (state.trashItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangeSoftBg),
                        border = BorderStroke(1.5.dp, OrangeBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "মোট ${BengaliFormatters.formatNumber(state.trashItems.size.toDouble())} টি মুছে ফেলা তথ্য",
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Button(
                                onClick = { showEmptyConfirm = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StrawberryRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("empty_trash_button")
                            ) {
                                Text("সব খালি করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (state.trashItems.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.DeleteSweep,
                        title = "রিসাইকেল বিন খালি আছে",
                        subtitle = "ভুল করে কোনো হিসাব মুছে ফেললে সেটি এখানে জমা থাকবে, যেকোনো সময় ফিরিয়ে আনতে পারবেন।"
                    )
                }
            } else {
                items(state.trashItems, key = { "${it.category.name}_${it.id}" }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                        border = BorderStroke(1.5.dp, OrangeBorder)
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
                                        color = OrangeSoftBg
                                    ) {
                                        Text(
                                            text = item.category.bengaliLabel,
                                            color = TextDarkBlack,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${item.dateLabel}${if (item.subtitle.isNotBlank()) " • ${item.subtitle}" else ""}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                                    )
                                }
                                Text(
                                    text = BengaliFormatters.formatTaka(item.amount),
                                    style = MaterialTheme.typography.titleLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onRestore(item) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("restore_trash_${item.id}")
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "ফিরিয়ে আনুন", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ফিরিয়ে আনুন", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onDeletePermanently(item) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = StrawberrySoftBg,
                                        contentColor = StrawberryRed
                                    ),
                                    border = BorderStroke(1.dp, StrawberryBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("perm_delete_trash_${item.id}")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "স্থায়ীভাবে মুছুন", tint = StrawberryRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("স্থায়ীভাবে মুছুন", color = StrawberryRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            containerColor = SurfacePureWhite,
            title = { Text("রিসাইকেল বিন খালি করবেন?", color = TextDarkBlack, fontWeight = FontWeight.Bold) },
            text = { Text("এখানে থাকা সব তথ্য স্থায়ীভাবে মুছে যাবে এবং আর ফিরিয়ে আনা যাবে না।", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        onEmptyTrash()
                        showEmptyConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StrawberryRed, contentColor = Color.White)
                ) {
                    Text("হ্যাঁ, মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text("বাতিল", color = TextDarkBlack)
                }
            }
        )
    }
}
