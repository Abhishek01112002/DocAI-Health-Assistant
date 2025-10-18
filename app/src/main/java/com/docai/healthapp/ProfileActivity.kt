package com.docai.healthapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var profileRecycler: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        setupUI()
        setupProfile()
    }
    
    private fun setupUI() {
        profileRecycler = findViewById(R.id.profileRecycler)
        
        // Navigation buttons
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = android.content.Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btnConsultancy).setOnClickListener {
            val intent = android.content.Intent(this, ConsultancyActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btnDevice).setOnClickListener {
            val intent = android.content.Intent(this, DeviceActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btnDietPlan).setOnClickListener {
            val intent = android.content.Intent(this, DietPlanActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupProfile() {
        // Profile settings
        val profileSettings = listOf(
            ProfileSetting("Avatar", "Change profile picture", ""),
            ProfileSetting("Nickname", "Please enter a nickname", ""),
            ProfileSetting("Sex", "male", ""),
            ProfileSetting("Age", "25", ""),
            ProfileSetting("Height", "170cm", ""),
            ProfileSetting("Weight", "65 kg", ""),
            ProfileSetting("Target step", "5000Step", ""),
            ProfileSetting("Weight unit", "kg", ""),
            ProfileSetting("Height unit", "cm", "")
        )
        
        profileRecycler.layoutManager = LinearLayoutManager(this)
        profileRecycler.adapter = ProfileAdapter(profileSettings)
    }
}

data class ProfileSetting(
    val title: String,
    val value: String,
    val description: String
)

class ProfileAdapter(private val settings: List<ProfileSetting>) : 
    RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingTitle: TextView = itemView.findViewById(R.id.settingTitle)
        val settingValue: TextView = itemView.findViewById(R.id.settingValue)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_setting, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val setting = settings[position]
        holder.settingTitle.text = setting.title
        holder.settingValue.text = setting.value
        
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Edit ${setting.title}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun getItemCount() = settings.size
}
