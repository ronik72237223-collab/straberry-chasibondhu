package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalFertilizerEntity::class,
        LocalIrrigationEntity::class,
        LocalSprayEntity::class,
        LocalLaborEntity::class,
        LocalHarvestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FarmDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: FarmDatabase? = null

        fun getInstance(context: Context): FarmDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FarmDatabase::class.java,
                    "strawberry_chashi_bondhu.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
