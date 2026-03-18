/**
 * Android Development Suite - Room Database
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.data.local

import android.content.Context
import androidx.room.*
import com.androiddevsuite.data.local.dao.*
import com.androiddevsuite.data.local.entity.*

/**
 * Main application database.
 */
@Database(
    entities = [
        ProjectEntity::class,
        BuildHistoryEntity::class,
        TerminalHistoryEntity::class,
        WorkspaceBlockEntity::class,
        FileBookmarkEntity::class,
        RecentFileEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun buildHistoryDao(): BuildHistoryDao
    abstract fun terminalHistoryDao(): TerminalHistoryDao
    abstract fun workspaceBlockDao(): WorkspaceBlockDao
    abstract fun fileBookmarkDao(): FileBookmarkDao
    abstract fun recentFileDao(): RecentFileDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "android_dev_suite.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

/**
 * Type converters for Room.
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split("|||") ?: emptyList()
    }
    
    @TypeConverter
    fun toStringList(value: List<String>?): String? {
        return value?.joinToString("|||")
    }
    
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String> {
        if (value == null) return emptyMap()
        return value.split(",")
            .filter { it.contains(":") }
            .associate {
                val (key, v) = it.split(":", limit = 2)
                key to v
            }
    }
    
    @TypeConverter
    fun toStringMap(value: Map<String, String>?): String? {
        return value?.entries?.joinToString(",") { "${it.key}:${it.value}" }
    }
}

// =====================================================
// ENTITIES
// =====================================================

/**
 * Project entity.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val path: String,
    val packageName: String,
    val description: String = "",
    val minSdk: Int = 26,
    val targetSdk: Int = 35,
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val template: String = "EMPTY_ACTIVITY",
    val gitRemote: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

/**
 * Build history entity.
 */
@Entity(tableName = "build_history")
data class BuildHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: String,
    val status: String,
    val outputApkPath: String? = null,
    val buildTime: Long = 0,
    val apkSize: Long = 0,
    val errors: String? = null,
    val warnings: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Terminal history entity.
 */
@Entity(tableName = "terminal_history")
data class TerminalHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val command: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Workspace block entity.
 */
@Entity(tableName = "workspace_blocks")
data class WorkspaceBlockEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val screenName: String,
    val blockDefinitionId: String,
    val x: Float,
    val y: Float,
    val parentBlockId: String? = null,
    val inputConnections: String? = null,
    val values: String? = null,
    val order: Int = 0
)

/**
 * File bookmark entity.
 */
@Entity(tableName = "file_bookmarks")
data class FileBookmarkEntity(
    @PrimaryKey
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Recent file entity.
 */
@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val path: String,
    val name: String,
    val lastOpened: Long = System.currentTimeMillis()
)

// =====================================================
// DAOs
// =====================================================

/**
 * Project DAO.
 */
@Dao
interface ProjectDao {
    
    @Query("SELECT * FROM projects ORDER BY modifiedAt DESC")
    suspend fun getAllProjects(): List<ProjectEntity>
    
    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?
    
    @Query("SELECT * FROM projects WHERE isFavorite = 1 ORDER BY modifiedAt DESC")
    suspend fun getFavoriteProjects(): List<ProjectEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)
    
    @Update
    suspend fun updateProject(project: ProjectEntity)
    
    @Delete
    suspend fun deleteProject(project: ProjectEntity)
    
    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}

/**
 * Build history DAO.
 */
@Dao
interface BuildHistoryDao {
    
    @Query("SELECT * FROM build_history WHERE projectId = :projectId ORDER BY timestamp DESC")
    suspend fun getBuildHistory(projectId: String): List<BuildHistoryEntity>
    
    @Query("SELECT * FROM build_history WHERE projectId = :projectId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastBuild(projectId: String): BuildHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: BuildHistoryEntity)
    
    @Query("DELETE FROM build_history WHERE projectId = :projectId")
    suspend fun deleteBuildHistory(projectId: String)
    
    @Query("DELETE FROM build_history WHERE timestamp < :olderThan")
    suspend fun deleteOldBuilds(olderThan: Long)
}

/**
 * Terminal history DAO.
 */
@Dao
interface TerminalHistoryDao {
    
    @Query("SELECT * FROM terminal_history WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    suspend fun getHistory(sessionId: String): List<TerminalHistoryEntity>
    
    @Query("SELECT * FROM terminal_history WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistory(sessionId: String, limit: Int = 100): List<TerminalHistoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: TerminalHistoryEntity)
    
    @Query("DELETE FROM terminal_history WHERE sessionId = :sessionId")
    suspend fun deleteHistory(sessionId: String)
}

/**
 * Workspace block DAO.
 */
@Dao
interface WorkspaceBlockDao {
    
    @Query("SELECT * FROM workspace_blocks WHERE projectId = :projectId AND screenName = :screenName ORDER BY `order`")
    suspend fun getBlocks(projectId: String, screenName: String): List<WorkspaceBlockEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: WorkspaceBlockEntity)
    
    @Update
    suspend fun updateBlock(block: WorkspaceBlockEntity)
    
    @Delete
    suspend fun deleteBlock(block: WorkspaceBlockEntity)
    
    @Query("DELETE FROM workspace_blocks WHERE projectId = :projectId")
    suspend fun deleteProjectBlocks(projectId: String)
}

/**
 * File bookmark DAO.
 */
@Dao
interface FileBookmarkDao {
    
    @Query("SELECT * FROM file_bookmarks ORDER BY name")
    suspend fun getAllBookmarks(): List<FileBookmarkEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: FileBookmarkEntity)
    
    @Delete
    suspend fun removeBookmark(bookmark: FileBookmarkEntity)
    
    @Query("DELETE FROM file_bookmarks WHERE path = :path")
    suspend fun removeBookmarkByPath(path: String)
}

/**
 * Recent file DAO.
 */
@Dao
interface RecentFileDao {
    
    @Query("SELECT * FROM recent_files ORDER BY lastOpened DESC LIMIT :limit")
    suspend fun getRecentFiles(limit: Int = 20): List<RecentFileEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentFile(file: RecentFileEntity)
    
    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun removeRecentFile(path: String)
    
    @Query("DELETE FROM recent_files")
    suspend fun clearRecentFiles()
}
