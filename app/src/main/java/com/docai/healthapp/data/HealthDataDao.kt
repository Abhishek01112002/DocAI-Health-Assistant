package com.docai.healthapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDataDao {
    
    @Query("SELECT * FROM health_data WHERE date = :date")
    suspend fun getHealthDataByDate(date: String): HealthData?
    
    @Query("SELECT * FROM health_data WHERE date = :date")
    fun getHealthDataByDateFlow(date: String): Flow<HealthData?>
    
    @Query("SELECT * FROM health_data ORDER BY date DESC LIMIT 1")
    suspend fun getLatestHealthData(): HealthData?
    
    @Query("SELECT * FROM health_data ORDER BY date DESC LIMIT 7")
    suspend fun getLastWeekHealthData(): List<HealthData>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthData(healthData: HealthData)
    
    @Update
    suspend fun updateHealthData(healthData: HealthData)
    
    @Delete
    suspend fun deleteHealthData(healthData: HealthData)
    
    @Query("DELETE FROM health_data WHERE date < :date")
    suspend fun deleteOldData(date: String)
}
