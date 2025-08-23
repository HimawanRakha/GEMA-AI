package com.example.stecu.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerPlan(plan: CareerPlanEntity): Long

    @Query("SELECT * FROM career_plans WHERE id = :id")
    suspend fun getCareerPlanById(id: Long): CareerPlanEntity?

    @Query("SELECT * FROM career_plans ORDER BY id DESC")
    fun getAllCareerPlans(): Flow<List<CareerPlanEntity>>

    @Query("DELETE FROM career_plans WHERE id = :planId")
    suspend fun deleteCareerPlanById(planId: Long)
}