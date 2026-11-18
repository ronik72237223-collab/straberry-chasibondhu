package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppConfig
import com.example.model.DefaultFarmArticles
import com.example.model.FarmArticle
import com.example.model.FeedingRecipe
import com.example.model.UserProfile
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldHeaderGreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.FarmDashboardState
import com.example.util.BengaliFormatters

@Composable
fun AdminPanelScreen(
    isSuperAdmin: Boolean,
    state: FarmDashboardState,
    users: List<UserProfile>,
    loadingUsers: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onBack: () -> Unit,
    onSaveConfig: (AppConfig) -> Unit,
    onSaveRecipe: (FeedingRecipe) -> Unit,
    onDeleteRecipe: (String) -> Unit,
    onSeedRecipes: () -> Unit,
    onSaveArticle: (FarmArticle) -> Unit,
    onDeleteArticle: (String) -> Unit,
    onSeedArticles: () -> Unit,
    onLoadUsers: () -> Unit
) {
    if (!isSuperAdmin) {
        Scaffold(
            containerColor = BgOffWhite,
            topBar = {
                StrawberryTopBar(
                    title = "প্রবেশাধিকার সংরক্ষিত",
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = onUndo,
                    onRedo = onRedo,
                    onBack = onBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyStateCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "শুধুমাত্র সুপার অ্যাডমিনের জন্য",
                    subtitle = "এই কন্ট্রোল প্যানেল ব্যবহার করার অনুমতি আপনার অ্যাকাউন্টে নেই।",
                    actionLabel = "ড্যাশবোর্ডে ফিরে যান",
                    onAction = onBack
                )
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showRecipeDialog by remember { mutableStateOf(false) }
    var editingRecipe by remember { mutableStateOf<FeedingRecipe?>(null) }
    var showArticleDialog by remember { mutableStateOf(false) }
    var editingArticle by remember { mutableStateOf<FarmArticle?>(null) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) {
            onLoadUsers()
        }
    }

    Scaffold(
        containerColor = BgOffWhite,
        topBar = {
            StrawberryTopBar(
                title = "অ্যাডমিন কন্ট্রোল প্যানেল",
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onBack = onBack
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                1 -> FloatingActionButton(
                    onClick = { editingRecipe = null; showRecipeDialog = true },
                    containerColor = StrawberryRed,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_recipe_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "নতুন রেসিপি")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নতুন রেসিপি", fontWeight = FontWeight.Bold)
                    }
                }
                2 -> FloatingActionButton(
                    onClick = { editingArticle = null; showArticleDialog = true },
                    containerColor = EmeraldHeaderGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_article_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "নতুন আর্টিকেল")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("আর্টিকেল লিখুন", fontWeight = FontWeight.Bold)
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfacePureWhite,
                contentColor = TextDarkBlack,
                edgePadding = 8.dp,
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
                    text = { Text("অ্যাপ সেটিংস", fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("সার রেসিপি", fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("আর্টিকেল প্রকাশ", fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("চাষি তালিকা", fontWeight = FontWeight.Bold, color = TextDarkBlack) }
                )
            }

            when (selectedTab) {
                0 -> AdminSettingsTab(
                    currentConfig = state.appConfig,
                    onSaveConfig = onSaveConfig
                )
                1 -> AdminRecipesTab(
                    recipes = state.recipes,
                    onEdit = { editingRecipe = it; showRecipeDialog = true },
                    onDelete = { onDeleteRecipe(it.id) },
                    onSeedDefaults = onSeedRecipes
                )
                2 -> AdminArticlesTab(
                    articles = state.articles,
                    onEdit = { editingArticle = it; showArticleDialog = true },
                    onDelete = { onDeleteArticle(it.id) },
                    onSeedDefaults = onSeedArticles
                )
                3 -> AdminUsersTab(
                    users = users,
                    loading = loadingUsers,
                    onRefresh = onLoadUsers
                )
            }
        }
    }

    if (showRecipeDialog) {
        AdminRecipeFormDialog(
            initial = editingRecipe,
            nextOrder = (state.recipes.maxOfOrNull { it.orderIndex } ?: 0L) + 1L,
            onDismiss = { showRecipeDialog = false },
            onSave = { recipe ->
                onSaveRecipe(recipe)
                showRecipeDialog = false
            }
        )
    }

    if (showArticleDialog) {
        AdminArticleFormDialog(
            initial = editingArticle,
            nextOrder = (state.articles.maxOfOrNull { it.orderIndex } ?: 0L) + 1L,
            onDismiss = { showArticleDialog = false },
            onSave = { article ->
                onSaveArticle(article)
                showArticleDialog = false
            }
        )
    }
}

@Composable
private fun AdminSettingsTab(
    currentConfig: AppConfig,
    onSaveConfig: (AppConfig) -> Unit
) {
    var appTitle by remember(currentConfig) { mutableStateOf(currentConfig.appTitle) }
    var welcomeBanner by remember(currentConfig) { mutableStateOf(currentConfig.welcomeBanner) }
    var dashboardSubtitle by remember(currentConfig) { mutableStateOf(currentConfig.dashboardSubtitle) }
    var intervalText by remember(currentConfig) { mutableStateOf(currentConfig.irrigationIntervalDays.toString()) }
    var workTypesText by remember(currentConfig) { mutableStateOf(currentConfig.workTypes.joinToString(", ")) }
    var fertPresetsText by remember(currentConfig) { mutableStateOf(currentConfig.fertilizerPresets.joinToString(", ")) }
    var sprayPresetsText by remember(currentConfig) { mutableStateOf(currentConfig.sprayPresets.joinToString(", ")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
            border = BorderStroke(1.dp, BorderSoft)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "অ্যাপের লাইভ সেটিংস পরিবর্তন করুন",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                )
                HighContrastTextField(
                    value = appTitle,
                    onValueChange = { appTitle = it },
                    label = "অ্যাপের শিরোনাম (App Name)",
                    testTag = "admin_app_title_input"
                )
                HighContrastTextField(
                    value = dashboardSubtitle,
                    onValueChange = { dashboardSubtitle = it },
                    label = "ড্যাশবোর্ড সাবটাইটেল (যেমন: সুস্থ গাছে সমৃদ্ধ ফসল)"
                )
                HighContrastTextField(
                    value = welcomeBanner,
                    onValueChange = { welcomeBanner = it },
                    label = "স্বাগতম নোটিশ / ব্যানার টেক্সট",
                    singleLine = false,
                    maxLines = 4,
                    testTag = "admin_banner_input"
                )
                HighContrastTextField(
                    value = intervalText,
                    onValueChange = { intervalText = it },
                    label = "স্বাভাবিক সেচের বিরতি (দিন)",
                    keyboardType = KeyboardType.Number
                )
                HighContrastTextField(
                    value = workTypesText,
                    onValueChange = { workTypesText = it },
                    label = "শ্রমিক কাজের ধরন (কমা দিয়ে আলাদা করুন)",
                    singleLine = false,
                    maxLines = 3
                )
                HighContrastTextField(
                    value = fertPresetsText,
                    onValueChange = { fertPresetsText = it },
                    label = "সারের তালিকা চিপস (কমা দিয়ে আলাদা)",
                    singleLine = false,
                    maxLines = 3
                )
                HighContrastTextField(
                    value = sprayPresetsText,
                    onValueChange = { sprayPresetsText = it },
                    label = "স্প্রে ওষুধ চিপস (কমা দিয়ে আলাদা)",
                    singleLine = false,
                    maxLines = 3
                )

                Button(
                    onClick = {
                        val interval = (BengaliFormatters.parseFlexibleLong(intervalText) ?: 4L).coerceIn(1L, 60L)
                        onSaveConfig(
                            AppConfig(
                                appTitle = appTitle.trim().ifBlank { "স্ট্রবেরি চাষি বন্ধু" },
                                welcomeBanner = welcomeBanner.trim(),
                                dashboardSubtitle = dashboardSubtitle.trim().ifBlank { "সুস্থ গাছে সমৃদ্ধ ফসল" },
                                irrigationIntervalDays = interval,
                                workTypes = workTypesText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                fertilizerPresets = fertPresetsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                sprayPresets = sprayPresetsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("admin_save_config_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "সংরক্ষণ")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সেটিংস লাইভ সেভ করুন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun AdminRecipesTab(
    recipes: List<FeedingRecipe>,
    onEdit: (FeedingRecipe) -> Unit,
    onDelete: (FeedingRecipe) -> Unit,
    onSeedDefaults: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onSeedDefaults,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSoftBg, contentColor = TextDarkBlack),
                border = BorderStroke(1.dp, EmeraldBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("ডিফল্ট ৩টি রেসিপি ক্লাউডে সিঙ্ক করুন", fontWeight = FontWeight.Bold, color = TextDarkBlack)
            }
        }

        items(recipes, key = { it.id }) { recipe ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = BorderStroke(1.dp, BorderSoft)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = StrawberrySoftBg) {
                        Text(
                            text = recipe.category,
                            color = StrawberryRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(recipe.title, style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold))
                    Text(recipe.dosage, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        InlineEditDeleteRow(
                            onEdit = { onEdit(recipe) },
                            onDelete = { onDelete(recipe) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminArticlesTab(
    articles: List<FarmArticle>,
    onEdit: (FarmArticle) -> Unit,
    onDelete: (FarmArticle) -> Unit,
    onSeedDefaults: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onSeedDefaults,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSoftBg, contentColor = TextDarkBlack),
                border = BorderStroke(1.dp, EmeraldBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("ডিফল্ট ৩টি কৃষি আর্টিকেল ক্লাউডে সিঙ্ক করুন", fontWeight = FontWeight.Bold, color = TextDarkBlack)
            }
        }

        items(articles, key = { it.id }) { article ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                border = BorderStroke(1.dp, EmeraldBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = EmeraldSoftBg) {
                            Text(
                                text = article.category,
                                color = EmeraldHeaderGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (article.publishedDateLabel.isNotBlank()) {
                            Text(
                                text = article.publishedDateLabel,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                    )
                    if (article.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = article.summary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        InlineEditDeleteRow(
                            onEdit = { onEdit(article) },
                            onDelete = { onDelete(article) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<UserProfile>,
    loading: Boolean,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "নিবন্ধিত চাষি (${BengaliFormatters.formatNumber(users.size.toDouble())} জন)",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "রিফ্রেশ", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রিফ্রেশ")
                }
            }
        }

        if (loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            }
        } else if (users.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.People,
                    title = "কোনো নিবন্ধিত ব্যবহারকারী পাওয়া যায়নি",
                    subtitle = "নতুন চাষিরা ইমেইল দিয়ে অ্যাকাউন্ট খুললে এখানে তালিকা দেখা যাবে।"
                )
            }
        } else {
            items(users, key = { it.id.ifBlank { it.userId } }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.dp, BorderSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(user.displayName, style = MaterialTheme.typography.titleMedium.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold))
                        Text(user.email, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark))
                        if (user.farmName.isNotBlank()) {
                            Text("বাগান: ${user.farmName}", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRecipeFormDialog(
    initial: FeedingRecipe?,
    nextOrder: Long,
    onDismiss: () -> Unit,
    onSave: (FeedingRecipe) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: "শুকনো সার") }
    var summary by remember { mutableStateOf(initial?.summary.orEmpty()) }
    var dosage by remember { mutableStateOf(initial?.dosage.orEmpty()) }
    var stepsText by remember { mutableStateOf(initial?.steps?.joinToString("\n").orEmpty()) }
    var tip by remember { mutableStateOf(initial?.tip.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "নতুন রেসিপি যোগ করুন" else "রেসিপি সম্পাদনা",
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
                HighContrastTextField(value = title, onValueChange = { title = it; errorText = null }, label = "শিরোনাম *")
                HighContrastTextField(value = category, onValueChange = { category = it }, label = "বিভাগ (শুকনো সার / তরল সার / বিশেষ স্প্রে)")
                HighContrastTextField(value = summary, onValueChange = { summary = it }, label = "সংক্ষিপ্ত বিবরণ", singleLine = false, maxLines = 3)
                HighContrastTextField(value = dosage, onValueChange = { dosage = it }, label = "সঠিক মাপ / ডোজ", singleLine = false, maxLines = 2)
                HighContrastTextField(value = stepsText, onValueChange = { stepsText = it }, label = "ধাপসমূহ (প্রতি লাইনে একটি ধাপ)", singleLine = false, maxLines = 5)
                HighContrastTextField(value = tip, onValueChange = { tip = it }, label = "বিশেষ টিপস", singleLine = false, maxLines = 2)
                if (!errorText.isNullOrBlank()) {
                    Text(errorText!!, color = StrawberryRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorText = "শিরোনাম লিখুন।"
                        return@Button
                    }
                    onSave(
                        FeedingRecipe(
                            id = initial?.id.orEmpty(),
                            title = title.trim(),
                            category = category.trim().ifBlank { "বিশেষ খাবার" },
                            summary = summary.trim(),
                            dosage = dosage.trim(),
                            steps = stepsText.lines().map { it.trim() }.filter { it.isNotBlank() },
                            tip = tip.trim(),
                            orderIndex = initial?.orderIndex ?: nextOrder
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White)
            ) {
                Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextDarkBlack)
            }
        }
    )
}

@Composable
private fun AdminArticleFormDialog(
    initial: FarmArticle?,
    nextOrder: Long,
    onDismiss: () -> Unit,
    onSave: (FarmArticle) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: DefaultFarmArticles.categories.first()) }
    var summary by remember { mutableStateOf(initial?.summary.orEmpty()) }
    var body by remember { mutableStateOf(initial?.body.orEmpty()) }
    var publishedDateLabel by remember {
        mutableStateOf(
            initial?.publishedDateLabel?.ifBlank {
                BengaliFormatters.formatDateLabel(System.currentTimeMillis())
            } ?: BengaliFormatters.formatDateLabel(System.currentTimeMillis())
        )
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePureWhite,
        title = {
            Text(
                text = if (initial == null) "নতুন কৃষি আর্টিকেল লিখুন" else "আর্টিকেল সম্পাদনা",
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
                Text(
                    text = "বিভাগ বাছাই করুন:",
                    style = MaterialTheme.typography.labelLarge.copy(color = TextDarkBlack, fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultFarmArticles.categories.forEach { preset ->
                        FilterChip(
                            selected = category == preset,
                            onClick = { category = preset },
                            label = { Text(preset, color = TextDarkBlack, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSoftBg,
                                containerColor = SurfacePureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = category == preset,
                                borderColor = BorderSlate,
                                selectedBorderColor = EmeraldPrimary
                            )
                        )
                    }
                }
                HighContrastTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = "বিভাগের নাম *"
                )
                HighContrastTextField(
                    value = title,
                    onValueChange = { title = it; errorText = null },
                    label = "আর্টিকেলের শিরোনাম *",
                    testTag = "admin_article_title_input"
                )
                HighContrastTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = "সংক্ষিপ্ত সারসংক্ষেপ",
                    singleLine = false,
                    maxLines = 3
                )
                HighContrastTextField(
                    value = body,
                    onValueChange = { body = it; errorText = null },
                    label = "বিস্তারিত পরামর্শ ও নিয়ম *",
                    singleLine = false,
                    maxLines = 8,
                    testTag = "admin_article_body_input"
                )
                HighContrastTextField(
                    value = publishedDateLabel,
                    onValueChange = { publishedDateLabel = it },
                    label = "প্রকাশের তারিখ / লেবেল"
                )
                if (!errorText.isNullOrBlank()) {
                    Text(errorText!!, color = StrawberryRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        errorText = "শিরোনাম ও বিস্তারিত পরামর্শ লিখুন।"
                        return@Button
                    }
                    onSave(
                        FarmArticle(
                            id = initial?.id.orEmpty(),
                            title = title.trim(),
                            category = category.trim().ifBlank { "কৃষি পরামর্শ" },
                            summary = summary.trim(),
                            body = body.trim(),
                            publishedDateLabel = publishedDateLabel.trim().ifBlank {
                                BengaliFormatters.formatDateLabel(System.currentTimeMillis())
                            },
                            orderIndex = initial?.orderIndex ?: nextOrder
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White),
                modifier = Modifier.testTag("admin_article_save_button")
            ) {
                Text("প্রকাশ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextDarkBlack)
            }
        }
    )
}
