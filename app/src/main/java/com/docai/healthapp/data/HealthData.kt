package com.docai.healthapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey
    val id: String,
    val date: String, // YYYY-MM-DD format
    val steps: Int = 0,
    val heartRate: Float = 0f,
    val calories: Float = 0f,
    val distance: Float = 0f, // in meters
    val lastUpdated: Long = System.currentTimeMillis()
)
