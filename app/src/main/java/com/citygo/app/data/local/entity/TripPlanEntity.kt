package com.citygo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_plans")
data class TripPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromCity: String,
    val toCity: String,
    val travellers: Int,
    val nights: Int,
    val travelMode: String,          // METRO | GOVT_BUS | PRIVATE_BUS | OWN_VEHICLE
    val roomType: String,            // BUDGET | MID | PREMIUM
    val roomCount: Int,
    val dailyFoodBudget: Double,
    val returnTrip: Boolean,
    val fuelCostPerKm: Double,       // only for OWN_VEHICLE
    val distanceKm: Double,          // fetched from OSRM
    val travelCost: Double,
    val stayCost: Double,
    val foodCost: Double,
    val totalCost: Double,
    val perPersonCost: Double,
    val savedAt: Long = System.currentTimeMillis()
)
