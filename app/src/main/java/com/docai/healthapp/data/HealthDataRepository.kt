package com.docai.healthapp.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class HealthDataRepository(private val healthDataDao: HealthDataDao) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun getTodayHealthData(): Flow<HealthData?> {
        val today = dateFormat.format(Date())
        return healthDataDao.getHealthDataByDateFlow(today)
    }
    
    suspend fun getTodayHealthDataSync(): HealthData? {
        val today = dateFormat.format(Date())
        return healthDataDao.getHealthDataByDate(today)
    }
    
    suspend fun insertOrUpdateHealthData(
        steps: Int = 0,
        heartRate: Float = 0f,
        calories: Float = 0f,
        distance: Float = 0f
    ) {
        val today = dateFormat.format(Date())
        val existingData = healthDataDao.getHealthDataByDate(today)
        
        val healthData = if (existingData != null) {
            existingData.copy(
                steps = if (steps > 0) steps else existingData.steps,
                heartRate = if (heartRate > 0) heartRate else existingData.heartRate,
                calories = if (calories > 0) calories else existingData.calories,
                distance = if (distance > 0) distance else existingData.distance,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            HealthData(
                id = today,
                date = today,
                steps = steps,
                heartRate = heartRate,
                calories = calories,
                distance = distance,
                lastUpdated = System.currentTimeMillis()
            )
        }
        
        healthDataDao.insertHealthData(healthData)
    }
    
    suspend fun getLastWeekHealthData(): List<HealthData> {
        return healthDataDao.getLastWeekHealthData()
    }
    
    suspend fun cleanupOldData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -30) // Keep only last 30 days
        val cutoffDate = dateFormat.format(calendar.time)
        healthDataDao.deleteOldData(cutoffDate)
    }
}
