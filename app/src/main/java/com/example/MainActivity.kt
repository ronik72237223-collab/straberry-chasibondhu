package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.FarmDatabase
import com.example.data.local.SessionPrefs
import com.example.data.repository.FirestoreFarmRepository
import com.example.data.repository.LocalFarmRepository
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.ArticlesScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalculatorBottomSheet
import com.example.ui.screens.FertilizerScreen
import com.example.ui.screens.FullScreenLoader
import com.example.ui.screens.HarvestProfitScreen
import com.example.ui.screens.HistoryExportScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.IrrigationSprayScreen
import com.example.ui.screens.LaborScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashBinScreen
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.EmeraldHeaderGreen
import com.example.ui.theme.EmeraldPillBg
import com.example.ui.theme.StrawberryTheme
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.SessionState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object AppRoutes {
    const val HOME = "home"
    const val FERTILIZER_PATTERN = "fertilizer?tab={tab}"
    const val IRRIGATION_SPRAY_PATTERN = "irrigation_spray?tab={tab}"
    const val LABOR_PATTERN = "labor?workType={workType}"
    const val HARVEST_PATTERN = "harvest?tab={tab}"
    const val HISTORY = "history"
    const val ARTICLES = "articles"
    const val SETTINGS = "settings"
    const val TRASH = "trash"
    const val ADMIN = "admin"

    fun fertilizer(tab: Int = 0) = "fertilizer?tab=$tab"
    fun irrigationSpray(tab: Int = 0) = "irrigation_spray?tab=$tab"
    fun labor(workType: String? = null): String {
        val encoded = if (workType.isNullOrBlank()) "" else Uri.encode(workType)
        return "labor?workType=$encoded"
    }
    fun harvest(tab: Int = 0) = "harvest?tab=$tab"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StrawberryTheme {
                StrawberryRootApp()
            }
        }
    }
}

@Composable
fun StrawberryRootApp() {
    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                AuthViewModel(SessionPrefs(app.applicationContext))
            }
        }
    )

    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()

    when (val session = sessionState) {
        SessionState.Loading -> FullScreenLoader()
        SessionState.Unauthenticated -> {
            AuthScreen(
                authUiState = authUiState,
                onLogin = authViewModel::signInWithEmail,
                onSignUp = authViewModel::signUpWithEmail,
                onForgotPassword = authViewModel::sendPasswordReset,
                onGuestMode = authViewModel::enterGuestMode,
                onClearMessages = authViewModel::clearMessages
            )
        }
        is SessionState.Active -> {
            // Key by sessionId so switching between Guest / User / Admin cleanly resets listeners & state
            key(session.sessionId) {
                AuthenticatedAppShell(
                    session = session,
                    onSignOut = authViewModel::signOut
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedAppShell(
    session: SessionState.Active,
    onSignOut: () -> Unit
) {
    val farmViewModel: FarmViewModel = viewModel(
        key = session.sessionId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val firestore = Firebase.firestore
                val repo = if (session.isGuest) {
                    val dao = FarmDatabase.getInstance(app.applicationContext).farmDao()
                    LocalFarmRepository(dao = dao, firestore = firestore)
                } else {
                    FirestoreFarmRepository(db = firestore, userId = session.sessionId)
                }
                FarmViewModel(
                    repository = repo,
                    sessionId = session.sessionId,
                    isSuperAdmin = session.isSuperAdmin
                )
            }
        }
    )

    val state by farmViewModel.uiState.collectAsStateWithLifecycle()
    val canUndo by farmViewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by farmViewModel.canRedo.collectAsStateWithLifecycle()
    val copiedCalcAmount by farmViewModel.copiedCalculatorAmount.collectAsStateWithLifecycle()
    val adminUsers by farmViewModel.adminUsers.collectAsStateWithLifecycle()
    val adminLoadingUsers by farmViewModel.adminLoadingUsers.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(farmViewModel) {
        farmViewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var showCalculator by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.HOME

    val isHomeSelected = currentRoute == AppRoutes.HOME
    val isLedgerSelected = currentRoute.startsWith("fertilizer") ||
        currentRoute.startsWith("irrigation_spray") ||
        currentRoute.startsWith("labor") ||
        currentRoute.startsWith("harvest")
    val isHistorySelected = currentRoute == AppRoutes.HISTORY || currentRoute == AppRoutes.TRASH
    val isSettingsSelected = currentRoute == AppRoutes.SETTINGS ||
        currentRoute == AppRoutes.ADMIN ||
        currentRoute == AppRoutes.ARTICLES

    Scaffold(
        containerColor = BgOffWhite,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = TextDarkBlack,
                    contentColor = SurfacePureWhite,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        floatingActionButton = {
            if (isHomeSelected) {
                FloatingActionButton(
                    onClick = { showCalculator = true },
                    containerColor = EmeraldHeaderGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("global_calculator_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "ক্যালকুলেটর",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfacePureWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldHeaderGreen,
                    selectedTextColor = EmeraldHeaderGreen,
                    indicatorColor = EmeraldPillBg,
                    unselectedIconColor = TextDarkBlack,
                    unselectedTextColor = TextDarkBlack
                )

                NavigationBarItem(
                    selected = isHomeSelected && !showCalculator,
                    onClick = {
                        showCalculator = false
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "হোম") },
                    label = { Text("হোম", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("bottom_nav_home")
                )

                NavigationBarItem(
                    selected = isLedgerSelected && !showCalculator,
                    onClick = {
                        showCalculator = false
                        navController.navigate(AppRoutes.fertilizer(1)) {
                            popUpTo(AppRoutes.HOME)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "খাতা") },
                    label = { Text("খাতা", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("bottom_nav_ledger")
                )

                NavigationBarItem(
                    selected = showCalculator,
                    onClick = { showCalculator = true },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "ক্যালকুলেটর") },
                    label = { Text("ক্যালকুলেটর", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("bottom_nav_calculator")
                )

                NavigationBarItem(
                    selected = isHistorySelected && !showCalculator,
                    onClick = {
                        showCalculator = false
                        navController.navigate(AppRoutes.HISTORY) {
                            popUpTo(AppRoutes.HOME)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = "ইতিহাস") },
                    label = { Text("ইতিহাস", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("bottom_nav_history")
                )

                NavigationBarItem(
                    selected = isSettingsSelected && !showCalculator,
                    onClick = {
                        showCalculator = false
                        navController.navigate(AppRoutes.SETTINGS) {
                            popUpTo(AppRoutes.HOME)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "সেটিংস") },
                    label = { Text("সেটিংস", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("bottom_nav_settings")
                )
            }
        }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = outerPadding.calculateBottomPadding())
        ) {
            composable(AppRoutes.HOME) {
                HomeDashboardScreen(
                    session = session,
                    state = state,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onNavigateFertilizer = { tab -> navController.navigate(AppRoutes.fertilizer(tab)) },
                    onNavigateIrrigationSpray = { tab -> navController.navigate(AppRoutes.irrigationSpray(tab)) },
                    onNavigateLabor = { workFilter -> navController.navigate(AppRoutes.labor(workFilter)) },
                    onNavigateHarvest = { tab -> navController.navigate(AppRoutes.harvest(tab)) },
                    onOpenCalculator = { showCalculator = true },
                    onNavigateHistory = { navController.navigate(AppRoutes.HISTORY) },
                    onNavigateArticles = { navController.navigate(AppRoutes.ARTICLES) },
                    onNavigateAdmin = {
                        if (session.isSuperAdmin) {
                            navController.navigate(AppRoutes.ADMIN)
                        }
                    }
                )
            }
            composable(
                route = AppRoutes.FERTILIZER_PATTERN,
                arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
            ) { entry ->
                val initialTab = entry.arguments?.getInt("tab") ?: 0
                FertilizerScreen(
                    state = state,
                    initialTab = initialTab,
                    copiedCalculatorAmount = copiedCalcAmount,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onOpenArticles = { navController.navigate(AppRoutes.ARTICLES) },
                    onSaveFeedingPlan = farmViewModel::saveCustomFeedingPlan,
                    onTrashFeedingPlan = farmViewModel::trashCustomFeedingPlan,
                    onSavePurchase = farmViewModel::saveFertilizer,
                    onTrashPurchase = farmViewModel::trashFertilizer,
                    onPayDue = farmViewModel::payFertilizerDue,
                    onClearCopiedAmount = farmViewModel::clearCopiedCalculatorAmount
                )
            }
            composable(
                route = AppRoutes.IRRIGATION_SPRAY_PATTERN,
                arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
            ) { entry ->
                val initialTab = entry.arguments?.getInt("tab") ?: 0
                IrrigationSprayScreen(
                    state = state,
                    initialTab = initialTab,
                    copiedCalculatorAmount = copiedCalcAmount,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onSaveIrrigation = farmViewModel::saveIrrigation,
                    onTrashIrrigation = farmViewModel::trashIrrigation,
                    onSaveSpray = farmViewModel::saveSpray,
                    onTrashSpray = farmViewModel::trashSpray,
                    onClearCopiedAmount = farmViewModel::clearCopiedCalculatorAmount
                )
            }
            composable(
                route = AppRoutes.LABOR_PATTERN,
                arguments = listOf(
                    navArgument("workType") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                val rawWorkType = entry.arguments?.getString("workType")
                val initialFilter = rawWorkType?.takeIf { it.isNotBlank() }?.let { Uri.decode(it) }
                LaborScreen(
                    state = state,
                    initialWorkFilter = initialFilter,
                    copiedCalculatorAmount = copiedCalcAmount,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onSaveLabor = farmViewModel::saveLabor,
                    onTrashLabor = farmViewModel::trashLabor,
                    onClearCopiedAmount = farmViewModel::clearCopiedCalculatorAmount
                )
            }
            composable(
                route = AppRoutes.HARVEST_PATTERN,
                arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
            ) { entry ->
                val initialTab = entry.arguments?.getInt("tab") ?: 0
                HarvestProfitScreen(
                    state = state,
                    initialTab = initialTab,
                    copiedCalculatorAmount = copiedCalcAmount,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onSaveHarvest = farmViewModel::saveHarvest,
                    onTrashHarvest = farmViewModel::trashHarvest,
                    onClearCopiedAmount = farmViewModel::clearCopiedCalculatorAmount
                )
            }
            composable(AppRoutes.HISTORY) {
                HistoryExportScreen(
                    state = state,
                    farmerName = session.displayName,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onNotify = farmViewModel::notifyMessage
                )
            }
            composable(AppRoutes.ARTICLES) {
                ArticlesScreen(
                    state = state,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.SETTINGS) {
                SettingsScreen(
                    session = session,
                    state = state,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onOpenArticles = { navController.navigate(AppRoutes.ARTICLES) },
                    onOpenTrash = { navController.navigate(AppRoutes.TRASH) },
                    onOpenHistory = { navController.navigate(AppRoutes.HISTORY) },
                    onOpenAdmin = {
                        if (session.isSuperAdmin) {
                            navController.navigate(AppRoutes.ADMIN)
                        }
                    },
                    onNotify = farmViewModel::notifyMessage,
                    onSignOut = onSignOut
                )
            }
            composable(AppRoutes.TRASH) {
                TrashBinScreen(
                    state = state,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onRestore = farmViewModel::restoreTrashItem,
                    onDeletePermanently = farmViewModel::deleteTrashItemPermanently,
                    onEmptyTrash = farmViewModel::emptyTrash
                )
            }
            composable(AppRoutes.ADMIN) {
                AdminPanelScreen(
                    isSuperAdmin = session.isSuperAdmin,
                    state = state,
                    users = adminUsers,
                    loadingUsers = adminLoadingUsers,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onSaveConfig = farmViewModel::saveAppConfig,
                    onSaveRecipe = farmViewModel::saveFeedingRecipe,
                    onDeleteRecipe = farmViewModel::deleteFeedingRecipe,
                    onSeedRecipes = farmViewModel::seedDefaultRecipes,
                    onSaveArticle = farmViewModel::saveArticle,
                    onDeleteArticle = farmViewModel::deleteArticle,
                    onSeedArticles = farmViewModel::seedDefaultArticles,
                    onLoadUsers = farmViewModel::loadRegisteredUsers
                )
            }
        }
    }

    if (showCalculator) {
        CalculatorBottomSheet(
            onDismiss = { showCalculator = false },
            onCopyResult = { amount ->
                farmViewModel.setCopiedCalculatorAmount(amount)
            }
        )
    }
}
