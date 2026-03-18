/**
 * Android Development Suite - Project Entity
 * منصة تطوير أندرويد الشاملة
 * 
 * Room database entity for storing project information
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.Embedded
import androidx.room.Relation

/**
 * Project entity - represents an Android project.
 */
@Entity(
    tableName = "projects",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["packageName"], unique = true)
    ]
)
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    
    val name: String,
    val path: String,
    val packageName: String,
    val description: String = "",
    
    // SDK Configuration
    val minSdk: Int = 26,
    val targetSdk: Int = 35,
    val compileSdk: Int = 35,
    
    // Build Configuration
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val applicationId: String = "",
    
    // Kotlin/Java Configuration
    val useKotlin: Boolean = true,
    val useCompose: Boolean = true,
    val useHilt: Boolean = true,
    val useRoom: Boolean = true,
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    
    // State
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    
    // Build Status
    val lastBuildStatus: BuildStatus = BuildStatus.NOT_BUILT,
    val lastBuildTime: Long? = null,
    val buildError: String? = null,
    
    // Statistics
    val totalFiles: Int = 0,
    val totalLines: Int = 0,
    val totalClasses: Int = 0
)

/**
 * Build status enum.
 */
enum class BuildStatus {
    NOT_BUILT,
    BUILDING,
    SUCCESS,
    FAILED,
    CANCELLED
}

/**
 * File entity - represents a file in a project.
 */
@Entity(
    tableName = "project_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId", "relativePath"], unique = true)]
)
data class ProjectFileEntity(
    @PrimaryKey
    val id: String,
    
    val projectId: String,
    val relativePath: String,
    val fileName: String,
    val extension: String,
    
    // File type
    val fileType: FileType,
    val language: ProgrammingLanguage = ProgrammingLanguage.KOTLIN,
    
    // Metadata
    val size: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    
    // State
    val isOpen: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val cursorPosition: Int = 0,
    val scrollPosition: Int = 0
)

/**
 * File types.
 */
enum class FileType {
    SOURCE,
    RESOURCE,
    LAYOUT,
    DRAWABLE,
    MANIFEST,
    GRADLE,
    CONFIG,
    ASSET,
    NATIVE,
    OTHER
}

/**
 * Programming languages.
 */
enum class ProgrammingLanguage {
    KOTLIN,
    JAVA,
    XML,
    GRADLE,
    JSON,
    PROGUARD,
    C,
    CPP,
    UNKNOWN
}

/**
 * Build configuration entity.
 */
@Entity(
    tableName = "build_configurations",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BuildConfigurationEntity(
    @PrimaryKey
    val id: String,
    
    val projectId: String,
    val name: String,
    
    // Build type
    val buildType: BuildType = BuildType.DEBUG,
    
    // Signing
    @Embedded
    val signingConfig: SigningConfig? = null,
    
    // ProGuard
    val minifyEnabled: Boolean = false,
    val shrinkResources: Boolean = false,
    val proguardRules: String = "",
    
    // Output
    val outputDirectory: String = "",
    val outputFileName: String = "",
    
    // Custom Gradle properties
    val customProperties: String = "{}"
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
data class SigningConfig(
    val storeFilePath: String,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
    val storeType: String = "JKS"
)

/**
 * Git repository entity.
 */
@Entity(
    tableName = "git_repositories",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GitRepositoryEntity(
    @PrimaryKey
    val id: String,
    
    val projectId: String,
    val remoteUrl: String? = null,
    val branch: String = "main",
    val lastCommitHash: String? = null,
    val lastCommitMessage: String? = null,
    val lastCommitTime: Long? = null,
    val hasUncommittedChanges: Boolean = false,
    val hasUnpushedCommits: Boolean = false
)

/**
 * Recent file entity - for quick access.
 */
@Entity(
    tableName = "recent_files",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class RecentFileEntity(
    @PrimaryKey
    val id: String,
    
    val projectId: String,
    val filePath: String,
    val fileName: String,
    
    val openedAt: Long = System.currentTimeMillis(),
    val pinPosition: Int? = null
)

/**
 * Project with files - Room relation.
 */
data class ProjectWithFiles(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val files: List<ProjectFileEntity>
)

/**
 * Project with git repository - Room relation.
 */
data class ProjectWithGit(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val gitRepository: GitRepositoryEntity?
)
