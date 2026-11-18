package com.example.data.repository

import com.example.model.AppConfig
import com.example.model.CustomFeedingPlan
import com.example.model.DefaultFarmArticles
import com.example.model.DefaultFeedingRecipes
import com.example.model.FarmArticle
import com.example.model.FeedingRecipe
import com.example.model.FertilizerPurchase
import com.example.model.HarvestSale
import com.example.model.IrrigationLog
import com.example.model.LaborLog
import com.example.model.SUPER_ADMIN_UID
import com.example.model.SprayLog
import com.example.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFarmRepository(
    private val db: FirebaseFirestore,
    private val userId: String,
    private val auth: FirebaseAuth = Firebase.auth
) : FarmRepository {

    private fun requireActiveUid(): String {
        val uid = auth.currentUser?.uid
        require(!uid.isNullOrBlank() && uid == userId) {
            "ব্যবহারকারী লগইন অবস্থায় নেই বা অ্যাকাউন্ট পরিবর্তিত হয়েছে।"
        }
        return uid
    }

    private fun requireSuperAdmin(): String {
        val uid = requireActiveUid()
        require(uid == SUPER_ADMIN_UID) {
            "শুধুমাত্র সুপার অ্যাডমিন এই কাজটি করতে পারবেন।"
        }
        return uid
    }

    private inline fun <reified T> observeUserCollection(collectionName: String): Flow<List<T>> = flow {
        val path = "users/$userId/$collectionName"
        if (auth.currentUser?.uid != userId) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("users").document(userId).collection(collectionName)
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(T::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override fun observeAppConfig(): Flow<AppConfig> =
        db.collection("app_config").document("settings")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(AppConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AppConfig()
                } else {
                    AppConfig()
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.GET, "app_config/settings")
                emit(AppConfig())
            }

    override fun observeFeedingRecipes(): Flow<List<FeedingRecipe>> =
        db.collection("feeding_recipes")
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(
                    FeedingRecipe::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (list.isEmpty()) DefaultFeedingRecipes.items else list.sortedBy { it.orderIndex }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "feeding_recipes")
                emit(DefaultFeedingRecipes.items)
            }

    override fun observeArticles(): Flow<List<FarmArticle>> =
        db.collection("articles")
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(
                    FarmArticle::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (list.isEmpty()) DefaultFarmArticles.items else list.sortedBy { it.orderIndex }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "articles")
                emit(DefaultFarmArticles.items)
            }

    override fun observeCustomFeedingPlans(): Flow<List<CustomFeedingPlan>> =
        observeUserCollection("custom_feeding_plans")

    override fun observeFertilizers(): Flow<List<FertilizerPurchase>> =
        observeUserCollection("fertilizer_purchases")

    override fun observeIrrigations(): Flow<List<IrrigationLog>> =
        observeUserCollection("irrigation_logs")

    override fun observeSprays(): Flow<List<SprayLog>> =
        observeUserCollection("spray_logs")

    override fun observeLabors(): Flow<List<LaborLog>> =
        observeUserCollection("labor_logs")

    override fun observeHarvests(): Flow<List<HarvestSale>> =
        observeUserCollection("harvest_sales")

    // --- Custom Feeding Plan CRUD ---
    override suspend fun addCustomFeedingPlan(item: CustomFeedingPlan): Result<CustomFeedingPlan> {
        var docPath = "users/$userId/custom_feeding_plans"
        return try {
            val uid = requireActiveUid()
            val col = db.collection("users").document(uid).collection("custom_feeding_plans")
            val docRef = if (item.id.isNotBlank()) col.document(item.id) else col.document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to item.title.trim().take(160),
                "method" to item.method.trim().take(120),
                "dosage" to item.dosage.trim().take(300),
                "dateMillis" to item.dateMillis,
                "dateLabel" to item.dateLabel.take(40),
                "nextDateMillis" to item.nextDateMillis.coerceAtLeast(0L),
                "nextDateLabel" to item.nextDateLabel.take(40),
                "notes" to item.notes.trim().take(500),
                "trashed" to item.trashed,
                "trashedAtMillis" to item.trashedAtMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(item.copy(id = docRef.id, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateCustomFeedingPlan(item: CustomFeedingPlan): Result<Unit> {
        val docPath = "users/$userId/custom_feeding_plans/${item.id}"
        return try {
            val uid = requireActiveUid()
            require(item.id.isNotBlank()) { "Document ID cannot be blank" }
            db.collection("users").document(uid).collection("custom_feeding_plans").document(item.id)
                .update(
                    mapOf(
                        "title" to item.title.trim().take(160),
                        "method" to item.method.trim().take(120),
                        "dosage" to item.dosage.trim().take(300),
                        "dateMillis" to item.dateMillis,
                        "dateLabel" to item.dateLabel.take(40),
                        "nextDateMillis" to item.nextDateMillis.coerceAtLeast(0L),
                        "nextDateLabel" to item.nextDateLabel.take(40),
                        "notes" to item.notes.trim().take(500),
                        "trashed" to item.trashed,
                        "trashedAtMillis" to item.trashedAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun setCustomFeedingPlanTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> =
        partialTrashUpdate("custom_feeding_plans", id, trashed, trashedAtMillis)

    override suspend fun deleteCustomFeedingPlanPermanently(id: String): Result<Unit> =
        deleteDocument("custom_feeding_plans", id)

    // --- Fertilizer CRUD ---
    override suspend fun addFertilizer(item: FertilizerPurchase): Result<FertilizerPurchase> {
        var docPath = "users/$userId/fertilizer_purchases"
        return try {
            val uid = requireActiveUid()
            val col = db.collection("users").document(uid).collection("fertilizer_purchases")
            val docRef = if (item.id.isNotBlank()) col.document(item.id) else col.document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "dateMillis" to item.dateMillis,
                "dateLabel" to item.dateLabel.take(40),
                "fertilizerName" to item.fertilizerName.trim().take(120),
                "shopName" to item.shopName.trim().take(120),
                "quantity" to item.quantity.trim().take(80),
                "totalPrice" to item.totalPrice.coerceAtLeast(0.0),
                "due" to item.due,
                "notes" to item.notes.trim().take(500),
                "trashed" to item.trashed,
                "trashedAtMillis" to item.trashedAtMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(item.copy(id = docRef.id, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateFertilizer(item: FertilizerPurchase): Result<Unit> {
        val docPath = "users/$userId/fertilizer_purchases/${item.id}"
        return try {
            val uid = requireActiveUid()
            require(item.id.isNotBlank()) { "Document ID cannot be blank" }
            val docRef = db.collection("users").document(uid).collection("fertilizer_purchases").document(item.id)
            docRef.update(
                mapOf(
                    "dateMillis" to item.dateMillis,
                    "dateLabel" to item.dateLabel.take(40),
                    "fertilizerName" to item.fertilizerName.trim().take(120),
                    "shopName" to item.shopName.trim().take(120),
                    "quantity" to item.quantity.trim().take(80),
                    "totalPrice" to item.totalPrice.coerceAtLeast(0.0),
                    "due" to item.due,
                    "notes" to item.notes.trim().take(500),
                    "trashed" to item.trashed,
                    "trashedAtMillis" to item.trashedAtMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun setFertilizerTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> =
        partialTrashUpdate("fertilizer_purchases", id, trashed, trashedAtMillis)

    override suspend fun setFertilizerDue(id: String, due: Boolean): Result<Unit> {
        val docPath = "users/$userId/fertilizer_purchases/$id"
        return try {
            val uid = requireActiveUid()
            db.collection("users").document(uid).collection("fertilizer_purchases").document(id)
                .update(mapOf("due" to due, "updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun deleteFertilizerPermanently(id: String): Result<Unit> =
        deleteDocument("fertilizer_purchases", id)

    // --- Irrigation CRUD ---
    override suspend fun addIrrigation(item: IrrigationLog): Result<IrrigationLog> {
        var docPath = "users/$userId/irrigation_logs"
        return try {
            val uid = requireActiveUid()
            val col = db.collection("users").document(uid).collection("irrigation_logs")
            val docRef = if (item.id.isNotBlank()) col.document(item.id) else col.document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "dateMillis" to item.dateMillis,
                "dateLabel" to item.dateLabel.take(40),
                "plotName" to item.plotName.trim().take(120),
                "cost" to item.cost.coerceAtLeast(0.0),
                "nextIrrigationMillis" to item.nextIrrigationMillis,
                "nextIrrigationLabel" to item.nextIrrigationLabel.take(40),
                "notes" to item.notes.trim().take(500),
                "trashed" to item.trashed,
                "trashedAtMillis" to item.trashedAtMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(item.copy(id = docRef.id, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateIrrigation(item: IrrigationLog): Result<Unit> {
        val docPath = "users/$userId/irrigation_logs/${item.id}"
        return try {
            val uid = requireActiveUid()
            require(item.id.isNotBlank())
            db.collection("users").document(uid).collection("irrigation_logs").document(item.id)
                .update(
                    mapOf(
                        "dateMillis" to item.dateMillis,
                        "dateLabel" to item.dateLabel.take(40),
                        "plotName" to item.plotName.trim().take(120),
                        "cost" to item.cost.coerceAtLeast(0.0),
                        "nextIrrigationMillis" to item.nextIrrigationMillis,
                        "nextIrrigationLabel" to item.nextIrrigationLabel.take(40),
                        "notes" to item.notes.trim().take(500),
                        "trashed" to item.trashed,
                        "trashedAtMillis" to item.trashedAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun setIrrigationTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> =
        partialTrashUpdate("irrigation_logs", id, trashed, trashedAtMillis)

    override suspend fun deleteIrrigationPermanently(id: String): Result<Unit> =
        deleteDocument("irrigation_logs", id)

    // --- Spray CRUD ---
    override suspend fun addSpray(item: SprayLog): Result<SprayLog> {
        var docPath = "users/$userId/spray_logs"
        return try {
            val uid = requireActiveUid()
            val col = db.collection("users").document(uid).collection("spray_logs")
            val docRef = if (item.id.isNotBlank()) col.document(item.id) else col.document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "dateMillis" to item.dateMillis,
                "dateLabel" to item.dateLabel.take(40),
                "medicineName" to item.medicineName.trim().take(140),
                "dose" to item.dose.trim().take(120),
                "cost" to item.cost.coerceAtLeast(0.0),
                "nextSprayMillis" to item.nextSprayMillis.coerceAtLeast(0L),
                "nextSprayLabel" to item.nextSprayLabel.take(40),
                "notes" to item.notes.trim().take(500),
                "trashed" to item.trashed,
                "trashedAtMillis" to item.trashedAtMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(item.copy(id = docRef.id, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateSpray(item: SprayLog): Result<Unit> {
        val docPath = "users/$userId/spray_logs/${item.id}"
        return try {
            val uid = requireActiveUid()
            require(item.id.isNotBlank())
            db.collection("users").document(uid).collection("spray_logs").document(item.id)
                .update(
                    mapOf(
                        "dateMillis" to item.dateMillis,
                        "dateLabel" to item.dateLabel.take(40),
                        "medicineName" to item.medicineName.trim().take(140),
                        "dose" to item.dose.trim().take(120),
                        "cost" to item.cost.coerceAtLeast(0.0),
                        "nextSprayMillis" to item.nextSprayMillis.coerceAtLeast(0L),
                        "nextSprayLabel" to item.nextSprayLabel.take(40),
                        "notes" to item.notes.trim().take(500),
                        "trashed" to item.trashed,
                        "trashedAtMillis" to item.trashedAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun setSprayTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> =
        partialTrashUpdate("spray_logs", id, trashed, trashedAtMillis)

    override suspend fun deleteSprayPermanently(id: String): Result<Unit> =
        deleteDocument("spray_logs", id)

    // --- Labor CRUD ---
    override suspend fun addLabor(item: LaborLog): Result<LaborLog> {
        var docPath = "users/$userId/labor_logs"
        return try {
            val uid = requireActiveUid()
            val col = db.collection("users").document(uid).collection("labor_logs")
            val docRef = if (item.id.isNotBlank()) col.document(item.id) else col.document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "dateMillis" to item.dateMillis,
                "dateLabel" to item.dateLabel.take(40),
                "workerCount" to item.workerCount.coerceIn(1L, 1000L),
                "dailyWage" to item.dailyWage.coerceAtLeast(0.0),
                "totalCost" to item.totalCost.coerceAtLeast(0.0),
                "workType" to item.workType.trim().take(100),
                "notes" to item.notes.trim().take(500),
                "trashed" to item.trashed,
                "trashedAtMillis" to item.trashedAtMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(item.copy(id = docRef.id, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateLabor(item: LaborLog): Result<Unit> {
        val docPath = "users/$userId/labor_logs/${item.id}"
        return try {
            val uid = requireActiveUid()
            require(item.id.isNotBlank())
            db.collection("users").document(uid).collection("labor_logs").document(item.id)
                .update(
                    mapOf(
                        "dateMillis" to item.dateMillis,
                        "dateLabel" to item.dateLabel.take(40),
                        "workerCount" to item.workerCount.coerceIn(1L, 1000L),
                        "dailyWage" to item.dailyWage.coerceAtLeast(0.0),
                        "totalCost" to item.totalCost.coerceAtLeast(0.0),
                        "workType" to item.workType.trim().take(100),
                        "notes" to item.notes.trim().take(500),
                        "trashed" to item.trashed,
                        "trashedAtMillis" to item.trashedAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun setLaborTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> =
        partialTrashUpdate("labor_logs", id, trashed, trashedAtMillis)

    override suspend fun deleteLaborPermanently(id: String): Result<Unit> =
        deleteDocument("labor_logs", id)

    // --- Harvest CRUD ---
    override suspend fun addHarvest(item: HarvestSale): Result<HarvestSale> {
        var docPath = "users/$userId/harvest_sales"
        return try {
            val uid = requireActiveUid()
            val col = db.collection("users").document(uid).collection("harvest_sales")
            val docRef = if (item.id.isNotBlank()) col.document(item.id) else col.document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "dateMillis" to item.dateMillis,
                "dateLabel" to item.dateLabel.take(40),
                "weightKg" to item.weightKg.coerceAtLeast(0.0),
                "pricePerKg" to item.pricePerKg.coerceAtLeast(0.0),
                "totalAmount" to item.totalAmount.coerceAtLeast(0.0),
                "buyerName" to item.buyerName.trim().take(120),
                "notes" to item.notes.trim().take(500),
                "trashed" to item.trashed,
                "trashedAtMillis" to item.trashedAtMillis,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(item.copy(id = docRef.id, userId = uid))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateHarvest(item: HarvestSale): Result<Unit> {
        val docPath = "users/$userId/harvest_sales/${item.id}"
        return try {
            val uid = requireActiveUid()
            require(item.id.isNotBlank())
            db.collection("users").document(uid).collection("harvest_sales").document(item.id)
                .update(
                    mapOf(
                        "dateMillis" to item.dateMillis,
                        "dateLabel" to item.dateLabel.take(40),
                        "weightKg" to item.weightKg.coerceAtLeast(0.0),
                        "pricePerKg" to item.pricePerKg.coerceAtLeast(0.0),
                        "totalAmount" to item.totalAmount.coerceAtLeast(0.0),
                        "buyerName" to item.buyerName.trim().take(120),
                        "notes" to item.notes.trim().take(500),
                        "trashed" to item.trashed,
                        "trashedAtMillis" to item.trashedAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun setHarvestTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit> =
        partialTrashUpdate("harvest_sales", id, trashed, trashedAtMillis)

    override suspend fun deleteHarvestPermanently(id: String): Result<Unit> =
        deleteDocument("harvest_sales", id)

    // --- Shared Helpers ---
    private suspend fun partialTrashUpdate(
        collectionName: String,
        id: String,
        trashed: Boolean,
        trashedAtMillis: Long
    ): Result<Unit> {
        val docPath = "users/$userId/$collectionName/$id"
        return try {
            val uid = requireActiveUid()
            require(id.isNotBlank())
            db.collection("users").document(uid).collection(collectionName).document(id)
                .update(
                    mapOf(
                        "trashed" to trashed,
                        "trashedAtMillis" to if (trashed) trashedAtMillis else 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    private suspend fun deleteDocument(collectionName: String, id: String): Result<Unit> {
        val docPath = "users/$userId/$collectionName/$id"
        return try {
            val uid = requireActiveUid()
            require(id.isNotBlank())
            db.collection("users").document(uid).collection(collectionName).document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun emptyTrash(): Result<Unit> {
        return try {
            val uid = requireActiveUid()
            val collections = listOf(
                "custom_feeding_plans",
                "fertilizer_purchases",
                "irrigation_logs",
                "spray_logs",
                "labor_logs",
                "harvest_sales"
            )
            val batch = db.batch()
            for (colName in collections) {
                val snap = db.collection("users").document(uid).collection(colName)
                    .whereEqualTo("trashed", true)
                    .get()
                    .await()
                for (doc in snap.documents) {
                    batch.delete(doc.reference)
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$userId/*")
            Result.failure(e)
        }
    }

    // --- Admin Operations ---
    override suspend fun saveAppConfig(config: AppConfig): Result<Unit> {
        val path = "app_config/settings"
        return try {
            requireSuperAdmin()
            val payload = mapOf(
                "appTitle" to config.appTitle.trim().take(100),
                "welcomeBanner" to config.welcomeBanner.trim().take(500),
                "dashboardSubtitle" to config.dashboardSubtitle.trim().take(200),
                "irrigationIntervalDays" to config.irrigationIntervalDays.coerceIn(1L, 60L),
                "workTypes" to config.workTypes.take(25),
                "fertilizerPresets" to config.fertilizerPresets.take(30),
                "sprayPresets" to config.sprayPresets.take(30),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.document(path).set(payload, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun saveFeedingRecipe(recipe: FeedingRecipe): Result<FeedingRecipe> {
        var path = "feeding_recipes"
        return try {
            requireSuperAdmin()
            val col = db.collection("feeding_recipes")
            val docRef = if (recipe.id.isNotBlank()) col.document(recipe.id) else col.document()
            path = docRef.path
            val payload = mapOf(
                "title" to recipe.title.trim().take(160),
                "category" to recipe.category.trim().take(80),
                "summary" to recipe.summary.trim().take(500),
                "dosage" to recipe.dosage.trim().take(300),
                "steps" to recipe.steps.filter { it.isNotBlank() }.take(25),
                "tip" to recipe.tip.trim().take(400),
                "orderIndex" to recipe.orderIndex.coerceIn(0L, 1000L),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload, SetOptions.merge()).await()
            Result.success(recipe.copy(id = docRef.id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun deleteFeedingRecipe(recipeId: String): Result<Unit> {
        val path = "feeding_recipes/$recipeId"
        return try {
            requireSuperAdmin()
            db.collection("feeding_recipes").document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    override suspend fun seedDefaultRecipesIfEmpty(): Result<Unit> {
        return try {
            requireSuperAdmin()
            val existing = db.collection("feeding_recipes").limit(1).get().await()
            if (existing.isEmpty) {
                for (recipe in DefaultFeedingRecipes.items) {
                    saveFeedingRecipe(recipe)
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "feeding_recipes")
            Result.failure(e)
        }
    }

    override suspend fun saveArticle(article: FarmArticle): Result<FarmArticle> {
        var path = "articles"
        return try {
            requireSuperAdmin()
            val col = db.collection("articles")
            val docRef = if (article.id.isNotBlank()) col.document(article.id) else col.document()
            path = docRef.path
            val snap = if (article.id.isNotBlank()) docRef.get().await() else null
            if (snap != null && snap.exists()) {
                val payload = mapOf(
                    "title" to article.title.trim().take(200),
                    "category" to article.category.trim().take(80),
                    "summary" to article.summary.trim().take(500),
                    "body" to article.body.trim().take(5000),
                    "publishedDateLabel" to article.publishedDateLabel.trim().take(60),
                    "orderIndex" to article.orderIndex.coerceIn(0L, 2000L),
                    "createdAt" to (snap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val payload = mapOf(
                    "title" to article.title.trim().take(200),
                    "category" to article.category.trim().take(80),
                    "summary" to article.summary.trim().take(500),
                    "body" to article.body.trim().take(5000),
                    "publishedDateLabel" to article.publishedDateLabel.trim().take(60),
                    "orderIndex" to article.orderIndex.coerceIn(0L, 2000L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            Result.success(article.copy(id = docRef.id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun deleteArticle(articleId: String): Result<Unit> {
        val path = "articles/$articleId"
        return try {
            requireSuperAdmin()
            db.collection("articles").document(articleId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    override suspend fun seedDefaultArticlesIfEmpty(): Result<Unit> {
        return try {
            requireSuperAdmin()
            val existing = db.collection("articles").limit(1).get().await()
            if (existing.isEmpty) {
                for (article in DefaultFarmArticles.items) {
                    saveArticle(article)
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "articles")
            Result.failure(e)
        }
    }

    override suspend fun fetchRegisteredUsers(): Result<List<UserProfile>> {
        val path = "users"
        return try {
            requireSuperAdmin()
            val snap = db.collection("users").get().await()
            val users = snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            Result.success(users)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }
}
