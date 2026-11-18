package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.FarmDatabase
import com.example.data.local.SessionPrefs
import com.example.data.repository.FirestoreFarmRepository
import com.example.data.repository.LocalFarmRepository
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalculatorBottomSheet
import com.example.ui.screens.FertilizerScreen
import com.example.ui.screens.FullScreenLoader
import com.example.ui.screens.HarvestProfitScreen
import com.example.ui.screens.HistoryExportScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.IrrigationSprayScreen
import com.example.ui.screens.LaborScreen
import com.example.ui.screens.TrashBinScreen
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.StrawberryRed
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
    const val FERTILIZER = "fertilizer"
    const val IRRIGATION_SPRAY = "irrigation_spray"
    const val LABOR = "labor"
    const val HARVEST = "harvest"
    const val HISTORY = "history"
    const val TRASH = "trash"
    const val ADMIN = "admin"
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
            FloatingActionButton(
                onClick = { showCalculator = true },
                containerColor = StrawberryRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("global_calculator_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "ক্যালকুলেটর")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ক্যালকুলেটর", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = outerPadding.calculateBottomPadding().coerceAtMost(0.dp))
        ) {
            composable(AppRoutes.HOME) {
                HomeDashboardScreen(
                    session = session,
                    state = state,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onNavigateFertilizer = { navController.navigate(AppRoutes.FERTILIZER) },
                    onNavigateIrrigationSpray = { navController.navigate(AppRoutes.IRRIGATION_SPRAY) },
                    onNavigateLabor = { navController.navigate(AppRoutes.LABOR) },
                    onNavigateHarvest = { navController.navigate(AppRoutes.HARVEST) },
                    onNavigateHistory = { navController.navigate(AppRoutes.HISTORY) },
                    onNavigateTrash = { navController.navigate(AppRoutes.TRASH) },
                    onNavigateAdmin = {
                        if (session.isSuperAdmin) {
                            navController.navigate(AppRoutes.ADMIN)
                        }
                    },
                    onSignOut = onSignOut
                )
            }
            composable(AppRoutes.FERTILIZER) {
                FertilizerScreen(
                    state = state,
                    copiedCalculatorAmount = copiedCalcAmount,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = farmViewModel::undo,
                    onRedo = farmViewModel::redo,
                    onBack = { navController.popBackStack() },
                    onSavePurchase = farmViewModel::saveFertilizer,
                    onTrashPurchase = farmViewModel::trashFertilizer,
                    onPayDue = farmViewModel::payFertilizerDue,
                    onClearCopiedAmount = farmViewModel::clearCopiedCalculatorAmount
                )
            }
            composable(AppRoutes.IRRIGATION_SPRAY) {
                IrrigationSprayScreen(
                    state = state,
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
            composable(AppRoutes.LABOR) {
                LaborScreen(
                    state = state,
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
            composable(AppRoutes.HARVEST) {
                HarvestProfitScreen(
                    state = state,
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
                    onLoadUsers = farmViewModel::loadRegisteredUsers,
                    onSeedRecipes = farmViewModel::seedDefaultRecipes
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
