package com.docai.healthapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
import com.google.android.gms.fitness.data.Field
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fitnessOptions: FitnessOptions
    private var isGoogleFitConnected = false

    // Permission request launcher for Android 10+ Activity Recognition
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Activity recognition permission granted", Toast.LENGTH_SHORT).show()
            requestFitPermissions() // Proceed to Fit permissions
        } else {
            Toast.makeText(this, "Activity recognition permission is required to fetch steps.", Toast.LENGTH_LONG).show()
        }
    }

    // Google Fit authentication launcher
    private val googleFitAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleFitAuthResult(task)
        } else {
            Toast.makeText(this, "Google Fit authentication was cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupGoogleFit()
        setupUI()
    }

    private fun setupGoogleFit() {
        // 1. Define the fitness options (what data you want to read)
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        // 2. Build Google Sign-In options and add the Fitness options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .addExtension(fitnessOptions) // Crucial: This requests OAuth consent for Fit data
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btnConnectGoogleFit).setOnClickListener {
            checkPermissionsAndConnect()
        }

        findViewById<Button>(R.id.btnFetchSteps).setOnClickListener {
            if (isGoogleFitConnected) fetchStepCount() else showNotConnectedToast()
        }

        findViewById<Button>(R.id.btnFetchHeartRate).setOnClickListener {
            if (isGoogleFitConnected) fetchHeartRate() else showNotConnectedToast()
        }

        findViewById<Button>(R.id.btnFetchCalories).setOnClickListener {
            if (isGoogleFitConnected) fetchCalories() else showNotConnectedToast()
        }

        findViewById<Button>(R.id.btnFetchDistance).setOnClickListener {
            if (isGoogleFitConnected) fetchDistance() else showNotConnectedToast()
        }

        findViewById<Button>(R.id.btnFetchAllData).setOnClickListener {
            if (isGoogleFitConnected) fetchAllHealthData() else showNotConnectedToast()
        }
    }

    private fun showNotConnectedToast() {
        Toast.makeText(this, "Please connect to Google Fit first", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionsAndConnect() {
        // First, check for the ACTIVITY_RECOGNITION permission on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission. The result will be handled by the launcher.
            requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            // If permission is already granted or not required, proceed to Fit permissions.
            requestFitPermissions()
        }
    }

    private fun requestFitPermissions() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        // Check if user is signed in AND has the required Fitness permissions.
        if (account != null && GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            // Permissions are already granted. We are ready to fetch data.
            isGoogleFitConnected = true
            Toast.makeText(this, "Google Fit connected successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Launch the Google Sign-In flow. This will ask the user to select an account
            // and grant the permissions defined in `fitnessOptions`.
            val signInIntent = googleSignInClient.signInIntent
            googleFitAuthLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleFitAuthResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            // Check if the sign-in was successful AND the necessary permissions were granted.
            if (account != null && GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                isGoogleFitConnected = true
                Toast.makeText(this, "Google Fit authentication successful!", Toast.LENGTH_SHORT).show()
            } else {
                isGoogleFitConnected = false
                Toast.makeText(this, "Permissions not granted. Please try connecting again.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            isGoogleFitConnected = false
            Toast.makeText(this, "Authentication failed: ${e.message} (Code: ${e.statusCode})", Toast.LENGTH_LONG).show()
            android.util.Log.e("GoogleFitAuth", "Authentication error", e)
        }
    }

    // --- Data Fetching Functions (No changes needed below this line) ---

    private fun fetchStepCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)!!
                val historyClient = Fitness.getHistoryClient(this@MainActivity, account)
                
                val end = System.currentTimeMillis()
                val start = end - TimeUnit.DAYS.toMillis(1)

                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                    .build()

                val response = historyClient.readData(request).await()
                var totalSteps = 0
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        totalSteps += dp.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Steps today: $totalSteps", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching steps: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchHeartRate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)!!
                val historyClient = Fitness.getHistoryClient(this@MainActivity, account)

                val end = System.currentTimeMillis()
                val start = end - TimeUnit.DAYS.toMillis(1)

                val request = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_HEART_RATE_BPM)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                    .build()

                val response = historyClient.readData(request).await()
                var avgHeartRate = 0f
                if (response.buckets.isNotEmpty()) {
                    val dataSet = response.buckets[0].getDataSet(DataType.AGGREGATE_HEART_RATE_SUMMARY)
                    if (dataSet?.dataPoints?.isNotEmpty() == true) {
                        avgHeartRate = dataSet.dataPoints[0].getValue(DataType.TYPE_HEART_RATE_BPM.fields[0]).asFloat()
                    }
                }
                withContext(Dispatchers.Main) {
                    if (avgHeartRate > 0) {
                        Toast.makeText(this@MainActivity, "Average Heart Rate: ${avgHeartRate.toInt()} BPM", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "No heart rate data available", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching heart rate: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchCalories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)!!
                val historyClient = Fitness.getHistoryClient(this@MainActivity, account)

                val end = System.currentTimeMillis()
                val start = end - TimeUnit.DAYS.toMillis(1)

                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                    .build()

                val response = historyClient.readData(request).await()
                var totalCalories = 0f
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        totalCalories += dp.getValue(DataType.TYPE_CALORIES_EXPENDED.fields[0]).asFloat()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Calories burned today: ${totalCalories.toInt()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching calories: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchDistance() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(this@MainActivity)!!
                val historyClient = Fitness.getHistoryClient(this@MainActivity, account)

                val end = System.currentTimeMillis()
                val start = end - TimeUnit.DAYS.toMillis(1)

                val request = DataReadRequest.Builder()
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                    .build()

                val response = historyClient.readData(request).await()
                var totalDistance = 0f
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        totalDistance += dp.getValue(DataType.TYPE_DISTANCE_DELTA.fields[0]).asFloat()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Distance today: ${String.format("%.2f", totalDistance / 1000)} km", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error fetching distance: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun fetchAllHealthData() {
        // This function can now just call the individual fetch functions
        fetchStepCount()
        fetchHeartRate()
        fetchCalories()
        fetchDistance()
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
