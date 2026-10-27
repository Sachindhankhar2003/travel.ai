package com.citygo.app.data.local.dao

import androidx.room.*
import com.citygo.app.data.local.entity.TripPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripPlanDao {
    @Query("SELECT * FROM trip_plans ORDER BY savedAt DESC")
    fun getAllPlans(): Flow<List<TripPlanEntity>>

    @Query("SELECT * FROM trip_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: Long): TripPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(entity: TripPlanEntity): Long

    @Update
    suspend fun updatePlan(entity: TripPlanEntity)

    @Delete
    suspend fun deletePlan(entity: TripPlanEntity)

    @Query("DELETE FROM trip_plans WHERE id = :id")
    suspend fun deletePlanById(id: Long)
}
