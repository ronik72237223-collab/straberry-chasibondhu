package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DefaultFarmArticles
import com.example.model.FarmArticle
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldHeaderGreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.HealthyCardGreenBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun ArticlesScreen(
    state: FarmDashboardState,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val availableCategories = remember(state.articles) {
        (DefaultFarmArticles.categories + state.articles.map { it.category })
            .filter { it.isNotBlank() }
            .distinct()
    }

    val filteredArticles = remember(state.articles, selectedCategory) {
        if (selectedCategory == null) state.articles
        else state.articles.filter { it.category == selectedCategory }
    }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "কৃষি পরামর্শ ও আর্টিকেল",
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Banner matching "সুস্থ গাছে সমৃদ্ধ ফসল"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthyCardGreenBg),
                    border = BorderStroke(1.5.dp, EmeraldBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সুস্থ গাছে সমৃদ্ধ ফসল",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = EmeraldHeaderGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "স্ট্রবেরি চাষের আধুনিক নিয়ম, সার ডোজ, রোগবালাই দমন ও ফল বড় করার বিশেষ পরামর্শ পড়ুন।",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextDarkBlack,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Image(
                            painter = painterResource(id = R.drawable.strawberry_farm_icon),
                            contentDescription = "স্ট্রবেরি চাষের পরামর্শ",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
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
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = {
                            Text(
                                text = "সব (${BengaliFormatters.formatNumber(state.articles.size.toDouble())})",
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSoftBg,
                            containerColor = SurfacePureWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = BorderSlate,
                            selectedBorderColor = EmeraldPrimary
                        )
                    )
                    availableCategories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    color = TextDarkBlack,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSoftBg,
                                containerColor = SurfacePureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == category,
                                borderColor = BorderSlate,
                                selectedBorderColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }

            if (filteredArticles.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Article,
                        title = "এই বিভাগে এখনো কোনো আর্টিকেল নেই",
                        subtitle = "অ্যাডমিন নতুন কৃষি পরামর্শ যোগ করলে এখানে সাথে সাথে দেখা যাবে।"
                    )
                }
            } else {
                items(filteredArticles, key = { it.id }) { article ->
                    ArticleExpandableCard(article = article)
                }
            }
        }
    }
}

@Composable
private fun ArticleExpandableCard(article: FarmArticle) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("article_card_${article.id}"),
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
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = article.category,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(25.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSoftBg,
                            border = BorderStroke(1.dp, EmeraldBorder)
                        ) {
                            Text(
                                text = article.category,
                                color = EmeraldHeaderGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (article.publishedDateLabel.isNotBlank()) {
                            Text(
                                text = article.publishedDateLabel,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextDarkBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "বিস্তারিত",
                    tint = TextDarkBlack
                )
            }

            if (article.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = BorderSoft)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = article.body,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = TextDarkBlack,
                            lineHeight = 25.sp
                        )
                    )
                }
            }
        }
    }
}
