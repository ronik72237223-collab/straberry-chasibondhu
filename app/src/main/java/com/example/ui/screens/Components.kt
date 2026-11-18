package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSoftBg
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.BorderSoft
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrawberryTopBar(
    title: String,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: (() -> Unit)? = null,
    extraActions: @Composable () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfacePureWhite,
            titleContentColor = TextDarkBlack,
            navigationIconContentColor = TextDarkBlack,
            actionIconContentColor = TextDarkBlack
        ),
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = TextDarkBlack
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.strawberry_farm_icon),
                    contentDescription = "স্ট্রবেরি চাষি বন্ধু",
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextDarkBlack,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            UndoRedoActionPill(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = "Undo",
                enabled = canUndo,
                onClick = onUndo,
                testTag = "undo_button"
            )
            Spacer(modifier = Modifier.width(4.dp))
            UndoRedoActionPill(
                icon = Icons.AutoMirrored.Filled.Redo,
                label = "Redo",
                enabled = canRedo,
                onClick = onRedo,
                testTag = "redo_button"
            )
            extraActions()
            Spacer(modifier = Modifier.width(6.dp))
        }
    )
}

@Composable
private fun UndoRedoActionPill(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) EmeraldSoftBg else SurfaceSubtle,
        border = BorderStroke(1.dp, if (enabled) EmeraldBorder else BorderSoft),
        modifier = Modifier
            .heightIn(min = 42.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) TextDarkBlack else Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HighContrastTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: ImageVector? = null,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        placeholder = if (placeholder.isNotBlank()) {
            {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                )
            }
        } else null,
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = TextDarkBlack
                )
            }
        } else null,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextDarkBlack,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfacePureWhite,
            unfocusedContainerColor = SurfacePureWhite,
            focusedTextColor = TextDarkBlack,
            unfocusedTextColor = TextDarkBlack,
            cursorColor = EmeraldPrimary,
            focusedBorderColor = EmeraldPrimary,
            unfocusedBorderColor = BorderSlate,
            focusedLabelColor = EmeraldPrimary,
            unfocusedLabelColor = TextSecondaryDark
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    )
}

@Composable
fun PasteFromCalculatorChip(
    copiedAmount: String?,
    onPaste: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (copiedAmount.isNullOrBlank()) return
    Surface(
        onClick = { onPaste(copiedAmount) },
        shape = RoundedCornerShape(12.dp),
        color = AmberSoftBg,
        border = BorderStroke(1.5.dp, AmberBorder),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag("paste_calculator_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = "পেস্ট করুন",
                tint = TextDarkBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ক্যালকুলেটর থেকে বসান: ৳$copiedAmount",
                style = MaterialTheme.typography.labelLarge.copy(color = TextDarkBlack)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectorRow(
    selectedMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val today = remember { BengaliFormatters.startOfTodayMillis() }
    val yesterday = remember { BengaliFormatters.addDays(today, -1) }
    val selectedLabel = BengaliFormatters.formatDateLabel(selectedMillis)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "তারিখ: $selectedLabel",
            style = MaterialTheme.typography.titleSmall.copy(
                color = TextDarkBlack,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = BengaliFormatters.formatDateLabel(selectedMillis) == BengaliFormatters.formatDateLabel(today),
                onClick = { onDateSelected(today) },
                label = { Text("আজ", color = TextDarkBlack, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldSoftBg,
                    containerColor = SurfacePureWhite
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = BengaliFormatters.formatDateLabel(selectedMillis) == BengaliFormatters.formatDateLabel(today),
                    borderColor = BorderSlate,
                    selectedBorderColor = EmeraldPrimary
                ),
                modifier = Modifier.heightIn(min = 42.dp)
            )
            FilterChip(
                selected = BengaliFormatters.formatDateLabel(selectedMillis) == BengaliFormatters.formatDateLabel(yesterday),
                onClick = { onDateSelected(yesterday) },
                label = { Text("গতকাল", color = TextDarkBlack, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldSoftBg,
                    containerColor = SurfacePureWhite
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = BengaliFormatters.formatDateLabel(selectedMillis) == BengaliFormatters.formatDateLabel(yesterday),
                    borderColor = BorderSlate,
                    selectedBorderColor = EmeraldPrimary
                ),
                modifier = Modifier.heightIn(min = 42.dp)
            )
            OutlinedButton(
                onClick = { showPicker = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderSlate),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfacePureWhite,
                    contentColor = TextDarkBlack
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.heightIn(min = 42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "তারিখ নির্বাচন",
                    tint = TextDarkBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("অন্য তারিখ", color = TextDarkBlack, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateSelected(it) }
                        showPicker = false
                    }
                ) {
                    Text("ঠিক আছে", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("বাতিল", color = TextDarkBlack)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun InlineEditDeleteRow(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    editTag: String = "inline_edit_button",
    deleteTag: String = "inline_delete_button"
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderSlate),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceSubtle,
                contentColor = TextDarkBlack
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag(editTag)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "সম্পাদনা",
                tint = TextDarkBlack,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("সম্পাদনা", color = TextDarkBlack, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onDelete,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = StrawberrySoftBg,
                contentColor = StrawberryRed
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag(deleteTag)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "মুছুন",
                tint = StrawberryRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("মুছুন", color = StrawberryRed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(EmeraldSoftBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextDarkBlack,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
