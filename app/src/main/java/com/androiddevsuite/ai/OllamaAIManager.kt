/**
 * Android Development Suite - Ollama AI Manager
 * منصة تطوير أندرويد الشاملة
 * 
 * AI integration using Ollama for local LLM inference
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ai

import android.content.Context
import com.androiddevsuite.data.model.AiAnalysis
import com.androiddevsuite.data.model.CodeIssue
import com.androiddevsuite.data.model.IssueSeverity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ollama API Models
 */
@JsonClass(generateAdapter = true)
data class OllamaGenerateRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false,
    val options: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class OllamaGenerateResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean,
    val context: List<Int>? = null,
    val total_duration: Long? = null,
    val load_duration: Long? = null,
    val prompt_eval_count: Int? = null,
    val eval_count: Int? = null
)

@JsonClass(generateAdapter = true)
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OllamaMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OllamaChatResponse(
    val model: String,
    val created_at: String,
    val message: OllamaMessage,
    val done: Boolean
)

@JsonClass(generateAdapter = true)
data class OllamaModel(
    val name: String,
    val modified_at: String,
    val size: Long,
    val digest: String
)

@JsonClass(generateAdapter = true)
data class OllamaModelsResponse(
    val models: List<OllamaModel>
)

/**
 * Ollama AI Configuration.
 */
data class OllamaConfig(
    val baseUrl: String = "http://localhost:11434",
    val model: String = "codellama:7b",
    val timeoutSeconds: Long = 60,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f
)

/**
 * Connection status for Ollama server.
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CHECKING,
    ERROR
}

/**
 * Ollama AI Manager - Handles AI operations using Ollama API.
 * 
 * Features:
 * - Local LLM inference via Ollama
 * - Code completion
 * - Code analysis and error detection
 * - Natural language to code conversion
 * - Code explanation
 * - Test generation
 */
@Singleton
class OllamaAIManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val config = MutableStateFlow(OllamaConfig())
    val currentConfig: StateFlow<OllamaConfig> = config.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val json = com.squareup.moshi.Moshi.Builder().build()
    private val adapter = json.adapter(OllamaGenerateResponse::class.java)
    private val chatAdapter = json.adapter(OllamaChatResponse::class.java)
    private val modelsAdapter = json.adapter(OllamaModelsResponse::class.java)
    
    /**
     * Initialize connection to Ollama server.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _connectionStatus.value = ConnectionStatus.CHECKING
            
            // Check if Ollama is running
            val request = Request.Builder()
                .url("${config.value.baseUrl}/api/tags")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val modelsResponse = modelsAdapter.fromJson(body)
                    _availableModels.value = modelsResponse?.models?.map { it.name } ?: emptyList()
                }
                
                _connectionStatus.value = ConnectionStatus.CONNECTED
                _isReady.value = true
                
                Timber.i("Connected to Ollama. Available models: ${_availableModels.value}")
                Result.success(Unit)
            } else {
                _connectionStatus.value = ConnectionStatus.ERROR
                _isReady.value = false
                Result.failure(Exception("Ollama server returned ${response.code}"))
            }
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            _isReady.value = false
            Timber.e(e, "Failed to connect to Ollama")
            Result.failure(e)
        }
    }
    
    /**
     * Update Ollama configuration.
     */
    fun updateConfig(newConfig: OllamaConfig) {
        config.value = newConfig
        Timber.d("Ollama config updated: $newConfig")
    }
    
    /**
     * Set the model to use.
     */
    fun setModel(model: String) {
        config.value = config.value.copy(model = model)
        Timber.d("Model set to: $model")
    }
    
    /**
     * Get code completions from Ollama.
     */
    suspend fun getCodeSuggestions(
        code: String,
        cursorPosition: Int,
        language: String = "kotlin"
    ): List<CodeSuggestion> = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext emptyList()
        }
        
        try {
            val contextStart = maxOf(0, cursorPosition - 512)
            val contextEnd = minOf(code.length, cursorPosition + 128)
            val context = code.substring(contextStart, contextEnd)
            
            val prompt = buildCompletionPrompt(context, language)
            val response = generate(prompt)
            
            if (response != null) {
                parseCodeSuggestions(response, language)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get code suggestions")
            emptyList()
        }
    }
    
    /**
     * Analyze code for issues and improvements.
     */
    suspend fun analyzeCode(
        code: String,
        language: String = "kotlin"
    ): AiAnalysis = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext AiAnalysis(
                issues = emptyList(),
                suggestions = emptyList(),
                complexity = 0,
                qualityScore = 0f,
                documentation = null
            )
        }
        
        try {
            val prompt = buildAnalysisPrompt(code, language)
            val response = generate(prompt)
            
            if (response != null) {
                parseAnalysisResult(response, code)
            } else {
                AiAnalysis(
                    issues = emptyList(),
                    suggestions = emptyList(),
                    complexity = calculateComplexity(code),
                    qualityScore = 0.5f,
                    documentation = null
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze code")
            AiAnalysis(
                issues = emptyList(),
                suggestions = emptyList(),
                complexity = 0,
                qualityScore = 0f,
                documentation = null
            )
        }
    }
    
    /**
     * Convert natural language to code.
     */
    suspend fun naturalLanguageToCode(
        description: String,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext ""
        }
        
        try {
            val prompt = buildCodeGenerationPrompt(description, language)
            generate(prompt) ?: ""
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert natural language to code")
            ""
        }
    }
    
    /**
     * Explain code in natural language.
     */
    suspend fun explainCode(code: String): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext "AI not available"
        }
        
        try {
            val prompt = """
You are a code documentation expert. Explain the following code in clear, simple terms:

```
$code
```

Provide:
1. A brief summary of what the code does
2. Key components and their purposes
3. Any important patterns or techniques used

Keep the explanation concise and easy to understand.
""".trimIndent()
            
            generate(prompt) ?: "Unable to explain code"
        } catch (e: Exception) {
            Timber.e(e, "Failed to explain code")
            "Error explaining code"
        }
    }
    
    /**
     * Generate unit tests for code.
     */
    suspend fun generateTests(
        code: String,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext ""
        }
        
        try {
            val prompt = """
You are a test generation expert. Generate comprehensive unit tests for the following $language code:

```
$code
```

Generate tests that cover:
1. Normal use cases
2. Edge cases
3. Error handling

Use appropriate testing frameworks (JUnit, MockK for Kotlin).
Output only the test code, no explanations.
""".trimIndent()
            
            generate(prompt) ?: ""
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate tests")
            ""
        }
    }
    
    /**
     * Refactor code according to best practices.
     */
    suspend fun refactorCode(
        code: String,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext code
        }
        
        try {
            val prompt = """
You are a code refactoring expert. Refactor the following $language code to follow best practices:

```
$code
```

Apply these improvements:
1. Better naming conventions
2. Extract reusable functions
3. Remove code duplication
4. Improve readability
5. Follow SOLID principles

Output only the refactored code, no explanations.
""".trimIndent()
            
            generate(prompt) ?: code
        } catch (e: Exception) {
            Timber.e(e, "Failed to refactor code")
            code
        }
    }
    
    /**
     * Fix code issues.
     */
    suspend fun fixCode(
        code: String,
        issues: List<String>,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext code
        }
        
        try {
            val issuesText = issues.joinToString("\n- ", "- ")
            
            val prompt = """
You are a code fixer. Fix the following issues in this $language code:

Issues:
$issuesText

Code:
```
$code
```

Output only the fixed code, no explanations.
""".trimIndent()
            
            generate(prompt) ?: code
        } catch (e: Exception) {
            Timber.e(e, "Failed to fix code")
            code
        }
    }
    
    /**
     * Chat with AI assistant.
     */
    suspend fun chat(
        messages: List<Pair<String, String>> // role to content
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext "AI not available. Please ensure Ollama is running."
        }
        
        try {
            val ollamaMessages = messages.map { (role, content) ->
                OllamaMessage(role, content)
            }
            
            val request = OllamaChatRequest(
                model = config.value.model,
                messages = ollamaMessages,
                stream = false
            )
            
            val requestBody = json.adapter(OllamaChatRequest::class.java)
                .toJson(request)
                .toRequestBody("application/json".toMediaType())
            
            val httpRequest = Request.Builder()
                .url("${config.value.baseUrl}/api/chat")
                .post(requestBody)
                .build()
            
            val response = client.newCall(httpRequest).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val chatResponse = chatAdapter.fromJson(body)
                    return@withContext chatResponse?.message?.content ?: "No response"
                }
            }
            
            "Error: ${response.code}"
        } catch (e: Exception) {
            Timber.e(e, "Chat failed")
            "Error: ${e.message}"
        }
    }
    
    // Private helper methods
    
    private suspend fun generate(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = OllamaGenerateRequest(
                model = config.value.model,
                prompt = prompt,
                stream = false,
                options = mapOf(
                    "num_predict" to config.value.maxTokens,
                    "temperature" to config.value.temperature
                )
            )
            
            val requestBody = json.adapter(OllamaGenerateRequest::class.java)
                .toJson(request)
                .toRequestBody("application/json".toMediaType())
            
            val httpRequest = Request.Builder()
                .url("${config.value.baseUrl}/api/generate")
                .post(requestBody)
                .build()
            
            val response = client.newCall(httpRequest).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val genResponse = adapter.fromJson(body)
                    return@withContext genResponse?.response
                }
            }
            
            null
        } catch (e: Exception) {
            Timber.e(e, "Generation failed")
            null
        }
    }
    
    private fun buildCompletionPrompt(code: String, language: String): String {
        return """
You are an intelligent code completion assistant for $language.
Complete the following code. Output only the completion, no explanations.

Code context:
```
$code
```

Provide the most likely completion for this code.
""".trimIndent()
    }
    
    private fun buildAnalysisPrompt(code: String, language: String): String {
        return """
You are a code analysis expert. Analyze the following $language code and provide:

1. List of issues (format: LINE:MESSAGE)
2. Suggestions for improvement
3. Complexity score (1-10)
4. Quality score (0-100)

Code:
```
$code
```

Format your response as JSON:
{
  "issues": [{"line": N, "message": "...", "severity": "error|warning|info"}],
  "suggestions": ["..."],
  "complexity": N,
  "qualityScore": N
}
""".trimIndent()
    }
    
    private fun buildCodeGenerationPrompt(description: String, language: String): String {
        return """
You are an expert $language developer. Generate code based on this description:

$description

Requirements:
- Use idiomatic $language code
- Include necessary imports
- Follow best practices
- Add brief comments for complex logic

Output only the code, no explanations.
""".trimIndent()
    }
    
    private fun parseCodeSuggestions(response: String, language: String): List<CodeSuggestion> {
        val suggestions = mutableListOf<CodeSuggestion>()
        
        // Parse the response and extract suggestions
        val lines = response.lines().filter { it.isNotBlank() }
        
        lines.forEach { line ->
            suggestions.add(CodeSuggestion(
                text = line.trim(),
                displayText = line.trim().take(50),
                confidence = 0.8f,
                type = detectSuggestionType(line)
            ))
        }
        
        return suggestions.take(10)
    }
    
    private fun detectSuggestionType(text: String): SuggestionType {
        return when {
            text.contains("fun ") -> SuggestionType.FUNCTION
            text.contains("import ") -> SuggestionType.IMPORT
            text.contains("val ") || text.contains("var ") -> SuggestionType.VARIABLE
            text.contains("{") && text.contains("}") -> SuggestionType.SNIPPET
            else -> SuggestionType.COMPLETION
        }
    }
    
    private fun parseAnalysisResult(response: String, code: String): AiAnalysis {
        val issues = mutableListOf<CodeIssue>()
        val suggestions = mutableListOf<String>()
        
        // Parse issues from response
        val issuePattern = Regex("(\\d+):(.+)")
        response.lines().forEach { line ->
            val match = issuePattern.find(line)
            if (match != null) {
                val lineNum = match.groupValues[1].toIntOrNull() ?: 0
                val message = match.groupValues[2]
                issues.add(CodeIssue(
                    message = message,
                    severity = if (line.contains("error", ignoreCase = true)) 
                        IssueSeverity.ERROR 
                    else 
                        IssueSeverity.WARNING,
                    line = lineNum,
                    column = 0,
                    endLine = lineNum,
                    endColumn = 0,
                    fix = null,
                    ruleId = "ai-analysis"
                ))
            }
        }
        
        return AiAnalysis(
            issues = issues,
            suggestions = suggestions,
            complexity = calculateComplexity(code),
            qualityScore = 100f - (issues.size * 10f).coerceIn(0f, 100f),
            documentation = null
        )
    }
    
    private fun calculateComplexity(code: String): Int {
        var complexity = 1
        complexity += code.split("if").size - 1
        complexity += code.split("when").size - 1
        complexity += code.split("for").size - 1
        complexity += code.split("while").size - 1
        return complexity
    }
    
    /**
     * Check if Ollama is running.
     */
    suspend fun isOllamaRunning(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${config.value.baseUrl}/api/tags")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Pull a model from Ollama registry.
     */
    suspend fun pullModel(modelName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBody = """{"name": "$modelName"}"""
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("${config.value.baseUrl}/api/pull")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Timber.i("Model pulled successfully: $modelName")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to pull model: ${response.code}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to pull model")
            Result.failure(e)
        }
    }
}
