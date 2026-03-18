/**
 * Android Development Suite - Data Models
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// =====================================================
// PROJECT MODELS
// =====================================================

/**
 * Project model representing an Android project.
 */
@Parcelize
data class Project(
    val id: String,
    val name: String,
    val path: String,
    val packageName: String,
    val description: String = "",
    val minSdk: Int = 26,
    val targetSdk: Int = 35,
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val template: ProjectTemplate = ProjectTemplate.EMPTY_ACTIVITY,
    val gitRemote: String? = null,
    val isFavorite: Boolean = false
) : Parcelable

/**
 * Project template types.
 */
enum class ProjectTemplate {
    EMPTY_ACTIVITY,
    BASIC_ACTIVITY,
    COMPOSE_ACTIVITY,
    NAVIGATION_DRAWER,
    BOTTOM_NAVIGATION,
    VIEW_MODEL_ACTIVITY
}

/**
 * Project file model.
 */
@Parcelize
data class ProjectFile(
    val name: String,
    val path: String,
    val type: FileType,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean
) : Parcelable

/**
 * File types in a project.
 */
enum class FileType {
    KOTLIN,
    JAVA,
    XML,
    GRADLE,
    PROGUARD,
    RESOURCE,
    ASSET,
    NATIVE,
    UNKNOWN
}

// =====================================================
// BUILD MODELS
// =====================================================

/**
 * Build status enum.
 */
enum class BuildStatus {
    IDLE,
    QUEUED,
    BUILDING,
    SUCCESS,
    FAILED,
    CANCELLED
}

/**
 * Build configuration.
 */
data class BuildConfig(
    val projectId: String,
    val buildType: BuildType = BuildType.DEBUG,
    val productFlavor: String? = null,
    val minifyEnabled: Boolean = false,
    val shrinkResources: Boolean = false,
    val signingConfig: SigningConfigData? = null
)

/**
 * Build types.
 */
enum class BuildType {
    DEBUG,
    RELEASE
}

/**
 * Signing configuration.
 */
data class SigningConfigData(
    val keystorePath: String,
    val keystorePassword: String,
    val keyAlias: String,
    val keyPassword: String,
    val v1SigningEnabled: Boolean = true,
    val v2SigningEnabled: Boolean = true
)

/**
 * Build result.
 */
data class BuildResult(
    val projectId: String,
    val status: BuildStatus,
    val outputApkPath: String? = null,
    val buildTime: Long = 0,
    val apkSize: Long = 0,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

// =====================================================
// APK MODELS
// =====================================================

/**
 * APK information.
 */
@Parcelize
data class ApkInfo(
    val filePath: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val applicationLabel: String,
    val iconPath: String?,
    val permissions: List<String>,
    val activities: List<String>,
    val services: List<String>,
    val receivers: List<String>,
    val providers: List<String>,
    val fileSize: Long,
    val lastModified: Long
) : Parcelable

/**
 * APK entry (file within APK).
 */
@Parcelize
data class ApkEntry(
    val name: String,
    val path: String,
    val size: Long,
    val compressedSize: Long,
    val isDirectory: Boolean,
    val crc: Long
) : Parcelable

/**
 * DEX class information.
 */
data class DexClassInfo(
    val className: String,
    val superClassName: String,
    val interfaces: List<String>,
    val accessFlags: Int,
    val methods: List<MethodInfo>,
    val fields: List<FieldInfo>
)

/**
 * Method information.
 */
data class MethodInfo(
    val name: String,
    val returnType: String,
    val parameters: List<String>,
    val accessFlags: Int
)

/**
 * Field information.
 */
data class FieldInfo(
    val name: String,
    val type: String,
    val accessFlags: Int
)

// =====================================================
// TERMINAL MODELS
// =====================================================

/**
 * Terminal session.
 */
data class TerminalSession(
    val id: String,
    val name: String,
    val workingDirectory: String,
    val shellPath: String,
    val createdAt: Long,
    val environment: Map<String, String>
)

/**
 * Terminal command history.
 */
@Entity(tableName = "terminal_history")
data class TerminalHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val command: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long
)

// =====================================================
// BLOCK EDITOR MODELS
// =====================================================

/**
 * Block definition.
 */
@Parcelize
data class BlockDefinition(
    val id: String,
    val type: BlockType,
    val category: BlockCategory,
    val label: String,
    val color: String,
    val inputs: List<BlockInput>,
    val outputs: List<BlockOutput>,
    val code: String // Template for code generation
) : Parcelable

/**
 * Block types.
 */
enum class BlockType {
    EVENT,      // Triggers (when button clicked, etc.)
    CONTROL,    // Control flow (if, for, while)
    OPERATION,  // Operations (set, get, calculate)
    VARIABLE,   // Variables
    FUNCTION,   // Custom functions
    COMPONENT,  // UI components
    LOGIC,      // Logic operations (and, or, not)
    MATH,       // Math operations
    TEXT,       // Text operations
    LIST,       // List operations
    COLOR       // Color picker
}

/**
 * Block categories.
 */
enum class BlockCategory {
    EVENTS,
    CONTROL,
    VARIABLES,
    FUNCTIONS,
    COMPONENTS,
    LOGIC,
    MATH,
    TEXT,
    LISTS,
    COLORS,
    SENSORS,
    MEDIA
}

/**
 * Block input definition.
 */
@Parcelize
data class BlockInput(
    val name: String,
    val type: InputType,
    val defaultValue: String = "",
    val required: Boolean = true
) : Parcelable

/**
 * Input types.
 */
enum class InputType {
    STRING,
    NUMBER,
    BOOLEAN,
    VARIABLE,
    BLOCK,
    DROPDOWN,
    COLOR,
    COMPONENT
}

/**
 * Block output definition.
 */
@Parcelize
data class BlockOutput(
    val name: String,
    val type: String
) : Parcelable

/**
 * Placed block in workspace.
 */
@Entity(tableName = "workspace_blocks")
data class WorkspaceBlock(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val screenName: String,
    val blockDefinitionId: String,
    val x: Float,
    val y: Float,
    val parentBlockId: String? = null,
    val inputConnections: Map<String, String> = emptyMap(), // inputName -> connectedBlockId
    val values: Map<String, String> = emptyMap(), // inputName -> value
    val order: Int = 0
)

// =====================================================
// AI MODELS
// =====================================================

/**
 * AI suggestion.
 */
data class AiSuggestion(
    val text: String,
    val displayText: String,
    val type: SuggestionType,
    val confidence: Float,
    val range: IntRange
)

/**
 * Suggestion types.
 */
enum class SuggestionType {
    COMPLETION,
    FUNCTION_CALL,
    VARIABLE_NAME,
    IMPORT,
    SNIPPET,
    FIX
}

/**
 * AI analysis result.
 */
data class AiAnalysis(
    val issues: List<CodeIssue>,
    val suggestions: List<String>,
    val complexity: Int,
    val qualityScore: Float,
    val documentation: String?
)

/**
 * Code issue.
 */
data class CodeIssue(
    val message: String,
    val severity: IssueSeverity,
    val line: Int,
    val column: Int,
    val endLine: Int,
    val endColumn: Int,
    val fix: String?,
    val ruleId: String
)

/**
 * Issue severity.
 */
enum class IssueSeverity {
    ERROR,
    WARNING,
    INFO,
    HINT
}

// =====================================================
// SETTINGS MODELS
// =====================================================

/**
 * Editor settings.
 */
data class EditorSettings(
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val showLineNumbers: Boolean = true,
    val highlightCurrentLine: Boolean = true,
    val wordWrap: Boolean = false,
    val autoSave: Boolean = true,
    val autoSaveInterval: Long = 30000,
    val codeCompletion: Boolean = true,
    val syntaxHighlighting: Boolean = true,
    val autoIndent: Boolean = true,
    val bracketMatching: Boolean = true
)

/**
 * Build settings.
 */
data class BuildSettings(
    val outputDirectory: String,
    val autoSign: Boolean = false,
    val defaultKeystorePath: String? = null,
    val buildCacheEnabled: Boolean = true,
    val parallelBuild: Boolean = true,
    val offlineMode: Boolean = false
)

/**
 * AI settings.
 */
data class AiSettings(
    val enabled: Boolean = true,
    val offlineMode: Boolean = false,
    val autoComplete: Boolean = true,
    val errorHighlighting: Boolean = true,
    val codeFormatting: Boolean = true
)

// =====================================================
// GIT MODELS
// =====================================================

/**
 * Git status.
 */
data class GitStatus(
    val branch: String,
    val ahead: Int,
    val behind: Int,
    val staged: List<String>,
    val unstaged: List<String>,
    val untracked: List<String>,
    val conflicts: List<String>
)

/**
 * Git commit.
 */
data class GitCommit(
    val id: String,
    val message: String,
    val author: String,
    val email: String,
    val timestamp: Long,
    val parentIds: List<String>
)

/**
 * Git branch.
 */
data class GitBranch(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean,
    val trackingBranch: String?,
    val lastCommitId: String
)

// =====================================================
// FILE MANAGER MODELS
// =====================================================

/**
 * File item for file manager.
 */
@Parcelize
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isHidden: Boolean,
    val mimeType: String?,
    val extension: String
) : Parcelable

/**
 * Storage info.
 */
data class StorageInfo(
    val totalSpace: Long,
    val freeSpace: Long,
    val usedSpace: Long,
    val path: String,
    val isRemovable: Boolean,
    val isPrimary: Boolean
)
