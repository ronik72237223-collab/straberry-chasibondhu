package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM local_fertilizer_purchases ORDER BY dateMillis DESC")
    fun observeFertilizers(): Flow<List<LocalFertilizerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFertilizer(item: LocalFertilizerEntity)

    @Query("DELETE FROM local_fertilizer_purchases WHERE id = :id")
    suspend fun deleteFertilizer(id: String)

    @Query("DELETE FROM local_fertilizer_purchases WHERE trashed = 1")
    suspend fun emptyTrashedFertilizers()

    @Query("SELECT * FROM local_irrigation_logs ORDER BY dateMillis DESC")
    fun observeIrrigations(): Flow<List<LocalIrrigationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIrrigation(item: LocalIrrigationEntity)

    @Query("DELETE FROM local_irrigation_logs WHERE id = :id")
    suspend fun deleteIrrigation(id: String)

    @Query("DELETE FROM local_irrigation_logs WHERE trashed = 1")
    suspend fun emptyTrashedIrrigations()

    @Query("SELECT * FROM local_spray_logs ORDER BY dateMillis DESC")
    fun observeSprays(): Flow<List<LocalSprayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpray(item: LocalSprayEntity)

    @Query("DELETE FROM local_spray_logs WHERE id = :id")
    suspend fun deleteSpray(id: String)

    @Query("DELETE FROM local_spray_logs WHERE trashed = 1")
    suspend fun emptyTrashedSprays()

    @Query("SELECT * FROM local_labor_logs ORDER BY dateMillis DESC")
    fun observeLabors(): Flow<List<LocalLaborEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLabor(item: LocalLaborEntity)

    @Query("DELETE FROM local_labor_logs WHERE id = :id")
    suspend fun deleteLabor(id: String)

    @Query("DELETE FROM local_labor_logs WHERE trashed = 1")
    suspend fun emptyTrashedLabors()

    @Query("SELECT * FROM local_harvest_sales ORDER BY dateMillis DESC")
    fun observeHarvests(): Flow<List<LocalHarvestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHarvest(item: LocalHarvestEntity)

    @Query("DELETE FROM local_harvest_sales WHERE id = :id")
    suspend fun deleteHarvest(id: String)

    @Query("DELETE FROM local_harvest_sales WHERE trashed = 1")
    suspend fun emptyTrashedHarvests()
}
