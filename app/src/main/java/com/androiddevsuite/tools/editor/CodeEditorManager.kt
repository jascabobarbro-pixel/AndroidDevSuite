/**
 * Android Development Suite - Code Editor
 * منصة تطوير أندرويد الشاملة
 * 
 * Advanced code editor with syntax highlighting
 */
package com.androiddevsuite.tools.editor

import android.content.Context
import com.androiddevsuite.ai.AIManager
import com.androiddevsuite.ai.CodeSuggestion
import com.androiddevsuite.data.model.EditorSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programming language definitions.
 */
enum class ProgrammingLanguage(
    val extension: String,
    val displayName: String,
    val keywords: Set<String>,
    val types: Set<String>,
    val commentStart: String,
    val commentEnd: String? = null,
    val stringDelimiters: Set<String> = setOf("\"", "'")
) {
    KOTLIN(
        extension = "kt",
        displayName = "Kotlin",
        keywords = setOf(
            "package", "import", "class", "interface", "object", "fun", "val", "var",
            "if", "else", "when", "for", "while", "do", "try", "catch", "finally",
            "return", "break", "continue", "is", "as", "in", "out", "typeof",
            "suspend", "inline", "noinline", "crossinline", "reified", "abstract",
            "final", "open", "override", "private", "protected", "public", "internal",
            "companion", "data", "enum", "sealed", "annotation", "inner", "tailrec",
            "operator", "infix", "const", "vararg", "lateinit", "by", "where", "init",
            "get", "set", "field", "it", "this", "super", "null", "true", "false"
        ),
        types = setOf(
            "Int", "Long", "Short", "Byte", "Float", "Double", "Boolean", "Char",
            "String", "Unit", "Nothing", "Any", "Array", "List", "Set", "Map",
            "MutableList", "MutableSet", "MutableMap", "Sequence", "Pair", "Triple"
        ),
        commentStart = "//",
        commentEnd = null,
        stringDelimiters = setOf("\"", "'", "\"\"\"")
    ),
    JAVA(
        extension = "java",
        displayName = "Java",
        keywords = setOf(
            "package", "import", "class", "interface", "enum", "extends", "implements",
            "public", "private", "protected", "static", "final", "abstract", "native",
            "synchronized", "volatile", "transient", "strictfp", "void", "int", "long",
            "short", "byte", "float", "double", "boolean", "char", "if", "else",
            "for", "while", "do", "switch", "case", "default", "break", "continue",
            "return", "throw", "throws", "try", "catch", "finally", "new", "this",
            "super", "null", "true", "false", "instanceof", "assert", "const", "goto"
        ),
        types = setOf(
            "String", "Integer", "Long", "Short", "Byte", "Float", "Double",
            "Boolean", "Character", "Object", "Class", "Exception", "Runnable"
        ),
        commentStart = "//",
        commentEnd = null
    ),
    XML(
        extension = "xml",
        displayName = "XML",
        keywords = emptySet(),
        types = emptySet(),
        commentStart = "<!--",
        commentEnd = "-->",
        stringDelimiters = setOf("\"", "'")
    ),
    GRADLE(
        extension = "gradle",
        displayName = "Gradle",
        keywords = setOf(
            "plugins", "id", "version", "apply", "android", "defaultConfig",
            "buildTypes", "dependencies", "implementation", "api", "compileOnly",
            "runtimeOnly", "testImplementation", "androidTestImplementation",
            "kapt", "ksp", "annotationProcessor", "mavenCentral", "google",
            "jcenter", "mavenLocal", "include", "project", "task", "doLast",
            "doFirst", "from", "into", "exclude", "transitive"
        ),
        types = emptySet(),
        commentStart = "//",
        commentEnd = null
    ),
    JSON(
        extension = "json",
        displayName = "JSON",
        keywords = setOf("true", "false", "null"),
        types = emptySet(),
        commentStart = "//",
        commentEnd = null
    ),
    PROGUARD(
        extension = "pro",
        displayName = "ProGuard",
        keywords = setOf(
            "keep", "keepclassmembers", "keepclasseswithmembers", "keepnames",
            "keepclassmembernames", "keepclasseswithmembernames", "dontwarn",
            "optimizationpasses", "dontusemixedcaseclassnames", "dontskipnonpubliclibraryclasses",
            "verbose", "optimizations", "injar", "outjar", "libraryjars", "printmapping"
        ),
        types = emptySet(),
        commentStart = "#",
        commentEnd = null
    );

    companion object {
        fun fromExtension(ext: String): ProgrammingLanguage {
            return values().find { it.extension == ext.lowercase() } ?: KOTLIN
        }
    }
}

/**
 * Syntax highlight token.
 */
data class SyntaxToken(
    val type: TokenType,
    val start: Int,
    val end: Int,
    val text: String
)

/**
 * Token types for syntax highlighting.
 */
enum class TokenType {
    KEYWORD,
    TYPE,
    STRING,
    NUMBER,
    COMMENT,
    OPERATOR,
    PUNCTUATION,
    IDENTIFIER,
    ANNOTATION,
    FUNCTION,
    VARIABLE,
    XML_TAG,
    XML_ATTRIBUTE,
    XML_VALUE,
    WHITESPACE,
    UNKNOWN
}

/**
 * Code Editor Manager - Handles syntax highlighting and code analysis.
 */
@Singleton
class CodeEditorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiManager: AIManager
) {
    private val settings = EditorSettings()
    
    /**
     * Detect programming language from file.
     */
    fun detectLanguage(file: File): ProgrammingLanguage {
        val extension = file.extension.lowercase()
        return ProgrammingLanguage.fromExtension(extension)
    }
    
    /**
     * Tokenize code for syntax highlighting.
     */
    fun tokenize(code: String, language: ProgrammingLanguage): List<SyntaxToken> {
        val tokens = mutableListOf<SyntaxToken>()
        val chars = code.toCharArray()
        var i = 0
        
        while (i < chars.size) {
            val char = chars[i]
            
            when {
                // Whitespace
                char.isWhitespace() -> {
                    val start = i
                    while (i < chars.size && chars[i].isWhitespace()) i++
                    tokens.add(SyntaxToken(TokenType.WHITESPACE, start, i, code.substring(start, i)))
                }
                
                // Comments
                isCommentStart(code, i, language) -> {
                    val start = i
                    i = skipComment(code, i, language)
                    tokens.add(SyntaxToken(TokenType.COMMENT, start, i, code.substring(start, i)))
                }
                
                // Strings
                isStringStart(code, i, language) -> {
                    val start = i
                    val delimiter = getStringDelimiter(code, i, language)
                    i = skipString(code, i, delimiter)
                    tokens.add(SyntaxToken(TokenType.STRING, start, i, code.substring(start, i)))
                }
                
                // Numbers
                char.isDigit() || (char == '.' && i + 1 < chars.size && chars[i + 1].isDigit()) -> {
                    val start = i
                    while (i < chars.size && (chars[i].isDigit() || chars[i] == '.' || chars[i] in "eEfFdDlLxX")) i++
                    tokens.add(SyntaxToken(TokenType.NUMBER, start, i, code.substring(start, i)))
                }
                
                // Annotations
                char == '@' && language == ProgrammingLanguage.KOTLIN -> {
                    val start = i
                    i++
                    while (i < chars.size && (chars[i].isLetterOrDigit() || chars[i] == '_')) i++
                    tokens.add(SyntaxToken(TokenType.ANNOTATION, start, i, code.substring(start, i)))
                }
                
                // Identifiers and keywords
                char.isLetter() || char == '_' -> {
                    val start = i
                    while (i < chars.size && (chars[i].isLetterOrDigit() || chars[i] == '_')) i++
                    val text = code.substring(start, i)
                    
                    val tokenType = when {
                        language.keywords.contains(text) -> TokenType.KEYWORD
                        language.types.contains(text) -> TokenType.TYPE
                        text == "fun" || text == "function" || text == "def" -> TokenType.FUNCTION
                        text.startsWith("m", ignoreCase = true) && text.length > 1 -> TokenType.VARIABLE
                        else -> TokenType.IDENTIFIER
                    }
                    
                    tokens.add(SyntaxToken(tokenType, start, i, text))
                }
                
                // Operators
                char in "+-*/%=<>!&|^~?:." -> {
                    val start = i
                    i++
                    // Check for multi-char operators
                    if (i < chars.size) {
                        val twoChar = code.substring(start, i + 1)
                        if (twoChar in listOf("++", "--", "==", "!=", "<=", ">=", "&&", "||", 
                            "++", "+=", "-=", "*=", "/=", "%=", "->", "=>", "::", "..", "?.", "!!")) {
                            i++
                        }
                    }
                    tokens.add(SyntaxToken(TokenType.OPERATOR, start, i, code.substring(start, i)))
                }
                
                // Punctuation
                char in "(){}[];," -> {
                    tokens.add(SyntaxToken(TokenType.PUNCTUATION, i, i + 1, char.toString()))
                    i++
                }
                
                // Unknown
                else -> {
                    tokens.add(SyntaxToken(TokenType.UNKNOWN, i, i + 1, char.toString()))
                    i++
                }
            }
        }
        
        return tokens
    }
    
    /**
     * Get code suggestions.
     */
    suspend fun getSuggestions(
        code: String,
        cursorPosition: Int,
        language: ProgrammingLanguage
    ): List<CodeSuggestion> {
        return aiManager.getCodeSuggestions(code, cursorPosition, language.name.lowercase())
    }
    
    /**
     * Format code.
     */
    suspend fun formatCode(code: String, language: ProgrammingLanguage): String = withContext(Dispatchers.Default) {
        when (language) {
            ProgrammingLanguage.KOTLIN, ProgrammingLanguage.JAVA -> formatKotlinJava(code)
            ProgrammingLanguage.XML -> formatXml(code)
            ProgrammingLanguage.JSON -> formatJson(code)
            else -> code
        }
    }
    
    /**
     * Find matching bracket.
     */
    fun findMatchingBracket(code: String, position: Int): Int? {
        val openBrackets = "([{"
        val closeBrackets = ")]}"
        val stack = Stack<Int>()
        
        for (i in code.indices) {
            when {
                openBrackets.indexOf(code[i]) >= 0 -> stack.push(i)
                closeBrackets.indexOf(code[i]) >= 0 -> {
                    val openIndex = closeBrackets.indexOf(code[i])
                    if (stack.isNotEmpty()) {
                        val start = stack.pop()
                        if (i == position) return start
                        if (start == position) return i
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Auto-indent code.
     */
    fun autoIndent(code: String, language: ProgrammingLanguage): String {
        val indent = "    " // 4 spaces
        var level = 0
        val lines = code.lines()
        
        return lines.joinToString("\n") { line ->
            val trimmed = line.trim()
            
            // Decrease indent for closing braces
            val decreased = trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith(")")
            val currentLevel = if (decreased) (level - 1).coerceAtLeast(0) else level
            
            // Increase indent after opening braces
            level += trimmed.count { it == '{' || it == '[' || it == '(' }
            level -= trimmed.count { it == '}' || it == ']' || it == ')' }
            level = level.coerceAtLeast(0)
            
            indent.repeat(currentLevel) + trimmed
        }
    }
    
    // Helper methods
    
    private fun isCommentStart(code: String, index: Int, language: ProgrammingLanguage): Boolean {
        return when {
            code.startsWith(language.commentStart, index) -> true
            code.startsWith("/*", index) -> true
            code.startsWith("<!--", index) -> true
            code.startsWith("#", index) -> language == ProgrammingLanguage.PROGUARD
            else -> false
        }
    }
    
    private fun skipComment(code: String, index: Int, language: ProgrammingLanguage): Int {
        return when {
            code.startsWith("/*", index) -> {
                val end = code.indexOf("*/", index + 2)
                if (end >= 0) end + 2 else code.length
            }
            code.startsWith("<!--", index) -> {
                val end = code.indexOf("-->", index + 4)
                if (end >= 0) end + 3 else code.length
            }
            code.startsWith("//", index) || code.startsWith("#", index) -> {
                val end = code.indexOf('\n', index)
                if (end >= 0) end else code.length
            }
            else -> index + 1
        }
    }
    
    private fun isStringStart(code: String, index: Int, language: ProgrammingLanguage): Boolean {
        return language.stringDelimiters.any { code.startsWith(it, index) }
    }
    
    private fun getStringDelimiter(code: String, index: Int, language: ProgrammingLanguage): String {
        return language.stringDelimiters.find { code.startsWith(it, index) } ?: "\""
    }
    
    private fun skipString(code: String, index: Int, delimiter: String): Int {
        var i = index + delimiter.length
        while (i < code.length) {
            if (code[i] == '\\' && i + 1 < code.length) {
                i += 2 // Skip escape sequence
            } else if (code.startsWith(delimiter, i)) {
                return i + delimiter.length
            } else {
                i++
            }
        }
        return code.length
    }
    
    private fun formatKotlinJava(code: String): String {
        // Simple formatting - in production would use proper formatter
        return autoIndent(code, ProgrammingLanguage.KOTLIN)
    }
    
    private fun formatXml(code: String): String {
        var level = 0
        val indent = "    "
        
        return code.lines().joinToString("\n") { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("</") -> {
                    level = (level - 1).coerceAtLeast(0)
                    indent.repeat(level) + trimmed
                }
                trimmed.startsWith("<") && !trimmed.endsWith("/>") && !trimmed.startsWith("<?") && !trimmed.startsWith("<!") -> {
                    val result = indent.repeat(level) + trimmed
                    if (!trimmed.contains("/>")) level++
                    result
                }
                else -> indent.repeat(level) + trimmed
            }
        }
    }
    
    private fun formatJson(code: String): String {
        val builder = StringBuilder()
        var indent = 0
        val indentStr = "  "
        var inString = false
        
        for (char in code) {
            when {
                char == '"' && !inString -> {
                    inString = true
                    builder.append(char)
                }
                char == '"' && inString -> {
                    inString = false
                    builder.append(char)
                }
                inString -> builder.append(char)
                char in listOf('{', '[') -> {
                    builder.append(char).append('\n').append(indentStr.repeat(++indent))
                }
                char in listOf('}', ']') -> {
                    builder.append('\n').append(indentStr.repeat(--indent)).append(char)
                }
                char == ',' -> {
                    builder.append(char).append('\n').append(indentStr.repeat(indent))
                }
                char == ':' -> builder.append(": ")
                !char.isWhitespace() -> builder.append(char)
            }
        }
        
        return builder.toString()
    }
}
