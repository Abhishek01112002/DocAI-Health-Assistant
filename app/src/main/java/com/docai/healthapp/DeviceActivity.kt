package com.docai.healthapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.concurrent.TimeUnit
import com.docai.healthapp.data.HealthDataRepository
import com.docai.healthapp.data.HealthDatabase
import com.docai.healthapp.service.HealthDataSyncWorker

class DeviceActivity : AppCompatActivity() {
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fitnessOptions: FitnessOptions
    private var isGoogleFitConnected = false
    private lateinit var healthDataRepository: HealthDataRepository
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Activity recognition permission granted", Toast.LENGTH_SHORT).show()
            connectToGoogleFit()
        } else {
            Toast.makeText(this, "Activity recognition permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Google Fit authentication launcher
    private val googleFitAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleFitAuthResult(account)
        } else {
            Toast.makeText(this, "Google Fit authentication failed", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        
        // Initialize database repository
        healthDataRepository = HealthDataRepository(
            HealthDatabase.getDatabase(this).healthDataDao()
        )
        
        setupGoogleFit()
        setupUI()
    }
    
    private fun setupGoogleFit() {
        // Configure Google Sign-In options for Google Fit
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Configure Fitness options
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .build()
    }
    
    private fun setupUI() {
        findViewById<Button>(R.id.btnConnectGoogleFit).setOnClickListener {
            checkPermissionsAndConnect()
        }
        
        findViewById<Button>(R.id.btnFetchSteps).setOnClickListener {
            if (isGoogleFitConnected) {
                fetchStepCount()
            } else {
                Toast.makeText(this, "Please connect to Google Fit first", Toast.LENGTH_SHORT).show()
            }
        }
        
        findViewById<Button>(R.id.btnFetchHeartRate).setOnClickListener {
            if (isGoogleFitConnected) {
                fetchHeartRate()
            } else {
                Toast.makeText(this, "Please connect to Google Fit first", Toast.LENGTH_SHORT).show()
            }
        }
        
        findViewById<Button>(R.id.btnFetchCalories).setOnClickListener {
            if (isGoogleFitConnected) {
                fetchCalories()
            } else {
                Toast.makeText(this, "Please connect to Google Fit first", Toast.LENGTH_SHORT).show()
            }
        }
        
        findViewById<Button>(R.id.btnFetchDistance).setOnClickListener {
            if (isGoogleFitConnected) {
                fetchDistance()
            } else {
                Toast.makeText(this, "Please connect to Google Fit first", Toast.LENGTH_SHORT).show()
            }
        }
        
        findViewById<Button>(R.id.btnFetchAllData).setOnClickListener {
            if (isGoogleFitConnected) {
                fetchAllHealthData()
            } else {
                Toast.makeText(this, "Please connect to Google Fit first", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Setup footer navigation
        val footerNavigation = findViewById<LinearLayout>(R.id.footerNavigation)
        
        // Home tab
        footerNavigation.getChildAt(0).setOnClickListener {
            val intent = android.content.Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        
        // Consultancy tab
        footerNavigation.getChildAt(1).setOnClickListener {
            val intent = android.content.Intent(this, ConsultancyActivity::class.java)
            startActivity(intent)
        }
        
        // Diet Plan tab
        footerNavigation.getChildAt(3).setOnClickListener {
            val intent = android.content.Intent(this, DietPlanActivity::class.java)
            startActivity(intent)
        }
        
        // Device tab is already active, no need to set click listener
    }
    
    private fun checkPermissionsAndConnect() {
        // Check for ACTIVITY_RECOGNITION permission (Android 10+)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            connectToGoogleFit()
        }
    }
    
    private fun connectToGoogleFit() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            // Already signed in
            isGoogleFitConnected = true
            Toast.makeText(this, "Google Fit connected successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Need to sign in
            val signInIntent = googleSignInClient.signInIntent
            googleFitAuthLauncher.launch(signInIntent)
        }
    }
    
    private fun handleGoogleFitAuthResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                // Successfully signed in
                isGoogleFitConnected = true
                Toast.makeText(this, "Google Fit authentication successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Authentication failed: No account returned", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("GoogleFitAuth", "Authentication error", e)
        }
    }
    
    private fun fetchStepCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(1)
                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val historyClient = Fitness.getHistoryClient(this@DeviceActivity, GoogleSignIn.getLastSignedInAccount(this@DeviceActivity)!!)
                val response = historyClient.readData(request).await()
                
                var totalSteps = 0
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        totalSteps += dp.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
                    }
                }
                
                // Store data in database
                healthDataRepository.insertOrUpdateHealthData(steps = totalSteps)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Steps today: $totalSteps", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Error fetching steps: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun fetchHeartRate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(1)
                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_HEART_RATE_BPM)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val historyClient = Fitness.getHistoryClient(this@DeviceActivity, GoogleSignIn.getLastSignedInAccount(this@DeviceActivity)!!)
                val response = historyClient.readData(request).await()
                
                var avgHeartRate = 0f
                if (response.buckets.isNotEmpty()) {
                    val dataSet = response.buckets[0].getDataSet(DataType.AGGREGATE_HEART_RATE_SUMMARY)
                    if (dataSet?.dataPoints?.isNotEmpty() == true) {
                        avgHeartRate = dataSet.dataPoints[0].getValue(DataType.TYPE_HEART_RATE_BPM.fields[0]).asFloat()
                    }
                }
                
                // Store data in database
                if (avgHeartRate > 0) {
                    healthDataRepository.insertOrUpdateHealthData(heartRate = avgHeartRate)
                }
                
                withContext(Dispatchers.Main) {
                    if (avgHeartRate > 0) {
                        Toast.makeText(this@DeviceActivity, "Average Heart Rate: ${avgHeartRate.toInt()} BPM", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@DeviceActivity, "No heart rate data available", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Error fetching heart rate: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun fetchCalories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(1)
                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val historyClient = Fitness.getHistoryClient(this@DeviceActivity, GoogleSignIn.getLastSignedInAccount(this@DeviceActivity)!!)
                val response = historyClient.readData(request).await()
                
                var totalCalories = 0f
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        totalCalories += dp.getValue(DataType.TYPE_CALORIES_EXPENDED.fields[0]).asFloat()
                    }
                }
                
                // Store data in database
                healthDataRepository.insertOrUpdateHealthData(calories = totalCalories)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Calories burned today: ${totalCalories.toInt()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Error fetching calories: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun fetchDistance() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(1)
                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val historyClient = Fitness.getHistoryClient(this@DeviceActivity, GoogleSignIn.getLastSignedInAccount(this@DeviceActivity)!!)
                val response = historyClient.readData(request).await()
                
                var totalDistance = 0f
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        totalDistance += dp.getValue(DataType.TYPE_DISTANCE_DELTA.fields[0]).asFloat()
                    }
                }
                
                // Store data in database
                healthDataRepository.insertOrUpdateHealthData(distance = totalDistance)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Distance today: ${String.format("%.2f", totalDistance / 1000)} km", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Error fetching distance: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun fetchAllHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(1)
                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_HEART_RATE_BPM)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build()
                
                val historyClient = Fitness.getHistoryClient(this@DeviceActivity, GoogleSignIn.getLastSignedInAccount(this@DeviceActivity)!!)
                val response = historyClient.readData(request).await()
                
                var totalSteps = 0
                var heartRateSum = 0f
                var heartRateCount = 0
                var totalCalories = 0f
                var totalDistance = 0f
                
                for (dataSet in response.dataSets) {
                    when (dataSet.dataType) {
                        DataType.TYPE_STEP_COUNT_DELTA -> {
                            for (dataPoint in dataSet.dataPoints) {
                                totalSteps += dataPoint.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
                            }
                        }
                        DataType.TYPE_HEART_RATE_BPM -> {
                            for (dataPoint in dataSet.dataPoints) {
                                heartRateSum += dataPoint.getValue(DataType.TYPE_HEART_RATE_BPM.fields[0]).asFloat()
                                heartRateCount++
                            }
                        }
                        DataType.TYPE_CALORIES_EXPENDED -> {
                            for (dataPoint in dataSet.dataPoints) {
                                totalCalories += dataPoint.getValue(DataType.TYPE_CALORIES_EXPENDED.fields[0]).asFloat()
                            }
                        }
                        DataType.TYPE_DISTANCE_DELTA -> {
                            for (dataPoint in dataSet.dataPoints) {
                                totalDistance += dataPoint.getValue(DataType.TYPE_DISTANCE_DELTA.fields[0]).asFloat()
                            }
                        }
                    }
                }
                
                // Store all data in database
                val averageHeartRate = if (heartRateCount > 0) heartRateSum / heartRateCount else 0f
                healthDataRepository.insertOrUpdateHealthData(
                    steps = totalSteps,
                    heartRate = averageHeartRate,
                    calories = totalCalories,
                    distance = totalDistance
                )
                
                // Start background sync for 24/7 updates
                HealthDataSyncWorker.schedulePeriodicSync(this@DeviceActivity)
                
                withContext(Dispatchers.Main) {
                    val summary = """
                        Health Summary Today:
                        Steps: $totalSteps
                        Heart Rate: ${averageHeartRate.toInt()} BPM
                        Calories: ${totalCalories.toInt()}
                        Distance: ${String.format("%.2f", totalDistance / 1000)} km
                    """.trimIndent()
                    Toast.makeText(this@DeviceActivity, summary, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeviceActivity, "Error fetching health data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Extension function to convert Task to suspend function
    private suspend fun <T> Task<T>.await(): T {
        return suspendCancellableCoroutine { continuation: CancellableContinuation<T> ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result)
                } else {
                    continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
                }
            }
        }
    }
}
