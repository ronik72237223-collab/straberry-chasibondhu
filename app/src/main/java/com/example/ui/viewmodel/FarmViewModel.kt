package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FarmRepository
import com.example.model.AppConfig
import com.example.model.CustomFeedingPlan
import com.example.model.DefaultFarmArticles
import com.example.model.DefaultFeedingRecipes
import com.example.model.FarmArticle
import com.example.model.FarmSummary
import com.example.model.FeedingRecipe
import com.example.model.FertilizerPurchase
import com.example.model.HarvestSale
import com.example.model.IrrigationLog
import com.example.model.LaborLog
import com.example.model.LedgerEntry
import com.example.model.RecordCategory
import com.example.model.SprayLog
import com.example.model.TrashItem
import com.example.model.UndoAction
import com.example.model.UserProfile
import com.example.util.BengaliFormatters
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FarmDashboardState(
    val appConfig: AppConfig = AppConfig(),
    val recipes: List<FeedingRecipe> = DefaultFeedingRecipes.items,
    val articles: List<FarmArticle> = DefaultFarmArticles.items,
    val customFeedingPlans: List<CustomFeedingPlan> = emptyList(),
    val fertilizers: List<FertilizerPurchase> = emptyList(),
    val irrigations: List<IrrigationLog> = emptyList(),
    val sprays: List<SprayLog> = emptyList(),
    val labors: List<LaborLog> = emptyList(),
    val harvests: List<HarvestSale> = emptyList(),
    val trashItems: List<TrashItem> = emptyList(),
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val summary: FarmSummary = FarmSummary(),
    val isLoaded: Boolean = false,
    val errorMessage: String? = null
)

class FarmViewModel(
    private val repository: FarmRepository,
    private val sessionId: String,
    val isSuperAdmin: Boolean
) : ViewModel() {

    private val _undoStack = MutableStateFlow<List<UndoAction>>(emptyList())
    private val _redoStack = MutableStateFlow<List<UndoAction>>(emptyList())

    val canUndo: StateFlow<Boolean> = _undoStack
        .combine(_redoStack) { u, _ -> u.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val canRedo: StateFlow<Boolean> = _redoStack
        .combine(_undoStack) { r, _ -> r.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val _copiedCalculatorAmount = MutableStateFlow<String?>(null)
    val copiedCalculatorAmount: StateFlow<String?> = _copiedCalculatorAmount.asStateFlow()

    private val _adminUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val adminUsers: StateFlow<List<UserProfile>> = _adminUsers.asStateFlow()

    private val _adminLoadingUsers = MutableStateFlow(false)
    val adminLoadingUsers: StateFlow<Boolean> = _adminLoadingUsers.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private val appConfigFlow = repository.observeAppConfig()
        .catch { err ->
            unwrapOrRethrow(err, "app_config")
            emit(AppConfig())
        }

    private val recipesFlow = repository.observeFeedingRecipes()
        .catch { err ->
            unwrapOrRethrow(err, "feeding_recipes")
            emit(DefaultFeedingRecipes.items)
        }

    private val articlesFlow = repository.observeArticles()
        .catch { err ->
            unwrapOrRethrow(err, "articles")
            emit(DefaultFarmArticles.items)
        }

    private val customPlansFlow = repository.observeCustomFeedingPlans()
        .catch { err ->
            val msg = unwrapOrRethrow(err, "custom_feeding_plans")
            _snackbarEvents.tryEmit(msg)
            emit(emptyList())
        }

    private val fertilizersFlow = repository.observeFertilizers()
        .catch { err ->
            val msg = unwrapOrRethrow(err, "fertilizer_purchases")
            _snackbarEvents.tryEmit(msg)
            emit(emptyList())
        }

    private val irrigationsFlow = repository.observeIrrigations()
        .catch { err ->
            val msg = unwrapOrRethrow(err, "irrigation_logs")
            _snackbarEvents.tryEmit(msg)
            emit(emptyList())
        }

    private val spraysFlow = repository.observeSprays()
        .catch { err ->
            val msg = unwrapOrRethrow(err, "spray_logs")
            _snackbarEvents.tryEmit(msg)
            emit(emptyList())
        }

    private val laborsFlow = repository.observeLabors()
        .catch { err ->
            val msg = unwrapOrRethrow(err, "labor_logs")
            _snackbarEvents.tryEmit(msg)
            emit(emptyList())
        }

    private val harvestsFlow = repository.observeHarvests()
        .catch { err ->
            val msg = unwrapOrRethrow(err, "harvest_sales")
            _snackbarEvents.tryEmit(msg)
            emit(emptyList())
        }

    val uiState: StateFlow<FarmDashboardState> = combine(
        combine(appConfigFlow, recipesFlow, articlesFlow, customPlansFlow) { cfg, rec, art, plans ->
            Quad(cfg, rec, art, plans)
        },
        combine(fertilizersFlow, irrigationsFlow, spraysFlow) { fert, irr, spr ->
            Triple(fert, irr, spr)
        },
        combine(laborsFlow, harvestsFlow) { lab, har ->
            Pair(lab, har)
        }
    ) { (config, recipes, articles, allPlans), (allFert, allIrr, allSpray), (allLabor, allHarvest) ->
        val activePlans = allPlans.filter { !it.trashed }.sortedByDescending { it.dateMillis }
        val activeFert = allFert.filter { !it.trashed }.sortedByDescending { it.dateMillis }
        val activeIrr = allIrr.filter { !it.trashed }.sortedByDescending { it.dateMillis }
        val activeSpray = allSpray.filter { !it.trashed }.sortedByDescending { it.dateMillis }
        val activeLabor = allLabor.filter { !it.trashed }.sortedByDescending { it.dateMillis }
        val activeHarvest = allHarvest.filter { !it.trashed }.sortedByDescending { it.dateMillis }

        val trashed = buildList {
            allPlans.filter { it.trashed }.forEach {
                add(TrashItem(it.id, RecordCategory.FEEDING_PLAN, it.title, it.dosage.ifBlank { it.method }, 0.0, it.dateLabel, it.trashedAtMillis))
            }
            allFert.filter { it.trashed }.forEach {
                add(TrashItem(it.id, RecordCategory.FERTILIZER, it.fertilizerName, it.shopName.ifBlank { it.quantity }, it.totalPrice, it.dateLabel, it.trashedAtMillis))
            }
            allIrr.filter { it.trashed }.forEach {
                add(TrashItem(it.id, RecordCategory.IRRIGATION, it.plotName, "পরবর্তী সেচ: ${it.nextIrrigationLabel}", it.cost, it.dateLabel, it.trashedAtMillis))
            }
            allSpray.filter { it.trashed }.forEach {
                add(TrashItem(it.id, RecordCategory.SPRAY, it.medicineName, it.dose, it.cost, it.dateLabel, it.trashedAtMillis))
            }
            allLabor.filter { it.trashed }.forEach {
                add(TrashItem(it.id, RecordCategory.LABOR, "${it.workType} (${BengaliFormatters.formatNumber(it.workerCount.toDouble())} জন)", "হাজিরা ${BengaliFormatters.formatTaka(it.dailyWage)}", it.totalCost, it.dateLabel, it.trashedAtMillis))
            }
            allHarvest.filter { it.trashed }.forEach {
                add(TrashItem(it.id, RecordCategory.HARVEST, "${BengaliFormatters.formatKg(it.weightKg)} বিক্রি", it.buyerName, it.totalAmount, it.dateLabel, it.trashedAtMillis))
            }
        }.sortedByDescending { it.trashedAtMillis }

        val ledger = buildList {
            activeFert.forEach {
                add(
                    LedgerEntry(
                        id = it.id,
                        category = RecordCategory.FERTILIZER,
                        title = it.fertilizerName,
                        detail = listOf(it.quantity, it.shopName).filter { s -> s.isNotBlank() }.joinToString(" • "),
                        amount = it.totalPrice,
                        isIncome = false,
                        isDue = it.due,
                        dateMillis = it.dateMillis,
                        dateLabel = it.dateLabel
                    )
                )
            }
            activeIrr.forEach {
                add(
                    LedgerEntry(
                        id = it.id,
                        category = RecordCategory.IRRIGATION,
                        title = "সেচ: ${it.plotName}",
                        detail = "পরবর্তী সম্ভাব্য সেচ: ${it.nextIrrigationLabel}",
                        amount = it.cost,
                        isIncome = false,
                        dateMillis = it.dateMillis,
                        dateLabel = it.dateLabel
                    )
                )
            }
            activeSpray.forEach {
                val nextInfo = if (it.nextSprayLabel.isNotBlank()) " • পরবর্তী: ${it.nextSprayLabel}" else ""
                add(
                    LedgerEntry(
                        id = it.id,
                        category = RecordCategory.SPRAY,
                        title = "স্প্রে: ${it.medicineName}",
                        detail = "${it.dose}$nextInfo".trim(' ', '•'),
                        amount = it.cost,
                        isIncome = false,
                        dateMillis = it.dateMillis,
                        dateLabel = it.dateLabel
                    )
                )
            }
            activeLabor.forEach {
                add(
                    LedgerEntry(
                        id = it.id,
                        category = RecordCategory.LABOR,
                        title = "${it.workType} (${BengaliFormatters.formatNumber(it.workerCount.toDouble())} জন)",
                        detail = "জনপ্রতি ${BengaliFormatters.formatTaka(it.dailyWage)}",
                        amount = it.totalCost,
                        isIncome = false,
                        dateMillis = it.dateMillis,
                        dateLabel = it.dateLabel
                    )
                )
            }
            activeHarvest.forEach {
                add(
                    LedgerEntry(
                        id = it.id,
                        category = RecordCategory.HARVEST,
                        title = "স্ট্রবেরি বিক্রি (${BengaliFormatters.formatKg(it.weightKg)})",
                        detail = it.buyerName,
                        amount = it.totalAmount,
                        isIncome = true,
                        dateMillis = it.dateMillis,
                        dateLabel = it.dateLabel
                    )
                )
            }
        }.sortedByDescending { it.dateMillis }

        val fertTotal = activeFert.sumOf { it.totalPrice }
        val fertDue = activeFert.filter { it.due }.sumOf { it.totalPrice }
        val irrTotal = activeIrr.sumOf { it.cost }
        val sprayTotal = activeSpray.sumOf { it.cost }
        val laborTotal = activeLabor.sumOf { it.totalCost }
        val totalExpense = fertTotal + irrTotal + sprayTotal + laborTotal
        val totalSales = activeHarvest.sumOf { it.totalAmount }
        val totalKg = activeHarvest.sumOf { it.weightKg }
        val latestIrr = activeIrr.maxByOrNull { it.dateMillis }
        val latestSpray = activeSpray.filter { it.nextSprayLabel.isNotBlank() }.maxByOrNull { it.dateMillis }

        FarmDashboardState(
            appConfig = config,
            recipes = recipes,
            articles = articles,
            customFeedingPlans = activePlans,
            fertilizers = activeFert,
            irrigations = activeIrr,
            sprays = activeSpray,
            labors = activeLabor,
            harvests = activeHarvest,
            trashItems = trashed,
            ledgerEntries = ledger,
            summary = FarmSummary(
                fertilizerTotal = fertTotal,
                fertilizerDue = fertDue,
                irrigationTotal = irrTotal,
                sprayTotal = sprayTotal,
                laborTotal = laborTotal,
                totalExpense = totalExpense,
                totalSales = totalSales,
                totalHarvestKg = totalKg,
                netProfit = totalSales - totalExpense,
                nextIrrigationLabel = latestIrr?.nextIrrigationLabel.orEmpty(),
                nextIrrigationPlot = latestIrr?.plotName.orEmpty(),
                nextSprayLabel = latestSpray?.nextSprayLabel.orEmpty(),
                nextSprayMedicine = latestSpray?.medicineName.orEmpty(),
                trashedCount = trashed.size
            ),
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FarmDashboardState()
    )

    init {
        if (isSuperAdmin) {
            viewModelScope.launch {
                repository.seedDefaultRecipesIfEmpty()
                repository.seedDefaultArticlesIfEmpty()
            }
        }
    }

    private fun unwrapOrRethrow(error: Throwable, tag: String): String {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w("FarmVM", "Stream warning [$tag]", root)
        return "তথ্য লোড করতে সমস্যা হয়েছে: ${root.localizedMessage ?: "পুনরায় চেষ্টা করুন"}"
    }

    private fun pushUndo(action: UndoAction) {
        _undoStack.value = (_undoStack.value + action).takeLast(30)
        _redoStack.value = emptyList()
    }

    fun setCopiedCalculatorAmount(value: String) {
        _copiedCalculatorAmount.value = value
        _snackbarEvents.tryEmit("হিসাব ($value) কপি করা হয়েছে — খরচের ঘরে পেস্ট করুন!")
    }

    fun clearCopiedCalculatorAmount() {
        _copiedCalculatorAmount.value = null
    }

    // --- Custom Feeding Plan Actions ---
    fun saveCustomFeedingPlan(existing: CustomFeedingPlan?, item: CustomFeedingPlan) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addCustomFeedingPlan(item)
                    .onSuccess { saved ->
                        pushUndo(UndoAction.AddedFeedingPlan(saved))
                        _snackbarEvents.tryEmit("নিজের সার ডোজ যোগ হয়েছে ✅")
                    }
                    .onFailure { _snackbarEvents.tryEmit("সংরক্ষণ ব্যর্থ হয়েছে: ${it.localizedMessage}") }
            } else {
                val updated = item.copy(id = existing.id, userId = existing.userId, createdAt = existing.createdAt)
                repository.updateCustomFeedingPlan(updated)
                    .onSuccess {
                        pushUndo(UndoAction.EditedFeedingPlan(existing, updated))
                        _snackbarEvents.tryEmit("সারের ডোজ আপডেট হয়েছে ✏️")
                    }
                    .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে: ${it.localizedMessage}") }
            }
        }
    }

    fun trashCustomFeedingPlan(item: CustomFeedingPlan) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.setCustomFeedingPlanTrashed(item.id, true, now)
                .onSuccess {
                    pushUndo(UndoAction.TrashedFeedingPlan(item.copy(trashed = true, trashedAtMillis = now)))
                    _snackbarEvents.tryEmit("রিসাইকেল বিনে পাঠানো হয়েছে 🗑️")
                }
        }
    }

    // --- Fertilizer Actions ---
    fun saveFertilizer(existing: FertilizerPurchase?, item: FertilizerPurchase) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addFertilizer(item)
                    .onSuccess { saved ->
                        pushUndo(UndoAction.AddedFertilizer(saved))
                        _snackbarEvents.tryEmit("সার কেনার হিসাব যোগ হয়েছে ✅")
                    }
                    .onFailure { _snackbarEvents.tryEmit("সংরক্ষণ ব্যর্থ হয়েছে: ${it.localizedMessage}") }
            } else {
                val updated = item.copy(id = existing.id, userId = existing.userId, createdAt = existing.createdAt)
                repository.updateFertilizer(updated)
                    .onSuccess {
                        pushUndo(UndoAction.EditedFertilizer(existing, updated))
                        _snackbarEvents.tryEmit("সার কেনার হিসাব আপডেট হয়েছে ✏️")
                    }
                    .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে: ${it.localizedMessage}") }
            }
        }
    }

    fun trashFertilizer(item: FertilizerPurchase) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.setFertilizerTrashed(item.id, true, now)
                .onSuccess {
                    pushUndo(UndoAction.TrashedFertilizer(item.copy(trashed = true, trashedAtMillis = now)))
                    _snackbarEvents.tryEmit("রিসাইকেল বিনে পাঠানো হয়েছে 🗑️")
                }
                .onFailure { _snackbarEvents.tryEmit("মুছতে ব্যর্থ হয়েছে") }
        }
    }

    fun payFertilizerDue(item: FertilizerPurchase) {
        viewModelScope.launch {
            repository.setFertilizerDue(item.id, false)
                .onSuccess {
                    pushUndo(UndoAction.PaidFertilizerDue(item, item.copy(due = false)))
                    _snackbarEvents.tryEmit("বাকি পরিশোধ করা হয়েছে ✅")
                }
                .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে") }
        }
    }

    // --- Irrigation Actions ---
    fun saveIrrigation(existing: IrrigationLog?, item: IrrigationLog) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addIrrigation(item)
                    .onSuccess { saved ->
                        pushUndo(UndoAction.AddedIrrigation(saved))
                        _snackbarEvents.tryEmit("সেচের হিসাব যোগ হয়েছে 💧")
                    }
                    .onFailure { _snackbarEvents.tryEmit("সংরক্ষণ ব্যর্থ হয়েছে") }
            } else {
                val updated = item.copy(id = existing.id, userId = existing.userId, createdAt = existing.createdAt)
                repository.updateIrrigation(updated)
                    .onSuccess {
                        pushUndo(UndoAction.EditedIrrigation(existing, updated))
                        _snackbarEvents.tryEmit("সেচের হিসাব আপডেট হয়েছে ✏️")
                    }
                    .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে") }
            }
        }
    }

    fun trashIrrigation(item: IrrigationLog) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.setIrrigationTrashed(item.id, true, now)
                .onSuccess {
                    pushUndo(UndoAction.TrashedIrrigation(item.copy(trashed = true, trashedAtMillis = now)))
                    _snackbarEvents.tryEmit("রিসাইকেল বিনে পাঠানো হয়েছে 🗑️")
                }
        }
    }

    // --- Spray Actions ---
    fun saveSpray(existing: SprayLog?, item: SprayLog) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addSpray(item)
                    .onSuccess { saved ->
                        pushUndo(UndoAction.AddedSpray(saved))
                        _snackbarEvents.tryEmit("স্প্রের হিসাব যোগ হয়েছে 🌿")
                    }
                    .onFailure { _snackbarEvents.tryEmit("সংরক্ষণ ব্যর্থ হয়েছে") }
            } else {
                val updated = item.copy(id = existing.id, userId = existing.userId, createdAt = existing.createdAt)
                repository.updateSpray(updated)
                    .onSuccess {
                        pushUndo(UndoAction.EditedSpray(existing, updated))
                        _snackbarEvents.tryEmit("স্প্রের হিসাব আপডেট হয়েছে ✏️")
                    }
                    .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে") }
            }
        }
    }

    fun trashSpray(item: SprayLog) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.setSprayTrashed(item.id, true, now)
                .onSuccess {
                    pushUndo(UndoAction.TrashedSpray(item.copy(trashed = true, trashedAtMillis = now)))
                    _snackbarEvents.tryEmit("রিসাইকেল বিনে পাঠানো হয়েছে 🗑️")
                }
        }
    }

    // --- Labor Actions ---
    fun saveLabor(existing: LaborLog?, item: LaborLog) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addLabor(item)
                    .onSuccess { saved ->
                        pushUndo(UndoAction.AddedLabor(saved))
                        _snackbarEvents.tryEmit("শ্রমিকের হিসাব যোগ হয়েছে 👷")
                    }
                    .onFailure { _snackbarEvents.tryEmit("সংরক্ষণ ব্যর্থ হয়েছে") }
            } else {
                val updated = item.copy(id = existing.id, userId = existing.userId, createdAt = existing.createdAt)
                repository.updateLabor(updated)
                    .onSuccess {
                        pushUndo(UndoAction.EditedLabor(existing, updated))
                        _snackbarEvents.tryEmit("শ্রমিকের হিসাব আপডেট হয়েছে ✏️")
                    }
                    .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে") }
            }
        }
    }

    fun trashLabor(item: LaborLog) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.setLaborTrashed(item.id, true, now)
                .onSuccess {
                    pushUndo(UndoAction.TrashedLabor(item.copy(trashed = true, trashedAtMillis = now)))
                    _snackbarEvents.tryEmit("রিসাইকেল বিনে পাঠানো হয়েছে 🗑️")
                }
        }
    }

    // --- Harvest Actions ---
    fun saveHarvest(existing: HarvestSale?, item: HarvestSale) {
        viewModelScope.launch {
            if (existing == null) {
                repository.addHarvest(item)
                    .onSuccess { saved ->
                        pushUndo(UndoAction.AddedHarvest(saved))
                        _snackbarEvents.tryEmit("ফসল বিক্রির হিসাব যোগ হয়েছে 🍓")
                    }
                    .onFailure { _snackbarEvents.tryEmit("সংরক্ষণ ব্যর্থ হয়েছে") }
            } else {
                val updated = item.copy(id = existing.id, userId = existing.userId, createdAt = existing.createdAt)
                repository.updateHarvest(updated)
                    .onSuccess {
                        pushUndo(UndoAction.EditedHarvest(existing, updated))
                        _snackbarEvents.tryEmit("বিক্রির হিসাব আপডেট হয়েছে ✏️")
                    }
                    .onFailure { _snackbarEvents.tryEmit("আপডেট ব্যর্থ হয়েছে") }
            }
        }
    }

    fun trashHarvest(item: HarvestSale) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.setHarvestTrashed(item.id, true, now)
                .onSuccess {
                    pushUndo(UndoAction.TrashedHarvest(item.copy(trashed = true, trashedAtMillis = now)))
                    _snackbarEvents.tryEmit("রিসাইকেল বিনে পাঠানো হয়েছে 🗑️")
                }
        }
    }

    // --- Recycle Bin Actions ---
    fun restoreTrashItem(item: TrashItem) {
        viewModelScope.launch {
            val res = when (item.category) {
                RecordCategory.FEEDING_PLAN -> repository.setCustomFeedingPlanTrashed(item.id, false, 0L)
                RecordCategory.FERTILIZER -> repository.setFertilizerTrashed(item.id, false, 0L)
                RecordCategory.IRRIGATION -> repository.setIrrigationTrashed(item.id, false, 0L)
                RecordCategory.SPRAY -> repository.setSprayTrashed(item.id, false, 0L)
                RecordCategory.LABOR -> repository.setLaborTrashed(item.id, false, 0L)
                RecordCategory.HARVEST -> repository.setHarvestTrashed(item.id, false, 0L)
            }
            res.onSuccess { _snackbarEvents.tryEmit("তথ্য সফলভাবে ফিরিয়ে আনা হয়েছে ♻️") }
                .onFailure { _snackbarEvents.tryEmit("ফিরিয়ে আনা যায়নি") }
        }
    }

    fun deleteTrashItemPermanently(item: TrashItem) {
        viewModelScope.launch {
            val res = when (item.category) {
                RecordCategory.FEEDING_PLAN -> repository.deleteCustomFeedingPlanPermanently(item.id)
                RecordCategory.FERTILIZER -> repository.deleteFertilizerPermanently(item.id)
                RecordCategory.IRRIGATION -> repository.deleteIrrigationPermanently(item.id)
                RecordCategory.SPRAY -> repository.deleteSprayPermanently(item.id)
                RecordCategory.LABOR -> repository.deleteLaborPermanently(item.id)
                RecordCategory.HARVEST -> repository.deleteHarvestPermanently(item.id)
            }
            res.onSuccess { _snackbarEvents.tryEmit("স্থায়ীভাবে মুছে ফেলা হয়েছে") }
                .onFailure { _snackbarEvents.tryEmit("মুছতে সমস্যা হয়েছে") }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
                .onSuccess { _snackbarEvents.tryEmit("রিসাইকেল বিন খালি করা হয়েছে") }
                .onFailure { _snackbarEvents.tryEmit("খালি করতে সমস্যা হয়েছে") }
        }
    }

    // --- Undo / Redo Engine ---
    fun undo() {
        val stack = _undoStack.value
        if (stack.isEmpty()) return
        val action = stack.last()
        _undoStack.value = stack.dropLast(1)
        _redoStack.value = (_redoStack.value + action).takeLast(30)

        viewModelScope.launch {
            val result = when (action) {
                is UndoAction.AddedFeedingPlan -> repository.setCustomFeedingPlanTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.EditedFeedingPlan -> repository.updateCustomFeedingPlan(action.before)
                is UndoAction.TrashedFeedingPlan -> repository.setCustomFeedingPlanTrashed(action.item.id, false, 0L)
                is UndoAction.RestoredFeedingPlan -> repository.setCustomFeedingPlanTrashed(action.item.id, true, System.currentTimeMillis())

                is UndoAction.AddedFertilizer -> repository.setFertilizerTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.EditedFertilizer -> repository.updateFertilizer(action.before)
                is UndoAction.TrashedFertilizer -> repository.setFertilizerTrashed(action.item.id, false, 0L)
                is UndoAction.RestoredFertilizer -> repository.setFertilizerTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.PaidFertilizerDue -> repository.setFertilizerDue(action.before.id, action.before.due)

                is UndoAction.AddedIrrigation -> repository.setIrrigationTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.EditedIrrigation -> repository.updateIrrigation(action.before)
                is UndoAction.TrashedIrrigation -> repository.setIrrigationTrashed(action.item.id, false, 0L)
                is UndoAction.RestoredIrrigation -> repository.setIrrigationTrashed(action.item.id, true, System.currentTimeMillis())

                is UndoAction.AddedSpray -> repository.setSprayTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.EditedSpray -> repository.updateSpray(action.before)
                is UndoAction.TrashedSpray -> repository.setSprayTrashed(action.item.id, false, 0L)
                is UndoAction.RestoredSpray -> repository.setSprayTrashed(action.item.id, true, System.currentTimeMillis())

                is UndoAction.AddedLabor -> repository.setLaborTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.EditedLabor -> repository.updateLabor(action.before)
                is UndoAction.TrashedLabor -> repository.setLaborTrashed(action.item.id, false, 0L)
                is UndoAction.RestoredLabor -> repository.setLaborTrashed(action.item.id, true, System.currentTimeMillis())

                is UndoAction.AddedHarvest -> repository.setHarvestTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.EditedHarvest -> repository.updateHarvest(action.before)
                is UndoAction.TrashedHarvest -> repository.setHarvestTrashed(action.item.id, false, 0L)
                is UndoAction.RestoredHarvest -> repository.setHarvestTrashed(action.item.id, true, System.currentTimeMillis())
            }
            result.onSuccess { _snackbarEvents.tryEmit("↩️ পূর্বের অবস্থায় ফিরিয়ে নেওয়া হয়েছে (Undo)") }
                .onFailure { _snackbarEvents.tryEmit("Undo করা যায়নি") }
        }
    }

    fun redo() {
        val stack = _redoStack.value
        if (stack.isEmpty()) return
        val action = stack.last()
        _redoStack.value = stack.dropLast(1)
        _undoStack.value = (_undoStack.value + action).takeLast(30)

        viewModelScope.launch {
            val result = when (action) {
                is UndoAction.AddedFeedingPlan -> repository.setCustomFeedingPlanTrashed(action.item.id, false, 0L)
                is UndoAction.EditedFeedingPlan -> repository.updateCustomFeedingPlan(action.after)
                is UndoAction.TrashedFeedingPlan -> repository.setCustomFeedingPlanTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.RestoredFeedingPlan -> repository.setCustomFeedingPlanTrashed(action.item.id, false, 0L)

                is UndoAction.AddedFertilizer -> repository.setFertilizerTrashed(action.item.id, false, 0L)
                is UndoAction.EditedFertilizer -> repository.updateFertilizer(action.after)
                is UndoAction.TrashedFertilizer -> repository.setFertilizerTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.RestoredFertilizer -> repository.setFertilizerTrashed(action.item.id, false, 0L)
                is UndoAction.PaidFertilizerDue -> repository.setFertilizerDue(action.after.id, action.after.due)

                is UndoAction.AddedIrrigation -> repository.setIrrigationTrashed(action.item.id, false, 0L)
                is UndoAction.EditedIrrigation -> repository.updateIrrigation(action.after)
                is UndoAction.TrashedIrrigation -> repository.setIrrigationTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.RestoredIrrigation -> repository.setIrrigationTrashed(action.item.id, false, 0L)

                is UndoAction.AddedSpray -> repository.setSprayTrashed(action.item.id, false, 0L)
                is UndoAction.EditedSpray -> repository.updateSpray(action.after)
                is UndoAction.TrashedSpray -> repository.setSprayTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.RestoredSpray -> repository.setSprayTrashed(action.item.id, false, 0L)

                is UndoAction.AddedLabor -> repository.setLaborTrashed(action.item.id, false, 0L)
                is UndoAction.EditedLabor -> repository.updateLabor(action.after)
                is UndoAction.TrashedLabor -> repository.setLaborTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.RestoredLabor -> repository.setLaborTrashed(action.item.id, false, 0L)

                is UndoAction.AddedHarvest -> repository.setHarvestTrashed(action.item.id, false, 0L)
                is UndoAction.EditedHarvest -> repository.updateHarvest(action.after)
                is UndoAction.TrashedHarvest -> repository.setHarvestTrashed(action.item.id, true, System.currentTimeMillis())
                is UndoAction.RestoredHarvest -> repository.setHarvestTrashed(action.item.id, false, 0L)
            }
            result.onSuccess { _snackbarEvents.tryEmit("↪️ পুনরায় করা হয়েছে (Redo)") }
                .onFailure { _snackbarEvents.tryEmit("Redo করা যায়নি") }
        }
    }

    // --- Admin Actions ---
    fun saveAppConfig(config: AppConfig) {
        viewModelScope.launch {
            repository.saveAppConfig(config)
                .onSuccess { _snackbarEvents.tryEmit("অ্যাপের সেটিংস সফলভাবে আপডেট হয়েছে ✅") }
                .onFailure { _snackbarEvents.tryEmit("সেটিংস সেভ ব্যর্থ: ${it.localizedMessage}") }
        }
    }

    fun saveFeedingRecipe(recipe: FeedingRecipe) {
        viewModelScope.launch {
            repository.saveFeedingRecipe(recipe)
                .onSuccess { _snackbarEvents.tryEmit("রেসিপি সংরক্ষণ করা হয়েছে ✅") }
                .onFailure { _snackbarEvents.tryEmit("রেসিপি সেভ ব্যর্থ: ${it.localizedMessage}") }
        }
    }

    fun deleteFeedingRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteFeedingRecipe(recipeId)
                .onSuccess { _snackbarEvents.tryEmit("রেসিপি মুছে ফেলা হয়েছে") }
                .onFailure { _snackbarEvents.tryEmit("রেসিপি মুছতে ব্যর্থ: ${it.localizedMessage}") }
        }
    }

    fun seedDefaultRecipes() {
        viewModelScope.launch {
            repository.seedDefaultRecipesIfEmpty()
                .onSuccess { _snackbarEvents.tryEmit("ডিফল্ট রেসিপি ডাটাবেসে যুক্ত হয়েছে") }
        }
    }

    fun saveArticle(article: FarmArticle) {
        viewModelScope.launch {
            repository.saveArticle(article)
                .onSuccess { _snackbarEvents.tryEmit("আর্টিকেল সফলভাবে প্রকাশিত হয়েছে ✅") }
                .onFailure { _snackbarEvents.tryEmit("আর্টিকেল প্রকাশ ব্যর্থ: ${it.localizedMessage}") }
        }
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            repository.deleteArticle(articleId)
                .onSuccess { _snackbarEvents.tryEmit("আর্টিকেল মুছে ফেলা হয়েছে") }
                .onFailure { _snackbarEvents.tryEmit("আর্টিকেল মুছতে ব্যর্থ: ${it.localizedMessage}") }
        }
    }

    fun seedDefaultArticles() {
        viewModelScope.launch {
            repository.seedDefaultArticlesIfEmpty()
                .onSuccess { _snackbarEvents.tryEmit("ডিফল্ট কৃষি আর্টিকেল ডাটাবেসে যুক্ত হয়েছে") }
        }
    }

    fun loadRegisteredUsers() {
        if (!isSuperAdmin) return
        viewModelScope.launch {
            _adminLoadingUsers.value = true
            repository.fetchRegisteredUsers()
                .onSuccess { _adminUsers.value = it }
                .onFailure { _snackbarEvents.tryEmit("ব্যবহারকারীর তালিকা লোড করা যায়নি") }
            _adminLoadingUsers.value = false
        }
    }

    fun notifyMessage(msg: String) {
        _snackbarEvents.tryEmit(msg)
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
