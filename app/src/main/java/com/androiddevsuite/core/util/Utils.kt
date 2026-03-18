/**
 * Android Development Suite - Core Utilities
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.core.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * File utilities.
 */
object FileUtils {
    
    /**
     * Get file extension from path.
     */
    fun getExtension(path: String): String {
        return path.substringAfterLast('.', "")
    }
    
    /**
     * Get file name from path.
     */
    fun getFileName(path: String): String {
        return path.substringAfterLast('/')
    }
    
    /**
     * Get file name without extension.
     */
    fun getFileNameWithoutExtension(path: String): String {
        val name = getFileName(path)
        return name.substringBeforeLast('.')
    }
    
    /**
     * Read file content as string.
     */
    suspend fun readFileAsString(file: File): String = withContext(Dispatchers.IO) {
        file.readText()
    }
    
    /**
     * Write string to file.
     */
    suspend fun writeStringToFile(file: File, content: String) = withContext(Dispatchers.IO) {
        file.writeText(content)
    }
    
    /**
     * Copy stream to file.
     */
    fun copyStreamToFile(inputStream: InputStream, file: File) {
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
    }
    
    /**
     * Get file size in human-readable format.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
    
    /**
     * Get MIME type from file extension.
     */
    fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "kt", "kts" -> "text/x-kotlin"
            "java" -> "text/x-java"
            "xml" -> "text/xml"
            "json" -> "application/json"
            "gradle" -> "text/x-gradle"
            "txt", "md" -> "text/plain"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "text/javascript"
            "apk" -> "application/vnd.android.package-archive"
            "zip", "jar", "aar" -> "application/zip"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Calculate MD5 hash of file.
     */
    suspend fun calculateMD5(file: File): String = withContext(Dispatchers.IO) {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } > 0) {
                md.update(buffer, 0, read)
            }
        }
        BigInteger(1, md.digest()).toString(16).padStart(32, '0')
    }
    
    /**
     * Calculate SHA-256 hash of file.
     */
    suspend fun calculateSHA256(file: File): String = withContext(Dispatchers.IO) {
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } > 0) {
                md.update(buffer, 0, read)
            }
        }
        BigInteger(1, md.digest()).toString(16).padStart(64, '0')
    }
    
    /**
     * Zip files.
     */
    suspend fun zipFiles(files: List<File>, outputFile: File) = withContext(Dispatchers.IO) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
            files.forEach { file ->
                if (file.exists()) {
                    zos.putNextEntry(ZipEntry(file.name))
                    FileInputStream(file).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
    }
    
    /**
     * Unzip file.
     */
    suspend fun unzipFile(zipFile: File, destDir: File) = withContext(Dispatchers.IO) {
        if (!destDir.exists()) destDir.mkdirs()
        
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val file = File(destDir, entry!!.name)
                if (entry!!.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        zis.copyTo(fos)
                    }
                }
            }
        }
    }
}

/**
 * Text utilities.
 */
object TextUtils {
    
    /**
     * Capitalize first letter.
     */
    fun capitalize(str: String): String {
        if (str.isEmpty()) return str
        return str.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Convert camelCase to snake_case.
     */
    fun camelToSnake(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])")) { 
            "${it.groupValues[1]}_${it.groupValues[2]}" 
        }.lowercase()
    }
    
    /**
     * Convert snake_case to camelCase.
     */
    fun snakeToCamel(str: String): String {
        return str.split("_").mapIndexed { index, s ->
            if (index == 0) s else s.replaceFirstChar { it.uppercase() }
        }.joinToString("")
    }
    
    /**
     * Indent text.
     */
    fun indent(text: String, spaces: Int = 4): String {
        val indentStr = " ".repeat(spaces)
        return text.lines().joinToString("\n") { indentStr + it }
    }
    
    /**
     * Truncate text.
     */
    fun truncate(text: String, maxLength: Int, suffix: String = "..."): String {
        return if (text.length <= maxLength) text
        else text.take(maxLength - suffix.length) + suffix
    }
    
    /**
     * Count lines.
     */
    fun countLines(text: String): Int = text.count { it == '\n' } + 1
    
    /**
     * Count words.
     */
    fun countWords(text: String): Int = text.split(Regex("\\s+")).count { it.isNotBlank() }
    
    /**
     * Count characters (excluding whitespace).
     */
    fun countCharacters(text: String): Int = text.count { !it.isWhitespace() }
}

/**
 * Date utilities.
 */
object DateUtils {
    
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    private val fileFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    /**
     * Format timestamp for display.
     */
    fun formatDisplay(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }
    
    /**
     * Format timestamp for ISO 8601.
     */
    fun formatISO(timestamp: Long): String {
        return isoFormat.format(Date(timestamp))
    }
    
    /**
     * Format timestamp for file name.
     */
    fun formatForFile(timestamp: Long = System.currentTimeMillis()): String {
        return fileFormat.format(Date(timestamp))
    }
    
    /**
     * Parse ISO 8601 date.
     */
    fun parseISO(dateStr: String): Long {
        return isoFormat.parse(dateStr)?.time ?: 0L
    }
    
    /**
     * Get relative time string.
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> formatDisplay(timestamp)
        }
    }
}

/**
 * Package utilities.
 */
object PackageUtils {
    
    /**
     * Get package info.
     */
    fun getPackageInfo(context: Context, packageName: String): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    /**
     * Get app version name.
     */
    fun getVersionName(context: Context): String {
        return getPackageInfo(context, context.packageName)?.versionName ?: "Unknown"
    }
    
    /**
     * Get app version code.
     */
    fun getVersionCode(context: Context): Long {
        val info = getPackageInfo(context, context.packageName) ?: return 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }
    
    /**
     * Check if app is installed.
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Get app icon as bitmap.
     */
    fun getAppIcon(context: Context, packageName: String): Bitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

/**
 * Permission utilities.
 */
object PermissionUtils {
    
    val DANGEROUS_PERMISSIONS = listOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.WRITE_CONTACTS,
        android.Manifest.permission.READ_PHONE_STATE
    )
}

/**
 * Theme utilities.
 */
object ThemeUtils {
    
    /**
     * Get attribute color.
     */
    fun getAttributeColor(context: Context, attrId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrId, typedValue, true)
        return typedValue.data
    }
    
    /**
     * Get color from resource.
     */
    fun getColor(context: Context, colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }
}

/**
 * Uri utilities.
 */
object UriUtils {
    
    /**
     * Get file name from content URI.
     */
    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
    
    /**
     * Get file size from content URI.
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }
}
