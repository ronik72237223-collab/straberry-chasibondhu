package com.example.data.repository

import com.example.data.local.FarmDao
import com.example.data.local.toLocalEntity
import com.example.model.AppConfig
import com.example.model.DefaultFeedingRecipes
import com.example.model.FeedingRecipe
import com.example.model.FertilizerPurchase
import com.example.model.GUEST_USER_ID
import com.example.model.HarvestSale
import com.example.model.IrrigationLog
import com.example.model.LaborLog
import com.example.model.SprayLog
import com.example.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class LocalFarmRepository(
    private val dao: FarmDao,
    private val firestore: FirebaseFirestore? = null
) : FarmRepository {

    override fun observeAppConfig(): Flow<AppConfig> {
        val db = firestore ?: return kotlinx.coroutines.flow.flowOf(AppConfig())
        return db.collection("app_config").document("settings")
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(AppConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AppConfig()
                } else {
                    AppConfig()
                }
            }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(AppConfig())
            }
    }

    override fun observeFeedingRecipes(): Flow<List<FeedingRecipe>> {
        val db = firestore ?: return kotlinx.coroutines.flow.flowOf(DefaultFeedingRecipes.items)
        return db.collection("feeding_recipes")
            .snapshots()
            .map { snap ->
                val list = snap.toObjects(FeedingRecipe::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (list.isEmpty()) DefaultFeedingRecipes.items else list.sortedBy { it.orderIndex }
            }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(DefaultFeedingRecipes.items)
            }
    }

    override fun observeFertilizers(): Flow<List<FertilizerPurchase>> =
        dao.observeFertilizers().map { list -> list.map { it.toModel() } }

    override fun observeIrrigations(): Flow<List<IrrigationLog>> =
        dao.observeIrrigations().map { list -> list.map { it.toModel() } }

    override fun observeSprays(): Flow<List<SprayLog>> =
        dao.observeSprays().map { list -> list.map { it.toModel() } }

    override fun observeLabors(): Flow<List<LaborLog>> =
        dao.observeLabors().map { list -> list.map { it.toModel() } }

    override fun observeHarvests(): Flow<List<HarvestSale>> =
        dao.observeHarvests().map { list -> list.map { it.toModel() } }

    override suspend fun addFertilizer(item: FertilizerPurchase): Result<FertilizerPurchase> = runCatching {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val saved = item.copy(id = id, userId = GUEST_USER_ID)
        dao.upsertFertilizer(saved.toLocalEntity())
        saved
    }

    override suspend fun updateFertilizer(item: FertilizerPurchase): Result<Unit> = runCatching {
        dao.upsertFertilizer(item.copy(userId = GUEST_USER_ID).toLocalEntity())
    }

    override suspend fun setFertilizerTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> = runCatching {
        val existing = dao.observeFertilizers().first().firstOrNull { it.id == id } ?: return@runCatching
        dao.upsertFertilizer(existing.copy(trashed = trashed, trashedAtMillis = if (trashed) trashedAtMillis else 0L))
    }

    override suspend fun setFertilizerDue(id: String, due: Boolean): Result<Unit> = runCatching {
        val existing = dao.observeFertilizers().first().firstOrNull { it.id == id } ?: return@runCatching
        dao.upsertFertilizer(existing.copy(due = due))
    }

    override suspend fun deleteFertilizerPermanently(id: String): Result<Unit> = runCatching {
        dao.deleteFertilizer(id)
    }

    override suspend fun addIrrigation(item: IrrigationLog): Result<IrrigationLog> = runCatching {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val saved = item.copy(id = id, userId = GUEST_USER_ID)
        dao.upsertIrrigation(saved.toLocalEntity())
        saved
    }

    override suspend fun updateIrrigation(item: IrrigationLog): Result<Unit> = runCatching {
        dao.upsertIrrigation(item.copy(userId = GUEST_USER_ID).toLocalEntity())
    }

    override suspend fun setIrrigationTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> = runCatching {
        val existing = dao.observeIrrigations().first().firstOrNull { it.id == id } ?: return@runCatching
        dao.upsertIrrigation(existing.copy(trashed = trashed, trashedAtMillis = if (trashed) trashedAtMillis else 0L))
    }

    override suspend fun deleteIrrigationPermanently(id: String): Result<Unit> = runCatching {
        dao.deleteIrrigation(id)
    }

    override suspend fun addSpray(item: SprayLog): Result<SprayLog> = runCatching {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val saved = item.copy(id = id, userId = GUEST_USER_ID)
        dao.upsertSpray(saved.toLocalEntity())
        saved
    }

    override suspend fun updateSpray(item: SprayLog): Result<Unit> = runCatching {
        dao.upsertSpray(item.copy(userId = GUEST_USER_ID).toLocalEntity())
    }

    override suspend fun setSprayTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> = runCatching {
        val existing = dao.observeSprays().first().firstOrNull { it.id == id } ?: return@runCatching
        dao.upsertSpray(existing.copy(trashed = trashed, trashedAtMillis = if (trashed) trashedAtMillis else 0L))
    }

    override suspend fun deleteSprayPermanently(id: String): Result<Unit> = runCatching {
        dao.deleteSpray(id)
    }

    override suspend fun addLabor(item: LaborLog): Result<LaborLog> = runCatching {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val saved = item.copy(id = id, userId = GUEST_USER_ID)
        dao.upsertLabor(saved.toLocalEntity())
        saved
    }

    override suspend fun updateLabor(item: LaborLog): Result<Unit> = runCatching {
        dao.upsertLabor(item.copy(userId = GUEST_USER_ID).toLocalEntity())
    }

    override suspend fun setLaborTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> = runCatching {
        val existing = dao.observeLabors().first().firstOrNull { it.id == id } ?: return@runCatching
        dao.upsertLabor(existing.copy(trashed = trashed, trashedAtMillis = if (trashed) trashedAtMillis else 0L))
    }

    override suspend fun deleteLaborPermanently(id: String): Result<Unit> = runCatching {
        dao.deleteLabor(id)
    }

    override suspend fun addHarvest(item: HarvestSale): Result<HarvestSale> = runCatching {
        val id = item.id.ifBlank { UUID.randomUUID().toString() }
        val saved = item.copy(id = id, userId = GUEST_USER_ID)
        dao.upsertHarvest(saved.toLocalEntity())
        saved
    }

    override suspend fun updateHarvest(item: HarvestSale): Result<Unit> = runCatching {
        dao.upsertHarvest(item.copy(userId = GUEST_USER_ID).toLocalEntity())
    }

    override suspend fun setHarvestTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> = runCatching {
        val existing = dao.observeHarvests().first().firstOrNull { it.id == id } ?: return@runCatching
        dao.upsertHarvest(existing.copy(trashed = trashed, trashedAtMillis = if (trashed) trashedAtMillis else 0L))
    }

    override suspend fun deleteHarvestPermanently(id: String): Result<Unit> = runCatching {
        dao.deleteHarvest(id)
    }

    override suspend fun emptyTrash(): Result<Unit> = runCatching {
        dao.emptyTrashedFertilizers()
        dao.emptyTrashedIrrigations()
        dao.emptyTrashedSprays()
        dao.emptyTrashedLabors()
        dao.emptyTrashedHarvests()
    }

    override suspend fun saveAppConfig(config: AppConfig): Result<Unit> =
        Result.failure(IllegalStateException("অ্যাডমিন সেটিংস পরিবর্তনের জন্য সুপার অ্যাডমিন লগইন প্রয়োজন।"))

    override suspend fun saveFeedingRecipe(recipe: FeedingRecipe): Result<FeedingRecipe> =
        Result.failure(IllegalStateException("রেসিপি পরিবর্তনের জন্য সুপার অ্যাডমিন লগইন প্রয়োজন।"))

    override suspend fun deleteFeedingRecipe(recipeId: String): Result<Unit> =
        Result.failure(IllegalStateException("রেসিপি মুছতে সুপার অ্যাডমিন লগইন প্রয়োজন।"))

    override suspend fun seedDefaultRecipesIfEmpty(): Result<Unit> = Result.success(Unit)

    override suspend fun fetchRegisteredUsers(): Result<List<UserProfile>> = Result.success(emptyList())
}
