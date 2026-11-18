package com.example.data.repository

import com.example.model.AppConfig
import com.example.model.CustomFeedingPlan
import com.example.model.FarmArticle
import com.example.model.FeedingRecipe
import com.example.model.FertilizerPurchase
import com.example.model.HarvestSale
import com.example.model.IrrigationLog
import com.example.model.LaborLog
import com.example.model.SprayLog
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    fun observeAppConfig(): Flow<AppConfig>
    fun observeFeedingRecipes(): Flow<List<FeedingRecipe>>
    fun observeArticles(): Flow<List<FarmArticle>>

    fun observeCustomFeedingPlans(): Flow<List<CustomFeedingPlan>>
    fun observeFertilizers(): Flow<List<FertilizerPurchase>>
    fun observeIrrigations(): Flow<List<IrrigationLog>>
    fun observeSprays(): Flow<List<SprayLog>>
    fun observeLabors(): Flow<List<LaborLog>>
    fun observeHarvests(): Flow<List<HarvestSale>>

    // Custom Feeding Plan CRUD
    suspend fun addCustomFeedingPlan(item: CustomFeedingPlan): Result<CustomFeedingPlan>
    suspend fun updateCustomFeedingPlan(item: CustomFeedingPlan): Result<Unit>
    suspend fun setCustomFeedingPlanTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit>
    suspend fun deleteCustomFeedingPlanPermanently(id: String): Result<Unit>

    // Fertilizer CRUD
    suspend fun addFertilizer(item: FertilizerPurchase): Result<FertilizerPurchase>
    suspend fun updateFertilizer(item: FertilizerPurchase): Result<Unit>
    suspend fun setFertilizerTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit>
    suspend fun setFertilizerDue(id: String, due: Boolean): Result<Unit>
    suspend fun deleteFertilizerPermanently(id: String): Result<Unit>

    // Irrigation CRUD
    suspend fun addIrrigation(item: IrrigationLog): Result<IrrigationLog>
    suspend fun updateIrrigation(item: IrrigationLog): Result<Unit>
    suspend fun setIrrigationTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit>
    suspend fun deleteIrrigationPermanently(id: String): Result<Unit>

    // Spray CRUD
    suspend fun addSpray(item: SprayLog): Result<SprayLog>
    suspend fun updateSpray(item: SprayLog): Result<Unit>
    suspend fun setSprayTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit>
    suspend fun deleteSprayPermanently(id: String): Result<Unit>

    // Labor CRUD
    suspend fun addLabor(item: LaborLog): Result<LaborLog>
    suspend fun updateLabor(item: LaborLog): Result<Unit>
    suspend fun setLaborTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit>
    suspend fun deleteLaborPermanently(id: String): Result<Unit>

    // Harvest CRUD
    suspend fun addHarvest(item: HarvestSale): Result<HarvestSale>
    suspend fun updateHarvest(item: HarvestSale): Result<Unit>
    suspend fun setHarvestTrashed(id: String, trashed: Boolean, trashedAtMillis: Long): Result<Unit>
    suspend fun deleteHarvestPermanently(id: String): Result<Unit>

    // Trash cleanup
    suspend fun emptyTrash(): Result<Unit>

    // Admin operations
    suspend fun saveAppConfig(config: AppConfig): Result<Unit>
    suspend fun saveFeedingRecipe(recipe: FeedingRecipe): Result<FeedingRecipe>
    suspend fun deleteFeedingRecipe(recipeId: String): Result<Unit>
    suspend fun seedDefaultRecipesIfEmpty(): Result<Unit>
    suspend fun saveArticle(article: FarmArticle): Result<FarmArticle>
    suspend fun deleteArticle(articleId: String): Result<Unit>
    suspend fun seedDefaultArticlesIfEmpty(): Result<Unit>
    suspend fun fetchRegisteredUsers(): Result<List<UserProfile>>
}
