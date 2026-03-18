/**
 * Android Development Suite - Project DAO
 * منصة تطوير أندرويد الشاملة
 * 
 * Room DAO for project operations
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Project Data Access Object.
 */
@Dao
interface ProjectDao {
    
    // =====================================================
    // PROJECT QUERIES
    // =====================================================
    
    @Query("SELECT * FROM projects ORDER BY lastOpenedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>
    
    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY lastOpenedAt DESC")
    fun getActiveProjects(): Flow<List<ProjectEntity>>
    
    @Query("SELECT * FROM projects WHERE isFavorite = 1 ORDER BY lastOpenedAt DESC")
    fun getFavoriteProjects(): Flow<List<ProjectEntity>>
    
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): ProjectEntity?
    
    @Query("SELECT * FROM projects WHERE path = :path")
    suspend fun getProjectByPath(path: String): ProjectEntity?
    
    @Query("SELECT * FROM projects WHERE packageName = :packageName")
    suspend fun getProjectByPackageName(packageName: String): ProjectEntity?
    
    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>
    
    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectWithFiles(projectId: String): Flow<ProjectWithFiles?>
    
    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectWithFilesOnce(projectId: String): ProjectWithFiles?
    
    // =====================================================
    // PROJECT INSERT/UPDATE/DELETE
    // =====================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)
    
    @Update
    suspend fun updateProject(project: ProjectEntity)
    
    @Delete
    suspend fun deleteProject(project: ProjectEntity)
    
    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)
    
    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()
    
    // =====================================================
    // PROJECT STATE UPDATES
    // =====================================================
    
    @Query("UPDATE projects SET lastOpenedAt = :timestamp WHERE id = :projectId")
    suspend fun updateLastOpened(projectId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE projects SET isFavorite = :isFavorite WHERE id = :projectId")
    suspend fun updateFavorite(projectId: String, isFavorite: Boolean)
    
    @Query("UPDATE projects SET isArchived = :isArchived WHERE id = :projectId")
    suspend fun updateArchived(projectId: String, isArchived: Boolean)
    
    @Query("UPDATE projects SET hasUnsavedChanges = :hasChanges WHERE id = :projectId")
    suspend fun updateUnsavedChanges(projectId: String, hasChanges: Boolean)
    
    @Query("UPDATE projects SET lastBuildStatus = :status, lastBuildTime = :time, buildError = :error WHERE id = :projectId")
    suspend fun updateBuildStatus(projectId: String, status: BuildStatus, time: Long?, error: String?)
    
    @Query("UPDATE projects SET totalFiles = :totalFiles, totalLines = :totalLines, totalClasses = :totalClasses WHERE id = :projectId")
    suspend fun updateStatistics(projectId: String, totalFiles: Int, totalLines: Int, totalClasses: Int)
    
    // =====================================================
    // FILE QUERIES
    // =====================================================
    
    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY relativePath")
    fun getProjectFiles(projectId: String): Flow<List<ProjectFileEntity>>
    
    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND fileType = :fileType")
    fun getProjectFilesByType(projectId: String, fileType: FileType): Flow<List<ProjectFileEntity>>
    
    @Query("SELECT * FROM project_files WHERE id = :fileId")
    suspend fun getFileById(fileId: String): ProjectFileEntity?
    
    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND relativePath = :relativePath")
    suspend fun getFileByPath(projectId: String, relativePath: String): ProjectFileEntity?
    
    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND fileName LIKE '%' || :query || '%'")
    fun searchFiles(projectId: String, query: String): Flow<List<ProjectFileEntity>>
    
    @Query("SELECT * FROM project_files WHERE isOpen = 1 ORDER BY modifiedAt DESC")
    fun getOpenFiles(): Flow<List<ProjectFileEntity>>
    
    // =====================================================
    // FILE INSERT/UPDATE/DELETE
    // =====================================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFileEntity>)
    
    @Update
    suspend fun updateFile(file: ProjectFileEntity)
    
    @Delete
    suspend fun deleteFile(file: ProjectFileEntity)
    
    @Query("DELETE FROM project_files WHERE id = :fileId")
    suspend fun deleteFileById(fileId: String)
    
    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteProjectFiles(projectId: String)
    
    // =====================================================
    // FILE STATE UPDATES
    // =====================================================
    
    @Query("UPDATE project_files SET isOpen = :isOpen WHERE id = :fileId")
    suspend fun updateFileOpenState(fileId: String, isOpen: Boolean)
    
    @Query("UPDATE project_files SET hasUnsavedChanges = :hasChanges WHERE id = :fileId")
    suspend fun updateFileUnsavedChanges(fileId: String, hasChanges: Boolean)
    
    @Query("UPDATE project_files SET cursorPosition = :position, scrollPosition = :scroll WHERE id = :fileId")
    suspend fun updateFilePosition(fileId: String, position: Int, scroll: Int)
    
    @Query("UPDATE project_files SET isOpen = 0")
    suspend fun closeAllFiles()
    
    // =====================================================
    // BUILD CONFIGURATION
    // =====================================================
    
    @Query("SELECT * FROM build_configurations WHERE projectId = :projectId")
    fun getBuildConfigurations(projectId: String): Flow<List<BuildConfigurationEntity>>
    
    @Query("SELECT * FROM build_configurations WHERE id = :configId")
    suspend fun getBuildConfiguration(configId: String): BuildConfigurationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuildConfiguration(config: BuildConfigurationEntity)
    
    @Update
    suspend fun updateBuildConfiguration(config: BuildConfigurationEntity)
    
    @Delete
    suspend fun deleteBuildConfiguration(config: BuildConfigurationEntity)
    
    // =====================================================
    // GIT REPOSITORY
    // =====================================================
    
    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectWithGit(projectId: String): Flow<ProjectWithGit?>
    
    @Query("SELECT * FROM git_repositories WHERE projectId = :projectId")
    suspend fun getGitRepository(projectId: String): GitRepositoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGitRepository(repo: GitRepositoryEntity)
    
    @Update
    suspend fun updateGitRepository(repo: GitRepositoryEntity)
    
    @Delete
    suspend fun deleteGitRepository(repo: GitRepositoryEntity)
    
    // =====================================================
    // RECENT FILES
    // =====================================================
    
    @Query("SELECT * FROM recent_files WHERE projectId = :projectId ORDER BY openedAt DESC LIMIT :limit")
    fun getRecentFiles(projectId: String, limit: Int = 20): Flow<List<RecentFileEntity>>
    
    @Query("SELECT * FROM recent_files ORDER BY openedAt DESC LIMIT :limit")
    fun getAllRecentFiles(limit: Int = 50): Flow<List<RecentFileEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentFile(file: RecentFileEntity)
    
    @Query("DELETE FROM recent_files WHERE openedAt < :olderThan")
    suspend fun deleteOldRecentFiles(olderThan: Long)
    
    @Query("DELETE FROM recent_files WHERE projectId = :projectId")
    suspend fun deleteProjectRecentFiles(projectId: String)
    
    // =====================================================
    // STATISTICS
    // =====================================================
    
    @Query("SELECT COUNT(*) FROM projects WHERE isArchived = 0")
    fun getActiveProjectCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM project_files WHERE projectId = :projectId")
    fun getFileCount(projectId: String): Flow<Int>
    
    @Query("SELECT SUM(totalLines) FROM projects")
    fun getTotalLinesOfCode(): Flow<Int?>
}

/**
 * Project Repository - combines DAO operations with business logic.
 */
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getActiveProjects()
    val favoriteProjects: Flow<List<ProjectEntity>> = projectDao.getFavoriteProjects()
    
    fun getProject(projectId: String): Flow<ProjectEntity?> {
        return projectDao.getProjectWithFiles(projectId)
            .let { flow ->
                kotlinx.coroutines.flow.map { it?.project }
            }
    }
    
    suspend fun createProject(project: ProjectEntity): Result<ProjectEntity> {
        return try {
            projectDao.insertProject(project)
            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProject(project: ProjectEntity): Result<Unit> {
        return try {
            projectDao.updateProject(project)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            projectDao.deleteProjectById(projectId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(projectId: String) {
        val project = projectDao.getProjectById(projectId)
        project?.let {
            projectDao.updateFavorite(projectId, !it.isFavorite)
        }
    }
    
    suspend fun recordProjectOpen(projectId: String) {
        projectDao.updateLastOpened(projectId)
    }
}
