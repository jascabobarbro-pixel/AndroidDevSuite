/**
 * Android Development Suite - File Manager
 * منصة تطوير أندرويد الشاملة
 * 
 * Complete file management system
 */
package com.androiddevsuite.tools.filemanager

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.androiddevsuite.data.model.FileItem
import com.androiddevsuite.data.model.StorageInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * File Manager - Handles all file operations.
 */
@Singleton
class FileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appDir = File(Environment.getExternalStorageDirectory(), "AndroidDevSuite")
    
    /**
     * Initialize app directories.
     */
    fun initializeDirectories() {
        listOf(
            "Projects",
            "Builds",
            "Downloads",
            "Cache",
            "Backups",
            "Templates"
        ).forEach { dirName ->
            File(appDir, dirName).apply {
                if (!exists()) {
                    mkdirs()
                    Timber.d("Created directory: $absolutePath")
                }
            }
        }
    }
    
    /**
     * Get storage info.
     */
    fun getStorageInfo(): List<StorageInfo> {
        val storageList = mutableListOf<StorageInfo>()
        
        // Internal storage
        val internalStorage = Environment.getDataDirectory()
        storageList.add(getStorageInfoForPath(internalStorage, false, true))
        
        // External storage
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val externalStorage = Environment.getExternalStorageDirectory()
            storageList.add(getStorageInfoForPath(externalStorage, false, false))
        }
        
        // Removable storage (SD cards)
        context.getExternalFilesDirs(null)
            ?.filterNotNull()
            ?.filter { it.absolutePath != context.getExternalFilesDir(null)?.absolutePath }
            ?.forEach { file ->
                storageList.add(getStorageInfoForPath(file, true, false))
            }
        
        return storageList
    }
    
    private fun getStorageInfoForPath(path: File, isRemovable: Boolean, isPrimary: Boolean): StorageInfo {
        val stat = StatFs(path.path)
        val totalSpace = stat.totalBytes
        val freeSpace = stat.availableBytes
        
        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = totalSpace - freeSpace,
            path = path.absolutePath,
            isRemovable = isRemovable,
            isPrimary = isPrimary
        )
    }
    
    /**
     * List files in directory.
     */
    suspend fun listFiles(
        directory: String,
        showHidden: Boolean = false,
        sortBy: SortBy = SortBy.NAME
    ): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val dir = File(directory)
            if (!dir.exists() || !dir.isDirectory) {
                return@withContext Result.failure(Exception("Directory not found: $directory"))
            }
            
            val files = dir.listFiles()
                ?.filter { showHidden || !it.name.startsWith(".") }
                ?.map { file ->
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isFile) file.length() else 0,
                        lastModified = file.lastModified(),
                        isHidden = file.name.startsWith("."),
                        mimeType = getMimeType(file),
                        extension = file.extension
                    )
                }
                ?.sortedWith(getComparator(sortBy))
                ?: emptyList()
            
            // Put directories first
            val sorted = files.sortedByDescending { it.isDirectory }
            
            Result.success(sorted)
        } catch (e: Exception) {
            Timber.e(e, "Failed to list files: $directory")
            Result.failure(e)
        }
    }
    
    /**
     * Create directory.
     */
    suspend fun createDirectory(path: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dir = File(path)
            if (dir.exists()) {
                return@withContext Result.failure(Exception("Directory already exists"))
            }
            
            if (dir.mkdirs()) {
                Timber.d("Created directory: $path")
                Result.success(dir)
            } else {
                Result.failure(Exception("Failed to create directory"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create directory: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Create file.
     */
    suspend fun createFile(path: String, content: String = ""): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                return@withContext Result.failure(Exception("File already exists"))
            }
            
            file.parentFile?.mkdirs()
            file.writeText(content)
            
            Timber.d("Created file: $path")
            Result.success(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create file: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Read file content.
     */
    suspend fun readFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found: $path"))
            }
            
            val content = file.readText()
            Result.success(content)
        } catch (e: Exception) {
            Timber.e(e, "Failed to read file: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Write to file.
     */
    suspend fun writeFile(path: String, content: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            
            Timber.d("Wrote to file: $path")
            Result.success(file)
        } catch (e: Exception) {
            Timber.e(e, "Failed to write file: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Copy file or directory.
     */
    suspend fun copy(source: String, destination: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(source)
            val destFile = File(destination)
            
            if (!srcFile.exists()) {
                return@withContext Result.failure(Exception("Source not found: $source"))
            }
            
            destFile.parentFile?.mkdirs()
            
            if (srcFile.isDirectory) {
                FileUtils.copyDirectory(srcFile, destFile)
            } else {
                FileUtils.copyFile(srcFile, destFile)
            }
            
            Timber.d("Copied: $source -> $destination")
            Result.success(destFile)
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy: $source -> $destination")
            Result.failure(e)
        }
    }
    
    /**
     * Move file or directory.
     */
    suspend fun move(source: String, destination: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(source)
            val destFile = File(destination)
            
            if (!srcFile.exists()) {
                return@withContext Result.failure(Exception("Source not found: $source"))
            }
            
            destFile.parentFile?.mkdirs()
            
            if (srcFile.renameTo(destFile)) {
                Timber.d("Moved: $source -> $destination")
                Result.success(destFile)
            } else {
                // Try copy then delete
                if (srcFile.isDirectory) {
                    FileUtils.copyDirectory(srcFile, destFile)
                    FileUtils.deleteDirectory(srcFile)
                } else {
                    FileUtils.copyFile(srcFile, destFile)
                    srcFile.delete()
                }
                Result.success(destFile)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to move: $source -> $destination")
            Result.failure(e)
        }
    }
    
    /**
     * Delete file or directory.
     */
    suspend fun delete(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found: $path"))
            }
            
            val success = if (file.isDirectory) {
                FileUtils.deleteDirectory(file)
            } else {
                file.delete()
            }
            
            if (success) {
                Timber.d("Deleted: $path")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete: $path"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Rename file or directory.
     */
    suspend fun rename(path: String, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            val newFile = File(file.parentFile, newName)
            
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found: $path"))
            }
            
            if (file.renameTo(newFile)) {
                Timber.d("Renamed: $path -> ${newFile.absolutePath}")
                Result.success(newFile)
            } else {
                Result.failure(Exception("Failed to rename"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to rename: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Compress files/directories to ZIP.
     */
    suspend fun compress(
        sources: List<String>,
        destination: String,
        password: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val zipFile = ZipFile(destination)
            
            if (password != null) {
                zipFile.setPassword(password.toCharArray())
            }
            
            sources.forEach { source ->
                val file = File(source)
                if (file.exists()) {
                    if (file.isDirectory) {
                        zipFile.addFolder(file)
                    } else {
                        zipFile.addFile(file)
                    }
                }
            }
            
            Timber.d("Compressed ${sources.size} items to: $destination")
            Result.success(zipFile.file)
        } catch (e: Exception) {
            Timber.e(e, "Failed to compress to: $destination")
            Result.failure(e)
        }
    }
    
    /**
     * Extract ZIP file.
     */
    suspend fun extract(
        source: String,
        destination: String,
        password: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val zipFile = ZipFile(source)
            
            if (password != null) {
                zipFile.setPassword(password.toCharArray())
            }
            
            zipFile.extractAll(destination)
            
            Timber.d("Extracted: $source -> $destination")
            Result.success(File(destination))
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract: $source")
            Result.failure(e)
        }
    }
    
    /**
     * Search files by name.
     */
    suspend fun search(
        directory: String,
        query: String,
        recursive: Boolean = true
    ): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<FileItem>()
            val dir = File(directory)
            
            fun searchInDir(d: File) {
                d.listFiles()?.forEach { file ->
                    if (file.name.contains(query, ignoreCase = true)) {
                        results.add(FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = if (file.isFile) file.length() else 0,
                            lastModified = file.lastModified(),
                            isHidden = file.name.startsWith("."),
                            mimeType = getMimeType(file),
                            extension = file.extension
                        ))
                    }
                    if (recursive && file.isDirectory) {
                        searchInDir(file)
                    }
                }
            }
            
            searchInDir(dir)
            Result.success(results)
        } catch (e: Exception) {
            Timber.e(e, "Search failed: $query")
            Result.failure(e)
        }
    }
    
    /**
     * Get file hash.
     */
    suspend fun getFileHash(path: String, algorithm: String = "SHA-256"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            val digest = MessageDigest.getInstance(algorithm)
            
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var read: Int
                while (fis.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            Result.success(hash)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get hash: $path")
            Result.failure(e)
        }
    }
    
    /**
     * Get file size.
     */
    fun getFileSize(path: String): Long {
        val file = File(path)
        return if (file.exists()) {
            if (file.isDirectory) {
                FileUtils.sizeOfDirectory(file)
            } else {
                file.length()
            }
        } else {
            0
        }
    }
    
    /**
     * Format file size.
     */
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    private fun getMimeType(file: File): String? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "kt", "kts" -> "text/x-kotlin"
            "java" -> "text/x-java"
            "xml" -> "text/xml"
            "json" -> "application/json"
            "gradle" -> "text/x-gradle"
            "txt", "md" -> "text/plain"
            "apk" -> "application/vnd.android.package-archive"
            "zip", "jar" -> "application/zip"
            "png", "jpg", "jpeg", "gif", "webp" -> "image/*"
            "mp3", "wav", "ogg" -> "audio/*"
            "mp4", "webm", "mkv" -> "video/*"
            else -> null
        }
    }
    
    private fun getComparator(sortBy: SortBy): Comparator<FileItem> {
        return when (sortBy) {
            SortBy.NAME -> compareBy { it.name.lowercase() }
            SortBy.SIZE -> compareByDescending { it.size }
            SortBy.DATE -> compareByDescending { it.lastModified }
            SortBy.TYPE -> compareBy { it.extension }
        }
    }
}

/**
 * Sort options for file listing.
 */
enum class SortBy {
    NAME,
    SIZE,
    DATE,
    TYPE
}
