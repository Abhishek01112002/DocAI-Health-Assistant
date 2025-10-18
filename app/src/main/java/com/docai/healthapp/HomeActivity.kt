package com.docai.healthapp

import android.os.Bundle
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.docai.healthapp.data.HealthDataRepository
import com.docai.healthapp.data.HealthDatabase

class HomeActivity : AppCompatActivity() {
    
    private lateinit var healthInsightsRecycler: RecyclerView
    private lateinit var greetingText: TextView
    private lateinit var dateText: TextView
    private lateinit var healthDataRepository: HealthDataRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        // Initialize database repository
        healthDataRepository = HealthDataRepository(
            HealthDatabase.getDatabase(this).healthDataDao()
        )
        
        setupUI()
        setupHealthInsights()
        updateGreeting()
    }
    
    private fun setupUI() {
        greetingText = findViewById(R.id.greetingText)
        dateText = findViewById(R.id.dateText)
        healthInsightsRecycler = findViewById(R.id.healthInsightsRecycler)
        
        // Setup footer navigation
        val footerNavigation = findViewById<LinearLayout>(R.id.footerNavigation)
        
        // Consultancy tab
        footerNavigation.getChildAt(1).setOnClickListener {
            val intent = android.content.Intent(this, ConsultancyActivity::class.java)
            startActivity(intent)
        }
        
        // Device tab
        footerNavigation.getChildAt(2).setOnClickListener {
            val intent = android.content.Intent(this, DeviceActivity::class.java)
            startActivity(intent)
        }
        
        // Diet Plan tab
        footerNavigation.getChildAt(3).setOnClickListener {
            val intent = android.content.Intent(this, DietPlanActivity::class.java)
            startActivity(intent)
        }
        
        // Home tab is already active, no need to set click listener
    }
    
    private fun setupHealthInsights() {
        // Observe real-time health data from database
        lifecycleScope.launch {
            healthDataRepository.getTodayHealthData().collect { healthData ->
                val insights = mutableListOf<HealthInsight>()
                
                // Steps card
                insights.add(
                    HealthInsight(
                        "Steps", 
                        "${healthData?.steps ?: 0}", 
                        R.drawable.ic_activity,
                        getLastUpdatedText(healthData?.lastUpdated)
                    )
                )
                
                // Heart Rate card
                insights.add(
                    HealthInsight(
                        "Heart Rate", 
                        "${(healthData?.heartRate ?: 0f).toInt()} BPM", 
                        R.drawable.ic_stress,
                        getLastUpdatedText(healthData?.lastUpdated)
                    )
                )
                
                // Calories card
                insights.add(
                    HealthInsight(
                        "Calories", 
                        "${(healthData?.calories ?: 0f).toInt()}", 
                        R.drawable.ic_hydration,
                        getLastUpdatedText(healthData?.lastUpdated)
                    )
                )
                
                // Distance card
                val distanceKm = (healthData?.distance ?: 0f) / 1000
                insights.add(
                    HealthInsight(
                        "Distance", 
                        "${String.format("%.2f", distanceKm)} km", 
                        R.drawable.ic_sleep,
                        getLastUpdatedText(healthData?.lastUpdated)
                    )
                )
                
                healthInsightsRecycler.layoutManager = GridLayoutManager(this@HomeActivity, 2)
                healthInsightsRecycler.adapter = HealthInsightAdapter(insights)
            }
        }
    }
    
    private fun getLastUpdatedText(lastUpdated: Long?): String {
        if (lastUpdated == null) return "No data"
        
        val currentTime = System.currentTimeMillis()
        val diffMinutes = (currentTime - lastUpdated) / (1000 * 60)
        
        return when {
            diffMinutes < 1 -> "Just now"
            diffMinutes < 60 -> "${diffMinutes}m ago"
            diffMinutes < 1440 -> "${diffMinutes / 60}h ago"
            else -> "${diffMinutes / 1440}d ago"
        }
    }
    
    private fun updateGreeting() {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..21 -> "Good Evening"
            else -> "Good Night"
        }
        
        dateText.text = dayFormat.format(calendar.time)
        greetingText.text = "$greeting, User 👋"
    }
}

data class HealthInsight(
    val title: String,
    val value: String,
    val iconRes: Int,
    val subtitle: String = ""
)

class HealthInsightAdapter(private val insights: List<HealthInsight>) : 
    RecyclerView.Adapter<HealthInsightAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.insightTitle)
        val valueText: TextView = itemView.findViewById(R.id.insightValue)
        val iconImage: ImageView = itemView.findViewById(R.id.insightIcon)
        val subtitleText: TextView = itemView.findViewById(R.id.insightSubtitle)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_insight, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val insight = insights[position]
        holder.titleText.text = insight.title
        holder.valueText.text = insight.value
        holder.iconImage.setImageResource(insight.iconRes)
        
        if (insight.subtitle.isNotEmpty()) {
            holder.subtitleText.text = insight.subtitle
            holder.subtitleText.visibility = View.VISIBLE
        } else {
            holder.subtitleText.visibility = View.GONE
        }
    }
    
    override fun getItemCount() = insights.size
}
