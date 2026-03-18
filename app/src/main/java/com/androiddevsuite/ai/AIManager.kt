/**
 * Android Development Suite - AI Manager
 * منصة تطوير أندرويد الشاملة
 * 
 * Artificial Intelligence management system with:
 * - Ollama for local LLM inference
 * - Code completion suggestions
 * - Error detection and fixes
 * - Natural language to code conversion
 * 
 * @author Lead Systems Architect
 * @version 2.0.0
 */
package com.androiddevsuite.ai

import android.content.Context
import com.androiddevsuite.data.model.CodeIssue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class for code suggestions.
 */
data class CodeSuggestion(
    val text: String,
    val displayText: String,
    val confidence: Float,
    val type: SuggestionType
)

/**
 * Types of code suggestions.
 */
enum class SuggestionType {
    KEYWORD,
    FUNCTION,
    VARIABLE,
    CLASS,
    METHOD,
    PROPERTY,
    IMPORT,
    SNIPPET,
    COMPLETION,
    FIX
}

/**
 * Data class for AI analysis results.
 */
data class AnalysisResult(
    val issues: List<CodeIssue>,
    val suggestions: List<String>,
    val complexity: Int,
    val qualityScore: Float
)

/**
 * AI Model configuration.
 */
data class AIModelConfig(
    val modelName: String,
    val baseUrl: String,
    val maxTokens: Int,
    val temperature: Float
)

/**
 * AI Manager - Singleton for managing AI capabilities.
 * 
 * Features:
 * - Ollama integration for local LLM inference
 * - Code completion and suggestions
 * - Error detection and automatic fixes
 * - Natural language to code conversion
 * - Chat capabilities
 */
@Singleton
class AIManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ollamaManager: OllamaAIManager
) {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(true)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    // Default model configurations for Ollama
    private val models = mapOf(
        "code_completion" to AIModelConfig(
            modelName = "codellama:7b",
            baseUrl = "http://localhost:11434",
            maxTokens = 512,
            temperature = 0.3f
        ),
        "error_detection" to AIModelConfig(
            modelName = "codellama:7b",
            baseUrl = "http://localhost:11434",
            maxTokens = 256,
            temperature = 0.2f
        ),
        "nl_to_code" to AIModelConfig(
            modelName = "codellama:7b",
            baseUrl = "http://localhost:11434",
            maxTokens = 1024,
            temperature = 0.7f
        )
    )
    
    /**
     * Initialize AI models.
     * Must be called before using any AI features.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Initializing AI Manager with Ollama...")
            
            // Initialize Ollama connection
            val result = ollamaManager.initialize()
            
            if (result.isSuccess) {
                _isReady.value = true
                _isOfflineMode.value = false
                Timber.i("AI Manager initialized successfully with Ollama")
                Result.success(Unit)
            } else {
                _isReady.value = false
                _isOfflineMode.value = true
                Timber.w("Ollama not available, running in offline mode")
                Result.success(Unit) // Still succeed, just offline
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize AI Manager")
            _isReady.value = false
            _isOfflineMode.value = true
            Result.failure(e)
        }
    }
    
    /**
     * Get code suggestions based on current context.
     * Uses Ollama for intelligent code completion.
     */
    suspend fun getCodeSuggestions(
        code: String,
        cursorPosition: Int,
        language: String = "kotlin"
    ): List<CodeSuggestion> = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext getCommonKotlinSuggestions()
        }
        
        try {
            // Use Ollama for suggestions
            val ollamaSuggestions = ollamaManager.getCodeSuggestions(code, cursorPosition, language)
            
            // Return Ollama suggestions or fallback
            ollamaSuggestions.ifEmpty { getCommonKotlinSuggestions() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get code suggestions")
            getCommonKotlinSuggestions()
        }
    }
    
    /**
     * Analyze code for issues and improvements.
     * Uses Ollama for intelligent code analysis.
     */
    suspend fun analyzeCode(
        code: String,
        language: String = "kotlin"
    ): AnalysisResult = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext AnalysisResult(
                issues = emptyList(),
                suggestions = emptyList(),
                complexity = calculateComplexity(code),
                qualityScore = 0.5f
            )
        }
        
        try {
            val aiAnalysis = ollamaManager.analyzeCode(code, language)
            
            AnalysisResult(
                issues = aiAnalysis.issues,
                suggestions = aiAnalysis.suggestions,
                complexity = aiAnalysis.complexity,
                qualityScore = aiAnalysis.qualityScore
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze code")
            AnalysisResult(
                issues = emptyList(),
                suggestions = emptyList(),
                complexity = calculateComplexity(code),
                qualityScore = 0.5f
            )
        }
    }
    
    /**
     * Convert natural language to code.
     * Uses Ollama for code generation.
     */
    suspend fun naturalLanguageToCode(
        description: String,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext "// AI not available. Please connect to Ollama."
        }
        
        try {
            ollamaManager.naturalLanguageToCode(description, language)
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert natural language to code")
            "// Error: ${e.message}"
        }
    }
    
    /**
     * Explain selected code in natural language.
     * Uses Ollama for code explanation.
     */
    suspend fun explainCode(code: String): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext buildString {
                appendLine("Code Analysis (Offline Mode):")
                appendLine()
                val patterns = detectPatterns(code)
                if (patterns.isNotEmpty()) {
                    appendLine("Detected Patterns:")
                    patterns.forEach { pattern ->
                        appendLine("  • $pattern")
                    }
                }
            }
        }
        
        try {
            ollamaManager.explainCode(code)
        } catch (e: Exception) {
            Timber.e(e, "Failed to explain code")
            "Error explaining code: ${e.message}"
        }
    }
    
    /**
     * Generate unit tests for given code.
     * Uses Ollama for test generation.
     */
    suspend fun generateTests(
        code: String,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext generateBasicTests(code, language)
        }
        
        try {
            ollamaManager.generateTests(code, language)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate tests")
            generateBasicTests(code, language)
        }
    }
    
    /**
     * Refactor code according to best practices.
     * Uses Ollama for intelligent refactoring.
     */
    suspend fun refactorCode(
        code: String,
        language: String = "kotlin"
    ): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext code
        }
        
        try {
            ollamaManager.refactorCode(code, language)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refactor code")
            code
        }
    }
    
    /**
     * Chat with AI assistant.
     */
    suspend fun chat(messages: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        if (!_isReady.value) {
            return@withContext "AI not available. Please connect to Ollama server."
        }
        
        try {
            ollamaManager.chat(messages)
        } catch (e: Exception) {
            Timber.e(e, "Chat failed")
            "Error: ${e.message}"
        }
    }
    
    /**
     * Check if Ollama is connected.
     */
    suspend fun isOllamaConnected(): Boolean = withContext(Dispatchers.IO) {
        ollamaManager.isOllamaRunning()
    }
    
    /**
     * Get available Ollama models.
     */
    fun getAvailableModels(): List<String> {
        return ollamaManager.availableModels.value
    }
    
    /**
     * Set the Ollama model to use.
     */
    fun setModel(model: String) {
        ollamaManager.setModel(model)
    }
    
    // Private helper methods
    
    private fun detectPatterns(code: String): List<String> {
        val patterns = mutableListOf<String>()
        
        if (code.contains("ViewModel")) patterns.add("MVVM Architecture")
        if (code.contains("Repository")) patterns.add("Repository Pattern")
        if (code.contains("Hilt") || code.contains("Dagger")) patterns.add("Dependency Injection")
        if (code.contains("StateFlow") || code.contains("LiveData")) patterns.add("Reactive Programming")
        if (code.contains("suspend")) patterns.add("Coroutines")
        if (code.contains("@Composable")) patterns.add("Jetpack Compose")
        if (code.contains("Room") || code.contains("@Entity")) patterns.add("Room Database")
        
        return patterns
    }
    
    private fun calculateComplexity(code: String): Int {
        var complexity = 1
        
        complexity += code.split("if").size - 1
        complexity += code.split("when").size - 1
        complexity += code.split("for").size - 1
        complexity += code.split("while").size - 1
        
        return complexity
    }
    
    private fun generateBasicTests(code: String, language: String): String {
        val functions = extractFunctions(code)
        val classes = extractClasses(code)
        
        return buildString {
            appendLine("// Auto-generated tests (Offline Mode)")
            appendLine("import org.junit.Test")
            appendLine("import org.junit.Assert.*")
            appendLine()
            
            classes.forEach { className ->
                appendLine("class ${className}Test {")
                
                functions.forEach { function ->
                    appendLine("    @Test")
                    appendLine("    fun test${function.replaceFirstChar { it.uppercase() }}() {")
                    appendLine("        // TODO: Implement test")
                    appendLine("    }")
                    appendLine()
                }
                
                appendLine("}")
            }
        }
    }
    
    private fun extractFunctions(code: String): List<String> {
        val functions = mutableListOf<String>()
        val regex = Regex("fun\\s+(\\w+)\\s*\\(")
        regex.findAll(code).forEach { match ->
            functions.add(match.groupValues[1])
        }
        return functions
    }
    
    private fun extractClasses(code: String): List<String> {
        val classes = mutableListOf<String>()
        val regex = Regex("(?:class|interface|object)\\s+(\\w+)")
        regex.findAll(code).forEach { match ->
            classes.add(match.groupValues[1])
        }
        return classes
    }
    
    private fun getCommonKotlinSuggestions(): List<CodeSuggestion> {
        return listOf(
            CodeSuggestion("fun", "fun", 0.95f, SuggestionType.KEYWORD),
            CodeSuggestion("val", "val", 0.94f, SuggestionType.KEYWORD),
            CodeSuggestion("var", "var", 0.93f, SuggestionType.KEYWORD),
            CodeSuggestion("if", "if", 0.92f, SuggestionType.KEYWORD),
            CodeSuggestion("when", "when", 0.91f, SuggestionType.KEYWORD),
            CodeSuggestion("for", "for", 0.90f, SuggestionType.KEYWORD),
            CodeSuggestion("return", "return", 0.89f, SuggestionType.KEYWORD),
            CodeSuggestion("class", "class", 0.88f, SuggestionType.KEYWORD),
            CodeSuggestion("object", "object", 0.87f, SuggestionType.KEYWORD),
            CodeSuggestion("import", "import", 0.86f, SuggestionType.IMPORT)
        )
    }
    
    /**
     * Release resources.
     */
    fun release() {
        _isReady.value = false
        Timber.d("AI Manager released")
    }
}
