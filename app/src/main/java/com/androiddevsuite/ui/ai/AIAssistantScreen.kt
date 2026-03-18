/**
 * Android Development Suite - AI Assistant Screen
 * منصة تطوير أندرويد الشاملة
 * 
 * AI-powered coding assistant interface
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Chat message data class.
 */
data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val codeBlocks: List<CodeBlock> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Code block in a message.
 */
data class CodeBlock(
    val language: String,
    val code: String
)

/**
 * AI suggestion data.
 */
data class AISuggestion(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * AI Assistant ViewModel.
 */
@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val aiManager: com.androiddevsuite.ai.AIManager,
    private val ollamaManager: com.androiddevsuite.ai.OllamaAIManager
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<AISuggestion>>(getDefaultSuggestions())
    val suggestions: StateFlow<List<AISuggestion>> = _suggestions.asStateFlow()
    
    private val _isOllamaConnected = MutableStateFlow(false)
    val isOllamaConnected: StateFlow<Boolean> = _isOllamaConnected.asStateFlow()
    
    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()
    
    init {
        // Add welcome message
        _messages.value = listOf(
            ChatMessage(
                id = "welcome",
                content = "مرحباً! أنا مساعدك الذكي لتطوير تطبيقات Android. كيف يمكنني مساعدتك؟\n\n" +
                    "• شرح الكود\n" +
                    "• اقتراح تحسينات\n" +
                    "• كتابة كود جديد\n" +
                    "• إصلاح الأخطاء\n" +
                    "• إنشاء اختبارات\n\n" +
                    "مدعوم بواسطة Ollama للنماذج المحلية",
                isFromUser = false
            )
        )
        
        // Check Ollama connection
        viewModelScope.launch {
            checkOllamaConnection()
        }
    }
    
    private suspend fun checkOllamaConnection() {
        _isOllamaConnected.value = aiManager.isOllamaConnected()
        if (_isOllamaConnected.value) {
            _availableModels.value = aiManager.getAvailableModels()
        }
    }
    
    fun sendMessage(text: String) {
        val userMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            content = text,
            isFromUser = true
        )
        
        _messages.value = _messages.value + userMessage
        _isProcessing.value = true
        
        // Add loading message
        val loadingId = "loading_${System.currentTimeMillis()}"
        _messages.value = _messages.value + ChatMessage(
            id = loadingId,
            content = "",
            isFromUser = false,
            isLoading = true
        )
        
        viewModelScope.launch {
            val response = if (_isOllamaConnected.value) {
                // Use Ollama for AI response
                try {
                    val chatMessages = _messages.value
                        .filter { !it.isLoading }
                        .takeLast(10) // Keep last 10 messages for context
                        .map { msg ->
                            (if (msg.isFromUser) "user" else "assistant") to msg.content
                        }
                    
                    val aiResponse = aiManager.chat(chatMessages)
                    generateChatMessage(aiResponse)
                } catch (e: Exception) {
                    generateFallbackResponse(text)
                }
            } else {
                // Fallback to pattern matching
                delay(1000)
                generateFallbackResponse(text)
            }
            
            // Remove loading message and add response
            _messages.value = _messages.value.filter { it.id != loadingId } + response
            _isProcessing.value = false
        }
    }
    
    private fun generateChatMessage(aiResponse: String): ChatMessage {
        val codeBlocks = parseCodeBlocks(aiResponse)
        
        return ChatMessage(
            id = System.currentTimeMillis().toString(),
            content = aiResponse.replace(Regex("```\\w*\n[\\s\\S]*?```"), "").trim(),
            isFromUser = false,
            codeBlocks = codeBlocks
        )
    }
    
    private fun generateFallbackResponse(userMessage: String): ChatMessage {
        val response = when {
            userMessage.contains("شرح", ignoreCase = true) -> {
                "سأشرح لك الكود المطلوب. حدد الكود الذي تريد شرحه وسأقوم بتحليله خطوة بخطوة."
            }
            userMessage.contains("كود", ignoreCase = true) || userMessage.contains("كتابة", ignoreCase = true) -> {
                """
                إليك مثال على كود Kotlin:
                
                ```kotlin
                @Composable
                fun Greeting(name: String) {
                    Text(
                        text = "مرحباً $name!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                ```
                
                هذا مكون Compose بسيط يعرض رسالة ترحيب.
                """.trimIndent()
            }
            userMessage.contains("خطأ", ignoreCase = true) || userMessage.contains("error", ignoreCase = true) -> {
                "أرسل لي رسالة الخطأ وسأساعدك في حلها. يمكنك نسخ الخطأ من Logcat أو من نافذة البناء."
            }
            userMessage.contains("تحسين", ignoreCase = true) -> {
                """
                إليك بعض النصائح لتحسين الكود:
                
                1. **استخدم Data Classes** للبيانات البسيطة
                2. **أضف تعليقات** للدوال المعقدة
                3. **استخدم Coroutines** للعمليات غير المتزامنة
                4. **تخلص من الموارد** عند الانتهاء منها
                5. **اختبر الكود** بشكل دوري
                """.trimIndent()
            }
            else -> {
                "شكراً على رسالتك! كيف يمكنني مساعدتك في تطوير تطبيق Android؟\n\nيمكنك سؤالي عن:\n• شرح الكود\n• كتابة كود جديد\n• إصلاح الأخطاء\n• تحسين الأداء\n• أفضل الممارسات"
            }
        }
        
        // Parse code blocks
        val codeBlocks = parseCodeBlocks(response)
        
        return ChatMessage(
            id = System.currentTimeMillis().toString(),
            content = response.replace(Regex("```\\w*\n[\\s\\S]*?```"), "").trim(),
            isFromUser = false,
            codeBlocks = codeBlocks
        )
    }
    
    private fun parseCodeBlocks(text: String): List<CodeBlock> {
        val codeBlocks = mutableListOf<CodeBlock>()
        val regex = Regex("```(\\w*)\n([\\s\\S]*?)```")
        
        regex.findAll(text).forEach { match ->
            codeBlocks.add(CodeBlock(
                language = match.groupValues[1].ifEmpty { "text" },
                code = match.groupValues[2].trim()
            ))
        }
        
        return codeBlocks
    }
    
    fun clearHistory() {
        _messages.value = listOf(
            ChatMessage(
                id = "welcome",
                content = "تم مسح المحادثة. كيف يمكنني مساعدتك؟",
                isFromUser = false
            )
        )
    }
    
    companion object {
        private fun getDefaultSuggestions() = listOf(
            AISuggestion("1", "شرح كود", "اشرح لي هذا الكود", Icons.Outlined.Code),
            AISuggestion("2", "إصلاح خطأ", "ساعدني في إصلاح خطأ", Icons.Outlined.BugReport),
            AISuggestion("3", "تحسين الأداء", "كيف أحسن هذا الكود؟", Icons.Outlined.Speed),
            AISuggestion("4", "كتابة اختبار", "اكتب اختبار لهذا الكود", Icons.Outlined.Science)
        )
    }
}

/**
 * AI Assistant Screen - Chat interface with AI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    viewModel: AIAssistantViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Assistant")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear history")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(message)
                }
            }
            
            // Quick suggestions
            AnimatedVisibility(
                visible = messages.size <= 2 && !isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(WeekPickerDefaults.ScrollableTabRowScrollState)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { 
                                inputText = suggestion.description
                                viewModel.sendMessage(suggestion.description)
                            },
                            label = { Text(suggestion.title) },
                            icon = {
                                Icon(
                                    suggestion.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Input area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask anything about Android development...") },
                        trailingIcon = {
                            if (inputText.isNotEmpty()) {
                                IconButton(onClick = { inputText = "" }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isProcessing) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Chat message item.
 */
@Composable
private fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // AI avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isFromUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                )
            ) {
                if (message.isLoading) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري التفكير...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // Code blocks
                        message.codeBlocks.forEach { codeBlock ->
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            CodeBlockCard(codeBlock)
                        }
                    }
                }
            }
        }
        
        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            
            // User avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Code block card.
 */
@Composable
private fun CodeBlockCard(codeBlock: CodeBlock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Language label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codeBlock.language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = { /* Copy code */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // Code content
            Text(
                text = codeBlock.code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// Helper for horizontalScroll
private val WeekPickerDefaults.ScrollableTabRowScrollState = androidx.compose.foundation.ScrollState(0)
