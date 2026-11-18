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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
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
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleBorder
import com.example.ui.theme.PurpleSoftBg
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SkyBorder
import com.example.ui.theme.SkySoftBg
import com.example.ui.theme.StrawberryBorder
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealSoftBg
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.ui.viewmodel.SessionState
import com.example.util.BengaliFormatters

@Composable
fun HomeDashboardScreen(
    session: SessionState.Active,
    state: FarmDashboardState,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onNavigateFertilizer: () -> Unit,
    onNavigateIrrigationSpray: () -> Unit,
    onNavigateLabor: () -> Unit,
    onNavigateHarvest: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateTrash: () -> Unit,
    onNavigateAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val summary = state.summary

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = state.appConfig.appTitle,
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                extraActions = {
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = if (session.isGuest) "লগইন পর্দায় যান" else "লগআউট",
                            tint = TextDarkBlack
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Greeting & User Mode Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "স্বাগতম, ${session.displayName}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = state.appConfig.dashboardSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (session.isSuperAdmin) StrawberrySoftBg else if (session.isGuest) AmberSoftBg else EmeraldSoftBg,
                        border = BorderStroke(
                            1.dp,
                            if (session.isSuperAdmin) StrawberryBorder else if (session.isGuest) AmberBorder else EmeraldBorder
                        )
                    ) {
                        Text(
                            text = when {
                                session.isSuperAdmin -> "সুপার অ্যাডমিন"
                                session.isGuest -> "অফলাইন গেস্ট"
                                else -> "ক্লাউড সিঙ্ক চালু"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Dynamic Admin Welcome Notice Banner
            if (state.appConfig.welcomeBanner.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                        border = BorderStroke(1.5.dp, EmeraldBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSoftBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "নোটিশ",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.appConfig.welcomeBanner,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextDarkBlack,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Big Visual Profit / Expense Hero Card
            item {
                Card(
                    onClick = onNavigateHarvest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_profit_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.5.dp, if (summary.netProfit >= 0) EmeraldBorder else StrawberryBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(EmeraldSoftBg.copy(alpha = 0.7f), SurfacePureWhite, StrawberrySoftBg.copy(alpha = 0.6f))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (summary.netProfit >= 0) "নিট লাভ (আয় - খরচ)" else "মোট ঘাটতি (খরচ বেশি)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextDarkBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (summary.netProfit >= 0) EmeraldPrimary else StrawberryRed
                            ) {
                                Text(
                                    text = if (summary.netProfit >= 0) "লাভজনক" else "ঘাটতি",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = BengaliFormatters.formatTaka(kotlin.math.abs(summary.netProfit)),
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = if (summary.netProfit >= 0) EmeraldPrimary else StrawberryRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = SurfacePureWhite,
                                border = BorderStroke(1.dp, BorderSoft)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("মোট বিক্রি (আয়)", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = BengaliFormatters.formatTaka(summary.totalSales),
                                        color = EmeraldPrimary,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "ফলন: ${BengaliFormatters.formatKg(summary.totalHarvestKg)}",
                                        color = TextDarkBlack,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = SurfacePureWhite,
                                border = BorderStroke(1.dp, BorderSoft)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("মোট খরচ", color = TextSecondaryDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = BengaliFormatters.formatTaka(summary.totalExpense),
                                        color = StrawberryRed,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "বাকি: ${BengaliFormatters.formatTaka(summary.fertilizerDue)}",
                                        color = TextDarkBlack,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bento Grid Row 1: Fertilizer + Irrigation/Spray
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoActionCard(
                        title = "সার ও বিশেষ খাবার",
                        subtitle = "নিয়ম ও কেনার খাতা",
                        metric = "বাকি: ${BengaliFormatters.formatTaka(summary.fertilizerDue)}",
                        icon = Icons.Default.Eco,
                        accentColor = EmeraldPrimary,
                        softBg = EmeraldSoftBg,
                        borderColor = EmeraldBorder,
                        onClick = onNavigateFertilizer,
                        testTag = "bento_fertilizer_card",
                        modifier = Modifier.weight(1f)
                    )
                    BentoActionCard(
                        title = "সেচ ও স্প্রে খাতা",
                        subtitle = "পানি ও ওষুধ হিসাব",
                        metric = if (summary.nextIrrigationLabel.isNotBlank()) "পরবর্তী: ${summary.nextIrrigationLabel}" else "নতুন এন্ট্রি দিন",
                        icon = Icons.Default.WaterDrop,
                        accentColor = SkyBlueAccent,
                        softBg = SkySoftBg,
                        borderColor = SkyBorder,
                        onClick = onNavigateIrrigationSpray,
                        testTag = "bento_irrigation_card",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bento Grid Row 2: Labor + Harvest/Profit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoActionCard(
                        title = "শ্রমিক ও নিড়ানি",
                        subtitle = "হাজিরা ও কাজ",
                        metric = "মোট: ${BengaliFormatters.formatTaka(summary.laborTotal)}",
                        icon = Icons.Default.Engineering,
                        accentColor = PurpleAccent,
                        softBg = PurpleSoftBg,
                        borderColor = PurpleBorder,
                        onClick = onNavigateLabor,
                        testTag = "bento_labor_card",
                        modifier = Modifier.weight(1f)
                    )
                    BentoActionCard(
                        title = "ফসল বিক্রি ও লাভ",
                        subtitle = "কেজি ও লাভ-ক্ষতি",
                        metric = BengaliFormatters.formatKg(summary.totalHarvestKg),
                        icon = Icons.Default.TrendingUp,
                        accentColor = StrawberryRed,
                        softBg = StrawberrySoftBg,
                        borderColor = StrawberryBorder,
                        onClick = onNavigateHarvest,
                        testTag = "bento_harvest_card",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bento Grid Row 3: History/Excel + Recycle Bin
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BentoActionCard(
                        title = "খাতা ও এক্সেল",
                        subtitle = "CSV ও হোয়াটসঅ্যাপ",
                        metric = "${BengaliFormatters.formatNumber(state.ledgerEntries.size.toDouble())} টি এন্ট্রি",
                        icon = Icons.Default.Assessment,
                        accentColor = TealAccent,
                        softBg = TealSoftBg,
                        borderColor = TealBorder,
                        onClick = onNavigateHistory,
                        testTag = "bento_history_card",
                        modifier = Modifier.weight(1f)
                    )
                    BentoActionCard(
                        title = "রিসাইকেল বিন",
                        subtitle = "মুছে ফেলা তথ্য",
                        metric = "${BengaliFormatters.formatNumber(summary.trashedCount.toDouble())} টি আইটেম",
                        icon = Icons.Default.DeleteSweep,
                        accentColor = OrangeAccent,
                        softBg = OrangeSoftBg,
                        borderColor = OrangeBorder,
                        onClick = onNavigateTrash,
                        testTag = "bento_trash_card",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Super Admin Panel Card (Strictly visible ONLY for Super Admin UID)
            if (session.isSuperAdmin) {
                item {
                    Card(
                        onClick = onNavigateAdmin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bento_admin_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                        border = BorderStroke(2.dp, StrawberryRed),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StrawberrySoftBg)
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(StrawberryRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "অ্যাডমিন প্যানেল",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "সুপার অ্যাডমিন কন্ট্রোল প্যানেল",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = TextDarkBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "অ্যাপের নাম, নোটিশ, সার রেসিপি ও ব্যবহারকারী নিয়ন্ত্রণ করুন",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Tip Card linking to Feeding Guide
            item {
                Card(
                    onClick = onNavigateFertilizer,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.dp, AmberBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AmberSoftBg.copy(alpha = 0.45f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = "চাষের নিয়ম",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পকেট গঞ্জ, সরিষার খৈল ও হরলিক্স স্প্রে নিয়ম",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextDarkBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "সহজ বাংলায় ধাপে ধাপে স্ট্রবেরি গাছের খাবার তৈরি দেখুন →",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoActionCard(
    title: String,
    subtitle: String,
    metric: String,
    icon: ImageVector,
    accentColor: Color,
    softBg: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 168.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(softBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextDarkBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = softBg
            ) {
                Text(
                    text = metric,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextDarkBlack,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
