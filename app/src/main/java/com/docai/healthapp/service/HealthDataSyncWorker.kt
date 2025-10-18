package com.docai.healthapp.service

import android.content.Context
import androidx.work.*
import com.docai.healthapp.data.HealthDataRepository
import com.docai.healthapp.data.HealthDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.concurrent.TimeUnit

class HealthDataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val repository = HealthDataRepository(
        HealthDatabase.getDatabase(context).healthDataDao()
    )
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            if (account == null) {
                return@withContext Result.retry()
            }
            
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.HOURS.toMillis(1) // Last hour data
            
            val request = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_HEART_RATE_BPM)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val historyClient = Fitness.getHistoryClient(applicationContext, account)
            val response = historyClient.readData(request).await()
            
            var steps = 0
            var heartRate = 0f
            var calories = 0f
            var distance = 0f
            
            for (dataSet in response.dataSets) {
                when (dataSet.dataType) {
                    DataType.TYPE_STEP_COUNT_DELTA -> {
                        for (dataPoint in dataSet.dataPoints) {
                            steps += dataPoint.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
                        }
                    }
                    DataType.TYPE_HEART_RATE_BPM -> {
                        var heartRateSum = 0f
                        var heartRateCount = 0
                        for (dataPoint in dataSet.dataPoints) {
                            heartRateSum += dataPoint.getValue(DataType.TYPE_HEART_RATE_BPM.fields[0]).asFloat()
                            heartRateCount++
                        }
                        if (heartRateCount > 0) {
                            heartRate = heartRateSum / heartRateCount
                        }
                    }
                    DataType.TYPE_CALORIES_EXPENDED -> {
                        for (dataPoint in dataSet.dataPoints) {
                            calories += dataPoint.getValue(DataType.TYPE_CALORIES_EXPENDED.fields[0]).asFloat()
                        }
                    }
                    DataType.TYPE_DISTANCE_DELTA -> {
                        for (dataPoint in dataSet.dataPoints) {
                            distance += dataPoint.getValue(DataType.TYPE_DISTANCE_DELTA.fields[0]).asFloat()
                        }
                    }
                }
            }
            
            // Update database with new data
            repository.insertOrUpdateHealthData(steps, heartRate, calories, distance)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
                }
            }
        }
    }
    
    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<HealthDataSyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "health_data_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
