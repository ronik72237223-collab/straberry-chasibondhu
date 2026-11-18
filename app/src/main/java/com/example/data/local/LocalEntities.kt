package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CustomFeedingPlan
import com.example.model.FertilizerPurchase
import com.example.model.GUEST_USER_ID
import com.example.model.HarvestSale
import com.example.model.IrrigationLog
import com.example.model.LaborLog
import com.example.model.SprayLog

@Entity(tableName = "local_custom_feeding_plans")
data class LocalCustomFeedingPlanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val method: String,
    val dosage: String,
    val dateMillis: Long,
    val dateLabel: String,
    val nextDateMillis: Long,
    val nextDateLabel: String,
    val notes: String,
    val trashed: Boolean,
    val trashedAtMillis: Long
) {
    fun toModel() = CustomFeedingPlan(
        id = id,
        userId = GUEST_USER_ID,
        title = title,
        method = method,
        dosage = dosage,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        nextDateMillis = nextDateMillis,
        nextDateLabel = nextDateLabel,
        notes = notes,
        trashed = trashed,
        trashedAtMillis = trashedAtMillis
    )
}

fun CustomFeedingPlan.toLocalEntity() = LocalCustomFeedingPlanEntity(
    id = id,
    title = title,
    method = method,
    dosage = dosage,
    dateMillis = dateMillis,
    dateLabel = dateLabel,
    nextDateMillis = nextDateMillis,
    nextDateLabel = nextDateLabel,
    notes = notes,
    trashed = trashed,
    trashedAtMillis = trashedAtMillis
)

@Entity(tableName = "local_fertilizer_purchases")
data class LocalFertilizerEntity(
    @PrimaryKey val id: String,
    val dateMillis: Long,
    val dateLabel: String,
    val fertilizerName: String,
    val shopName: String,
    val quantity: String,
    val totalPrice: Double,
    val due: Boolean,
    val notes: String,
    val trashed: Boolean,
    val trashedAtMillis: Long
) {
    fun toModel() = FertilizerPurchase(
        id = id,
        userId = GUEST_USER_ID,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        fertilizerName = fertilizerName,
        shopName = shopName,
        quantity = quantity,
        totalPrice = totalPrice,
        due = due,
        notes = notes,
        trashed = trashed,
        trashedAtMillis = trashedAtMillis
    )
}

fun FertilizerPurchase.toLocalEntity() = LocalFertilizerEntity(
    id = id,
    dateMillis = dateMillis,
    dateLabel = dateLabel,
    fertilizerName = fertilizerName,
    shopName = shopName,
    quantity = quantity,
    totalPrice = totalPrice,
    due = due,
    notes = notes,
    trashed = trashed,
    trashedAtMillis = trashedAtMillis
)

@Entity(tableName = "local_irrigation_logs")
data class LocalIrrigationEntity(
    @PrimaryKey val id: String,
    val dateMillis: Long,
    val dateLabel: String,
    val plotName: String,
    val cost: Double,
    val nextIrrigationMillis: Long,
    val nextIrrigationLabel: String,
    val notes: String,
    val trashed: Boolean,
    val trashedAtMillis: Long
) {
    fun toModel() = IrrigationLog(
        id = id,
        userId = GUEST_USER_ID,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        plotName = plotName,
        cost = cost,
        nextIrrigationMillis = nextIrrigationMillis,
        nextIrrigationLabel = nextIrrigationLabel,
        notes = notes,
        trashed = trashed,
        trashedAtMillis = trashedAtMillis
    )
}

fun IrrigationLog.toLocalEntity() = LocalIrrigationEntity(
    id = id,
    dateMillis = dateMillis,
    dateLabel = dateLabel,
    plotName = plotName,
    cost = cost,
    nextIrrigationMillis = nextIrrigationMillis,
    nextIrrigationLabel = nextIrrigationLabel,
    notes = notes,
    trashed = trashed,
    trashedAtMillis = trashedAtMillis
)

@Entity(tableName = "local_spray_logs")
data class LocalSprayEntity(
    @PrimaryKey val id: String,
    val dateMillis: Long,
    val dateLabel: String,
    val medicineName: String,
    val dose: String,
    val cost: Double,
    val nextSprayMillis: Long = 0L,
    val nextSprayLabel: String = "",
    val notes: String,
    val trashed: Boolean,
    val trashedAtMillis: Long
) {
    fun toModel() = SprayLog(
        id = id,
        userId = GUEST_USER_ID,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        medicineName = medicineName,
        dose = dose,
        cost = cost,
        nextSprayMillis = nextSprayMillis,
        nextSprayLabel = nextSprayLabel,
        notes = notes,
        trashed = trashed,
        trashedAtMillis = trashedAtMillis
    )
}

fun SprayLog.toLocalEntity() = LocalSprayEntity(
    id = id,
    dateMillis = dateMillis,
    dateLabel = dateLabel,
    medicineName = medicineName,
    dose = dose,
    cost = cost,
    nextSprayMillis = nextSprayMillis,
    nextSprayLabel = nextSprayLabel,
    notes = notes,
    trashed = trashed,
    trashedAtMillis = trashedAtMillis
)

@Entity(tableName = "local_labor_logs")
data class LocalLaborEntity(
    @PrimaryKey val id: String,
    val dateMillis: Long,
    val dateLabel: String,
    val workerCount: Long,
    val dailyWage: Double,
    val totalCost: Double,
    val workType: String,
    val notes: String,
    val trashed: Boolean,
    val trashedAtMillis: Long
) {
    fun toModel() = LaborLog(
        id = id,
        userId = GUEST_USER_ID,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        workerCount = workerCount,
        dailyWage = dailyWage,
        totalCost = totalCost,
        workType = workType,
        notes = notes,
        trashed = trashed,
        trashedAtMillis = trashedAtMillis
    )
}

fun LaborLog.toLocalEntity() = LocalLaborEntity(
    id = id,
    dateMillis = dateMillis,
    dateLabel = dateLabel,
    workerCount = workerCount,
    dailyWage = dailyWage,
    totalCost = totalCost,
    workType = workType,
    notes = notes,
    trashed = trashed,
    trashedAtMillis = trashedAtMillis
)

@Entity(tableName = "local_harvest_sales")
data class LocalHarvestEntity(
    @PrimaryKey val id: String,
    val dateMillis: Long,
    val dateLabel: String,
    val weightKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val buyerName: String,
    val notes: String,
    val trashed: Boolean,
    val trashedAtMillis: Long
) {
    fun toModel() = HarvestSale(
        id = id,
        userId = GUEST_USER_ID,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        weightKg = weightKg,
        pricePerKg = pricePerKg,
        totalAmount = totalAmount,
        buyerName = buyerName,
        notes = notes,
        trashed = trashed,
        trashedAtMillis = trashedAtMillis
    )
}

fun HarvestSale.toLocalEntity() = LocalHarvestEntity(
    id = id,
    dateMillis = dateMillis,
    dateLabel = dateLabel,
    weightKg = weightKg,
    pricePerKg = pricePerKg,
    totalAmount = totalAmount,
    buyerName = buyerName,
    notes = notes,
    trashed = trashed,
    trashedAtMillis = trashedAtMillis
)
