package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSoftBg
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeBorder
import com.example.ui.theme.OrangeSoftBg
import com.example.ui.theme.StrawberryBorder
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.ui.viewmodel.SessionState
import com.example.util.BengaliFormatters
import com.example.util.ExportHelper

@Composable
fun SettingsScreen(
    session: SessionState.Active,
    state: FarmDashboardState,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenArticles: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAdmin: () -> Unit,
    onNotify: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "সেটিংস ও অ্যাকাউন্ট",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Farmer Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.5.dp, EmeraldBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.strawberry_farm_icon),
                            contentDescription = "প্রোফাইল",
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = TextDarkBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = session.email,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    session.isSuperAdmin -> StrawberrySoftBg
                                    session.isGuest -> AmberSoftBg
                                    else -> EmeraldSoftBg
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        session.isSuperAdmin -> StrawberryBorder
                                        session.isGuest -> AmberBorder
                                        else -> EmeraldBorder
                                    }
                                )
                            ) {
                                Text(
                                    text = when {
                                        session.isSuperAdmin -> "সুপার অ্যাডমিন অ্যাকাউন্ট"
                                        session.isGuest -> "অফলাইন / লোকাল মোড"
                                        else -> "ক্লাউড সিঙ্ক চালু আছে"
                                    },
                                    color = TextDarkBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Super Admin Access Card (Strictly visible ONLY for Super Admin UID)
            if (session.isSuperAdmin) {
                item {
                    SettingsMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "সুপার অ্যাডমিন প্যানেল",
                        subtitle = "অ্যাপ নাম, নোটিশ, সার রেসিপি, কৃষি আর্টিকেল ও চাষি তালিকা",
                        accentColor = StrawberryRed,
                        softBg = StrawberrySoftBg,
                        borderColor = StrawberryBorder,
                        onClick = onOpenAdmin,
                        testTag = "settings_admin_button"
                    )
                }
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.AutoStories,
                    title = "কৃষি পরামর্শ ও আর্টিকেল",
                    subtitle = "${BengaliFormatters.formatNumber(state.articles.size.toDouble())} টি পরামর্শ ও গাইড পড়ুন",
                    accentColor = EmeraldPrimary,
                    softBg = EmeraldSoftBg,
                    borderColor = EmeraldBorder,
                    onClick = onOpenArticles,
                    testTag = "settings_articles_button"
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "রিসাইকেল বিন",
                    subtitle = "${BengaliFormatters.formatNumber(state.summary.trashedCount.toDouble())} টি মুছে ফেলা তথ্য ফিরিয়ে আনুন",
                    accentColor = OrangeAccent,
                    softBg = OrangeSoftBg,
                    borderColor = OrangeBorder,
                    onClick = onOpenTrash,
                    testTag = "settings_trash_button"
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.Download,
                    title = "এক্সেল ব্যাকআপ ডাউনলোড (.csv)",
                    subtitle = "সব হিসাব একসাথে ফোনে সেভ বা শেয়ার করুন",
                    accentColor = EmeraldPrimary,
                    softBg = EmeraldSoftBg,
                    borderColor = BorderSoft,
                    onClick = {
                        ExportHelper.exportCsvAndShare(
                            context = context,
                            entries = state.ledgerEntries,
                            summary = state.summary,
                            onSavedMessage = onNotify
                        )
                    },
                    testTag = "settings_export_button"
                )
            }

            item {
                SettingsMenuItem(
                    icon = Icons.Default.Person,
                    title = "হিসাবের খাতা ও ইতিহাস দেখুন",
                    subtitle = "মোট ${BengaliFormatters.formatNumber(state.ledgerEntries.size.toDouble())} টি হিসাবের তালিকা",
                    accentColor = TextDarkBlack,
                    softBg = BgOffWhite,
                    borderColor = BorderSoft,
                    onClick = onOpenHistory,
                    testTag = "settings_history_button"
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StrawberryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .testTag("settings_sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "সাইন আউট"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (session.isGuest) "লগইন / অ্যাকাউন্ট স্ক্রিনে যান" else "লগআউট করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    softBg: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(softBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextDarkBlack,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "যান",
                tint = TextDarkBlack
            )
        }
    }
}
