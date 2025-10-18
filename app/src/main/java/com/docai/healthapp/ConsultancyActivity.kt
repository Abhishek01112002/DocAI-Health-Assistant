package com.docai.healthapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ConsultancyActivity : AppCompatActivity() {
    
    private lateinit var chatRecycler: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultancy)
        
        setupUI()
        setupChat()
    }
    
    private fun setupUI() {
        chatRecycler = findViewById(R.id.chatRecycler)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        
        // Setup footer navigation
        val footerNavigation = findViewById<LinearLayout>(R.id.footerNavigation)
        
        // Home tab
        footerNavigation.getChildAt(0).setOnClickListener {
            val intent = android.content.Intent(this, HomeActivity::class.java)
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
        
        // Consultancy tab is already active, no need to set click listener
    }
    
    private fun setupChat() {
        chatAdapter = ChatAdapter(chatMessages)
        chatRecycler.layoutManager = LinearLayoutManager(this)
        chatRecycler.adapter = chatAdapter
        
        // Add welcome message
        addMessage("Hello! I'm your health consultant. How can I help you today?", false)
        
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessage(message, true)
                messageInput.text.clear()
                
                // Simulate AI response
                simulateAIResponse(message)
            }
        }
    }
    
    private fun addMessage(message: String, isUser: Boolean) {
        val chatMessage = ChatMessage(message, isUser, System.currentTimeMillis())
        chatMessages.add(chatMessage)
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecycler.scrollToPosition(chatMessages.size - 1)
    }
    
    private fun simulateAIResponse(userMessage: String) {
        // Simulate AI thinking time
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val response = when {
                userMessage.contains("steps", ignoreCase = true) -> 
                    "Based on your activity data, I recommend aiming for 10,000 steps daily for optimal health."
                userMessage.contains("heart", ignoreCase = true) -> 
                    "A normal resting heart rate is between 60-100 BPM. Regular exercise can help improve your heart health."
                userMessage.contains("diet", ignoreCase = true) -> 
                    "I suggest focusing on whole foods, plenty of vegetables, and staying hydrated. Check out our diet plan section!"
                userMessage.contains("sleep", ignoreCase = true) -> 
                    "Adults should aim for 7-9 hours of quality sleep per night. Try maintaining a consistent sleep schedule."
                else -> 
                    "That's a great question! I'd be happy to help you with that. Could you provide more details about your health goals?"
            }
            addMessage(response, false)
        }, 1500)
    }
}

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long
)

class ChatAdapter(private val messages: List<ChatMessage>) : 
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestampText: TextView = itemView.findViewById(R.id.timestampText)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (viewType == 0) R.layout.item_user_message else R.layout.item_ai_message
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.message
        
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        holder.timestampText.text = timeFormat.format(java.util.Date(message.timestamp))
    }
    
    override fun getItemCount() = messages.size
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 0 else 1
    }
}
