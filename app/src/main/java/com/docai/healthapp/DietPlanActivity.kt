package com.docai.healthapp

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView

class DietPlanActivity : AppCompatActivity() {
    
    private lateinit var breakfastRecycler: RecyclerView
    private lateinit var lunchRecycler: RecyclerView
    private lateinit var dinnerRecycler: RecyclerView
    private lateinit var dietPlanInput: EditText
    private lateinit var getPlanButton: Button
    private lateinit var dietPlanSection: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_plan)
        
        setupUI()
        setupRecipes()
    }
    
    private fun setupUI() {
        breakfastRecycler = findViewById(R.id.breakfastRecycler)
        lunchRecycler = findViewById(R.id.lunchRecycler)
        dinnerRecycler = findViewById(R.id.dinnerRecycler)
        dietPlanInput = findViewById(R.id.dietPlanInput)
        getPlanButton = findViewById(R.id.getPlanButton)
        dietPlanSection = findViewById(R.id.dietPlanSection)
        
        // Get Plan button click listener
        getPlanButton.setOnClickListener {
            val inputText = dietPlanInput.text.toString().trim()
            if (inputText.isNotEmpty()) {
                generateDietPlan(inputText)
            } else {
                Toast.makeText(this, "Please enter a diet preference", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Footer navigation click listeners
        findViewById<LinearLayout>(R.id.footerNavigation).getChildAt(0).setOnClickListener {
            val intent = android.content.Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<LinearLayout>(R.id.footerNavigation).getChildAt(1).setOnClickListener {
            val intent = android.content.Intent(this, ConsultancyActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<LinearLayout>(R.id.footerNavigation).getChildAt(2).setOnClickListener {
            val intent = android.content.Intent(this, DeviceActivity::class.java)
            startActivity(intent)
        }
        
        // Diet Plan tab is already active, no need to set click listener
    }
    
    private fun generateDietPlan(preference: String) {
        // Simulate diet plan generation
        Toast.makeText(this, "Generating $preference diet plan...", Toast.LENGTH_SHORT).show()
        
        // Update the diet plan section with the new preference
        val titleText = dietPlanSection.getChildAt(0) as TextView
        titleText.text = "${preference.uppercase()} DIET PLAN"
        
        // Show the diet plan section
        dietPlanSection.visibility = View.VISIBLE
    }
    
    private fun setupRecipes() {
        // Breakfast recipes
        val breakfastRecipes = listOf(
            Recipe("Avocado Toast", "10 Min • 250 Kcal", R.drawable.ic_avocado),
            Recipe("Oatmeal", "15 Min • 150 Kcal", R.drawable.ic_oatmeal),
            Recipe("Smoothie Bowl", "5 Min • 200 Kcal", R.drawable.ic_smoothie),
            Recipe("Greek Yogurt", "2 Min • 120 Kcal", R.drawable.ic_yogurt)
        )
        
        breakfastRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        breakfastRecycler.adapter = RecipeAdapter(breakfastRecipes)
        
        // Lunch recipes
        val lunchRecipes = listOf(
            Recipe("Chicken Salad", "20 Min • 350 Kcal", R.drawable.ic_chicken_salad),
            Recipe("Quinoa Bowl", "25 Min • 400 Kcal", R.drawable.ic_quinoa),
            Recipe("Grilled Salmon", "15 Min • 300 Kcal", R.drawable.ic_salmon),
            Recipe("Vegetable Wrap", "10 Min • 280 Kcal", R.drawable.ic_wrap)
        )
        
        lunchRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        lunchRecycler.adapter = RecipeAdapter(lunchRecipes)
        
        // Dinner recipes
        val dinnerRecipes = listOf(
            Recipe("Grilled Chicken", "30 Min • 400 Kcal", R.drawable.ic_grilled_chicken),
            Recipe("Vegetable Stir Fry", "20 Min • 250 Kcal", R.drawable.ic_stir_fry),
            Recipe("Baked Fish", "25 Min • 320 Kcal", R.drawable.ic_fish),
            Recipe("Pasta Primavera", "15 Min • 380 Kcal", R.drawable.ic_pasta)
        )
        
        dinnerRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dinnerRecycler.adapter = RecipeAdapter(dinnerRecipes)
    }
}

data class Recipe(
    val name: String,
    val details: String,
    val imageRes: Int
)

class RecipeAdapter(private val recipes: List<Recipe>) : 
    RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        val recipeDetails: TextView = itemView.findViewById(R.id.recipeDetails)
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name
        holder.recipeDetails.text = recipe.details
        holder.recipeImage.setImageResource(recipe.imageRes)
        
        holder.itemView.setOnClickListener {
            // Handle recipe click
            Toast.makeText(holder.itemView.context, "Opening ${recipe.name} recipe", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun getItemCount() = recipes.size
}
