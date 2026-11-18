package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.CalculatorBlue
import com.example.ui.theme.CalculatorPillBg
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldHeaderGreen
import com.example.ui.theme.EmeraldPillBg
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.HealthyCardGreenBg
import com.example.ui.theme.HistoryPillBg
import com.example.ui.theme.HistoryPurple
import com.example.ui.theme.NoticeBannerBg
import com.example.ui.theme.NoticeBannerBorder
import com.example.ui.theme.StrawberryBorder
import com.example.ui.theme.StrawberryHeaderRed
import com.example.ui.theme.StrawberryPillBg
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
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
    onNavigateFertilizer: (initialTab: Int) -> Unit,
    onNavigateIrrigationSpray: (initialTab: Int) -> Unit,
    onNavigateLabor: (workTypeFilter: String?) -> Unit,
    onNavigateHarvest: (initialTab: Int) -> Unit,
    onOpenCalculator: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateArticles: () -> Unit,
    onNavigateAdmin: () -> Unit
) {
    val summary = state.summary
    val titleWords = state.appConfig.appTitle.trim().split(" ", limit = 2)
    val firstTitleWord = titleWords.getOrNull(0)?.ifBlank { "স্ট্রবেরি" } ?: "স্ট্রবেরি"
    val secondTitleWords = titleWords.getOrNull(1)?.ifBlank { "চাষি বন্ধু" } ?: "চাষি বন্ধু"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgOffWhite)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Header Banner (Logo + Red & Green Brand Title + Farmer Illustration)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_header_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = BorderStroke(1.dp, BorderSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFFBF2), SurfacePureWhite, Color(0xFFF3FDE8))
                            )
                        )
                        .padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfacePureWhite,
                        border = BorderStroke(1.5.dp, EmeraldBorder),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.strawberry_farm_icon),
                            contentDescription = state.appConfig.appTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = firstTitleWord,
                            color = StrawberryHeaderRed,
                            fontSize = 24.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = secondTitleWords,
                            color = EmeraldHeaderGreen,
                            fontSize = 20.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Curved green swoosh underline matching reference
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(0f, size.height * 0.7f)
                                quadraticTo(size.width * 0.45f, 0f, size.width, size.height * 0.6f)
                            }
                            drawPath(
                                path = path,
                                color = EmeraldHeaderGreen,
                                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Image(
                        painter = painterResource(id = R.drawable.illus_farmer_banner),
                        contentDescription = "বাংলার কৃষক বন্ধু",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(134.dp)
                            .height(82.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
            }
        }

        // Optional Super Admin Shortcut & Quick Undo/Redo Strip (only when applicable)
        if (session.isSuperAdmin || canUndo || canRedo) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (session.isSuperAdmin) {
                        Surface(
                            onClick = onNavigateAdmin,
                            shape = RoundedCornerShape(12.dp),
                            color = StrawberrySoftBg,
                            border = BorderStroke(1.5.dp, StrawberryRed),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bento_admin_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "সুপার অ্যাডমিন প্যানেল",
                                    tint = StrawberryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "অ্যাডমিন প্যানেল ও আর্টিকেল প্রকাশ",
                                    color = TextDarkBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (canUndo || canRedo) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = onUndo,
                                enabled = canUndo,
                                shape = RoundedCornerShape(10.dp),
                                color = if (canUndo) EmeraldSoftBg else SurfacePureWhite,
                                border = BorderStroke(1.dp, if (canUndo) EmeraldBorder else BorderSoft)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (canUndo) TextDarkBlack else Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Undo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (canUndo) TextDarkBlack else Color(0xFF9CA3AF))
                                }
                            }
                            Surface(
                                onClick = onRedo,
                                enabled = canRedo,
                                shape = RoundedCornerShape(10.dp),
                                color = if (canRedo) EmeraldSoftBg else SurfacePureWhite,
                                border = BorderStroke(1.dp, if (canRedo) EmeraldBorder else BorderSoft)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = if (canRedo) TextDarkBlack else Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Redo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (canRedo) TextDarkBlack else Color(0xFF9CA3AF))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Main Row 1: Card 1 (Red Header: সার ও বিশেষ খাবার) + Card 2 (Green Header: সেচ ও স্প্রে খাতা)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BentoDualGroupCard(
                    headerTitle = "সার ও বিশেষ খাবার",
                    headerColor = StrawberryHeaderRed,
                    headerIcon = Icons.Default.Spa,
                    leftTitle = "সার দেওয়ার\nনিয়ম",
                    leftDrawable = R.drawable.illus_fert_bag,
                    leftPillColor = StrawberryPillBg,
                    onLeftClick = { onNavigateFertilizer(0) },
                    leftTestTag = "bento_fertilizer_guide_card",
                    rightTitle = "সার কেনার\nখাতা",
                    rightDrawable = R.drawable.illus_fert_book,
                    rightPillColor = StrawberryPillBg,
                    onRightClick = { onNavigateFertilizer(1) },
                    rightTestTag = "bento_fertilizer_card",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                BentoDualGroupCard(
                    headerTitle = "সেচ ও স্প্রে খাতা",
                    headerColor = EmeraldHeaderGreen,
                    headerIcon = Icons.Default.WaterDrop,
                    leftTitle = "সেচ দেওয়া",
                    leftDrawable = R.drawable.illus_sprinkler,
                    leftPillColor = EmeraldPillBg,
                    onLeftClick = { onNavigateIrrigationSpray(0) },
                    leftTestTag = "bento_irrigation_card",
                    rightTitle = "ওষুধ স্প্রে",
                    rightDrawable = R.drawable.illus_spray_bottle,
                    rightPillColor = EmeraldPillBg,
                    onRightClick = { onNavigateIrrigationSpray(1) },
                    rightTestTag = "bento_spray_card",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // 3. Main Row 2: Card 3 (Green Header: শ্রমিক ও নিড়ানি হিসাব) + Card 4 (Red Header: ফসল বিক্রি ও লাভ-ক্ষতি)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BentoDualGroupCard(
                    headerTitle = "শ্রমিক ও নিড়ানি হিসাব",
                    headerColor = EmeraldHeaderGreen,
                    headerIcon = Icons.Default.Engineering,
                    leftTitle = "শ্রমিকের হিসাব",
                    leftDrawable = R.drawable.illus_worker,
                    leftPillColor = EmeraldPillBg,
                    onLeftClick = { onNavigateLabor(null) },
                    leftTestTag = "bento_labor_card",
                    rightTitle = "নিড়ানি দেওয়া",
                    rightDrawable = R.drawable.illus_weeding,
                    rightPillColor = EmeraldPillBg,
                    onRightClick = { onNavigateLabor("নিড়ানি দেওয়া") },
                    rightTestTag = "bento_weeding_card",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                BentoDualGroupCard(
                    headerTitle = "ফসল বিক্রি ও লাভ-ক্ষতি",
                    headerColor = StrawberryHeaderRed,
                    headerIcon = Icons.Default.Eco,
                    leftTitle = "বিক্রির হিসাব",
                    leftDrawable = R.drawable.illus_crate,
                    leftPillColor = StrawberryPillBg,
                    onLeftClick = { onNavigateHarvest(0) },
                    leftTestTag = "bento_harvest_card",
                    rightTitle = "লাভ-ক্ষতি দেখুন",
                    rightDrawable = R.drawable.illus_profit_chart,
                    rightPillColor = StrawberryPillBg,
                    onRightClick = { onNavigateHarvest(1) },
                    rightTestTag = "bento_profit_card",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // 4. Bottom 3-Column Row: Calculator (Blue) + History/Backup (Purple) + Healthy Crop Articles (Green)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BentoSingleToolCard(
                    headerTitle = "ক্যালকুলেটর",
                    headerColor = CalculatorBlue,
                    headerIcon = Icons.Default.Calculate,
                    drawableRes = R.drawable.illus_calculator,
                    pillLabel = "সহজ হিসাব করুন",
                    pillColor = CalculatorPillBg,
                    onClick = onOpenCalculator,
                    testTag = "bento_calculator_card",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                BentoSingleToolCard(
                    headerTitle = "খাতা, এডিট ও ব্যাকআপ",
                    headerColor = HistoryPurple,
                    headerIcon = Icons.Default.History,
                    drawableRes = R.drawable.illus_backup_folder,
                    pillLabel = "ইতিহাস ও ব্যাকআপ",
                    pillColor = HistoryPillBg,
                    onClick = onNavigateHistory,
                    testTag = "bento_history_card",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Motivational Healthy Crop Card -> Opens Articles Screen
                Card(
                    onClick = onNavigateArticles,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("bento_articles_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthyCardGreenBg),
                    border = BorderStroke(1.5.dp, EmeraldBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp, start = 6.dp, end = 6.dp, bottom = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = "কৃষি পরামর্শ",
                                tint = EmeraldHeaderGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.appConfig.dashboardSubtitle.ifBlank { "সুস্থ গাছে সমৃদ্ধ ফসল" },
                                color = EmeraldHeaderGreen,
                                fontSize = 14.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "পরামর্শ পড়ুন →",
                                color = TextDarkBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.illus_healthy_strawberries),
                            contentDescription = "সমৃদ্ধ স্ট্রবেরি",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                        )
                    }
                }
            }
        }

        // 5. Notice Bar matching reference ("আমাদের লক্ষ্য: বেশি ফল, বেশি লাভ")
        item {
            Surface(
                onClick = onNavigateArticles,
                shape = RoundedCornerShape(24.dp),
                color = NoticeBannerBg,
                border = BorderStroke(1.5.dp, NoticeBannerBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_notice_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF08A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "লক্ষ্য ও নোটিশ",
                            tint = AmberAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.appConfig.welcomeBanner.ifBlank { "আমাদের লক্ষ্য: বেশি ফল, বেশি লাভ" },
                        color = TextDarkBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Subtle green accent swoosh like reference
                    Canvas(modifier = Modifier.width(42.dp).height(12.dp)) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.8f)
                            quadraticTo(size.width * 0.5f, 0f, size.width, size.height * 0.35f)
                        }
                        drawPath(
                            path = path,
                            color = EmeraldHeaderGreen,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // 6. Compact Live Summary Strip (Income / Expense / Net Profit / Next Date)
        item {
            Card(
                onClick = { onNavigateHarvest(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_profit_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = BorderStroke(1.5.dp, if (summary.netProfit >= 0) EmeraldBorder else StrawberryBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactStatPill(
                            label = "মোট বিক্রি",
                            value = BengaliFormatters.formatTaka(summary.totalSales),
                            valueColor = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatPill(
                            label = "মোট খরচ",
                            value = BengaliFormatters.formatTaka(summary.totalExpense),
                            valueColor = StrawberryRed,
                            modifier = Modifier.weight(1f)
                        )
                        CompactStatPill(
                            label = if (summary.netProfit >= 0) "নিট লাভ" else "ঘাটতি",
                            value = BengaliFormatters.formatTaka(kotlin.math.abs(summary.netProfit)),
                            valueColor = if (summary.netProfit >= 0) EmeraldHeaderGreen else StrawberryRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (summary.nextIrrigationLabel.isNotBlank() || summary.nextSprayLabel.isNotBlank() || summary.fertilizerDue > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (summary.nextIrrigationLabel.isNotBlank()) {
                                Text(
                                    text = "💧 পরবর্তী সেচ: ${summary.nextIrrigationLabel}",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (summary.nextSprayLabel.isNotBlank()) {
                                Text(
                                    text = "🌿 পরবর্তী স্প্রে: ${summary.nextSprayLabel}",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (summary.fertilizerDue > 0 && summary.nextSprayLabel.isBlank()) {
                                Text(
                                    text = "⚠️ সার বাকি: ${BengaliFormatters.formatTaka(summary.fertilizerDue)}",
                                    color = StrawberryRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun BentoDualGroupCard(
    headerTitle: String,
    headerColor: Color,
    headerIcon: ImageVector,
    leftTitle: String,
    leftDrawable: Int,
    leftPillColor: Color,
    onLeftClick: () -> Unit,
    leftTestTag: String,
    rightTitle: String,
    rightDrawable: Int,
    rightPillColor: Color,
    onRightClick: () -> Unit,
    rightTestTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Colored Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = headerTitle,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = headerTitle,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Two White Sub-Cards with Illustrations and Pill Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BentoSubTile(
                    title = leftTitle,
                    drawableRes = leftDrawable,
                    pillColor = leftPillColor,
                    onClick = onLeftClick,
                    testTag = leftTestTag,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .background(BorderSoft)
                )

                BentoSubTile(
                    title = rightTitle,
                    drawableRes = rightDrawable,
                    pillColor = rightPillColor,
                    onClick = onRightClick,
                    testTag = rightTestTag,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BentoSubTile(
    title: String,
    drawableRes: Int,
    pillColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = pillColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = TextDarkBlack,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BentoSingleToolCard(
    headerTitle: String,
    headerColor: Color,
    headerIcon: ImageVector,
    drawableRes: Int,
    pillLabel: String,
    pillColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
        border = BorderStroke(1.dp, BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 6.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = headerTitle,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = headerTitle,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = pillLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = pillColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 8.dp)
            ) {
                Text(
                    text = pillLabel,
                    color = TextDarkBlack,
                    fontSize = 11.5.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactStatPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = BgOffWhite,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = value,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
