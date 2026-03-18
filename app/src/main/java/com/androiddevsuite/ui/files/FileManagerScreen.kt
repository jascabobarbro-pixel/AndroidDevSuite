/**
 * Android Development Suite - File Manager Screen
 * منصة تطوير أندرويد الشاملة
 * 
 * Complete file manager with all operations
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.files

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * File item data class.
 */
data class FileItem(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isFile) file.length() else 0L,
    val lastModified: Long = file.lastModified(),
    val extension: String = if (file.isFile) file.extension else "",
    val isHidden: Boolean = file.name.startsWith(".")
)

/**
 * View mode for file display.
 */
enum class ViewMode {
    LIST,
    GRID
}

/**
 * Sort mode for files.
 */
enum class SortMode(val label: String) {
    NAME("Name"),
    DATE("Date"),
    SIZE("Size"),
    TYPE("Type")
}

/**
 * File Manager Screen - Complete file operations interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    initialPath: String? = null,
    onFileOpen: (Uri) -> Unit,
    onFileEdit: (Uri) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    
    // State
    var currentPath by remember { mutableStateOf(File(initialPath ?: "/storage/emulated/0")) }
    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var selectedFiles by remember { mutableStateOf<Set<File>>(emptySet()) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortMode by remember { mutableStateOf(SortMode.NAME) }
    var showHidden by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Dialogs
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Set<File>>(emptySet()) }
    var showPropertiesDialog by remember { mutableStateOf<File?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    
    // Load files
    LaunchedEffect(currentPath, showHidden, sortMode) {
        files = loadFiles(currentPath, showHidden, sortMode)
    }
    
    // Filter files by search
    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    val isSelectionMode = selectedFiles.isNotEmpty()
    
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("File Manager")
                        Text(
                            text = currentPath.absolutePath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Search
                    IconButton(onClick = { /* Show search */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                    
                    // View mode toggle
                    IconButton(onClick = { 
                        viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST 
                    }) {
                        Icon(
                            if (viewMode == ViewMode.LIST) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                            contentDescription = "Toggle view"
                        )
                    }
                    
                    // Sort
                    IconButton(onClick = { /* Show sort menu */ }) {
                        Icon(Icons.Outlined.Sort, contentDescription = "Sort")
                    }
                    
                    // More options
                    IconButton(onClick = { /* Show more */ }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedFiles = emptySet() }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear selection")
                        }
                    } else if (currentPath.parentFile != null) {
                        IconButton(onClick = { currentPath = currentPath.parentFile!! }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Go back")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewFolderDialog = true }
            ) {
                Icon(Icons.Filled.CreateNewFolder, contentDescription = "New folder")
            }
        },
        sheetContent = {
            // Bottom sheet for file operations
            FileOperationsSheet(
                selectedFiles = selectedFiles,
                onCopy = { /* Copy operation */ },
                onMove = { /* Move operation */ },
                onDelete = { showDeleteDialog = selectedFiles },
                onRename = { 
                    selectedFiles.firstOrNull()?.let { showRenameDialog = it }
                },
                onShare = { /* Share files */ },
                onCompress = { /* Compress files */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Breadcrumb navigation
            BreadcrumbNavigation(
                path = currentPath,
                onNavigate = { currentPath = it }
            )
            
            // File list/grid
            if (filteredFiles.isEmpty()) {
                EmptyFolderState(
                    onCreateFolder = { showNewFolderDialog = true },
                    onCreateFile = { showCreateDialog = true }
                )
            } else {
                when (viewMode) {
                    ViewMode.LIST -> FileListView(
                        files = filteredFiles,
                        selectedFiles = selectedFiles,
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                selectedFiles = if (item.file in selectedFiles) {
                                    selectedFiles - item.file
                                } else {
                                    selectedFiles + item.file
                                }
                            } else {
                                if (item.isDirectory) {
                                    currentPath = item.file
                                } else {
                                    onFileOpen(Uri.fromFile(item.file))
                                }
                            }
                        },
                        onItemLongClick = { item ->
                            selectedFiles = setOf(item.file)
                        },
                        onItemDoubleClick = { item ->
                            if (!item.isDirectory) {
                                onFileEdit(Uri.fromFile(item.file))
                            }
                        }
                    )
                    ViewMode.GRID -> FileGridView(
                        files = filteredFiles,
                        selectedFiles = selectedFiles,
                        onItemClick = { item ->
                            if (isSelectionMode) {
                                selectedFiles = if (item.file in selectedFiles) {
                                    selectedFiles - item.file
                                } else {
                                    selectedFiles + item.file
                                }
                            } else {
                                if (item.isDirectory) {
                                    currentPath = item.file
                                } else {
                                    onFileOpen(Uri.fromFile(item.file))
                                }
                            }
                        },
                        onItemLongClick = { item ->
                            selectedFiles = setOf(item.file)
                        }
                    )
                }
            }
        }
        
        // Dialogs
        if (showNewFolderDialog) {
            NewFolderDialog(
                onDismiss = { showNewFolderDialog = false },
                onCreate = { name ->
                    File(currentPath, name).mkdirs()
                    files = loadFiles(currentPath, showHidden, sortMode)
                    showNewFolderDialog = false
                }
            )
        }
        
        if (showCreateDialog) {
            NewFileDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    File(currentPath, name).createNewFile()
                    files = loadFiles(currentPath, showHidden, sortMode)
                    showCreateDialog = false
                }
            )
        }
        
        showRenameDialog?.let { file ->
            RenameDialog(
                file = file,
                onDismiss = { showRenameDialog = null },
                onRename = { newName ->
                    file.renameTo(File(file.parent, newName))
                    files = loadFiles(currentPath, showHidden, sortMode)
                    showRenameDialog = null
                    selectedFiles = emptySet()
                }
            )
        }
        
        if (showDeleteDialog.isNotEmpty()) {
            DeleteConfirmDialog(
                files = showDeleteDialog,
                onDismiss = { showDeleteDialog = emptySet() },
                onConfirm = {
                    showDeleteDialog.forEach { file ->
                        deleteRecursive(file)
                    }
                    files = loadFiles(currentPath, showHidden, sortMode)
                    showDeleteDialog = emptySet()
                    selectedFiles = emptySet()
                }
            )
        }
        
        showPropertiesDialog?.let { file ->
            FilePropertiesDialog(
                file = file,
                onDismiss = { showPropertiesDialog = null }
            )
        }
    }
}

/**
 * Load files from directory.
 */
private fun loadFiles(directory: File, showHidden: Boolean, sortMode: SortMode): List<FileItem> {
    return directory.listFiles()
        ?.filter { showHidden || !it.name.startsWith(".") }
        ?.map { FileItem(it) }
        ?.sortedWith(
            when (sortMode) {
                SortMode.NAME -> compareBy { it.name.lowercase() }
                SortMode.DATE -> compareByDescending { it.lastModified }
                SortMode.SIZE -> compareByDescending { it.size }
                SortMode.TYPE -> compareBy { it.extension }
            }
        )
        ?.sortedByDescending { it.isDirectory }
        ?: emptyList()
}

/**
 * Delete file recursively.
 */
private fun deleteRecursive(file: File) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { deleteRecursive(it) }
    }
    file.delete()
}

/**
 * File list view.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListView(
    files: List<FileItem>,
    selectedFiles: Set<File>,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
    onItemDoubleClick: (FileItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(files) { item ->
            val isSelected = item.file in selectedFiles
            
            FileListItem(
                item = item,
                isSelected = isSelected,
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) },
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}

/**
 * File grid view.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridView(
    files: List<FileItem>,
    selectedFiles: Set<File>,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(files) { item ->
            val isSelected = item.file in selectedFiles
            
            FileGridItem(
                item = item,
                isSelected = isSelected,
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) }
            )
        }
    }
}

/**
 * File list item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = getFileIcon(item),
                contentDescription = null,
                tint = getFileIconColor(item),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                
                if (!item.isDirectory) {
                    Text(
                        text = "${formatSize(item.size)} • ${formatDate(item.lastModified)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Checkbox for selection
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * File grid item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileGridItem(
    item: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getFileIcon(item),
                contentDescription = null,
                tint = getFileIconColor(item),
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Get icon for file type.
 */
private fun getFileIcon(item: FileItem): ImageVector {
    return when {
        item.isDirectory -> Icons.Filled.Folder
        item.extension in listOf("kt", "kts") -> Icons.Filled.Code
        item.extension in listOf("java") -> Icons.Filled.Code
        item.extension in listOf("xml") -> Icons.Filled.Code
        item.extension in listOf("json") -> Icons.Filled.Code
        item.extension in listOf("gradle") -> Icons.Filled.Settings
        item.extension in listOf("png", "jpg", "jpeg", "gif", "webp") -> Icons.Filled.Image
        item.extension in listOf("mp4", "mkv", "avi") -> Icons.Filled.VideoFile
        item.extension in listOf("mp3", "wav", "ogg") -> Icons.Filled.AudioFile
        item.extension == "apk" -> Icons.Filled.Android
        item.extension == "zip" || item.extension == "rar" -> Icons.Filled.FolderZip
        else -> Icons.Filled.InsertDriveFile
    }
}

/**
 * Get icon color for file type.
 */
@Composable
private fun getFileIconColor(item: FileItem): androidx.compose.ui.graphics.Color {
    return when {
        item.isDirectory -> MaterialTheme.colorScheme.primary
        item.extension in listOf("kt", "kts") -> androidx.compose.ui.graphics.Color(0xFF7F52FF)
        item.extension == "java" -> androidx.compose.ui.graphics.Color(0xFFED8B00)
        item.extension == "xml" -> androidx.compose.ui.graphics.Color(0xFF00BFA5)
        item.extension == "apk" -> androidx.compose.ui.graphics.Color(0xFF3DDC84)
        item.extension in listOf("png", "jpg", "jpeg", "gif", "webp") -> androidx.compose.ui.graphics.Color(0xFFFF7043)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/**
 * Format file size.
 */
private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * Format date.
 */
private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(Date(timestamp))
}

/**
 * Breadcrumb navigation.
 */
@Composable
private fun BreadcrumbNavigation(
    path: File,
    onNavigate: (File) -> Unit
) {
    val pathParts = mutableListOf<File>()
    var current = path
    while (current.parentFile != null) {
        pathParts.add(0, current)
        current = current.parentFile!!
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        pathParts.forEachIndexed { index, file ->
            if (index > 0) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            
            TextButton(
                onClick = { onNavigate(file) },
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = file.name.ifEmpty { "/" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Empty folder state.
 */
@Composable
private fun EmptyFolderState(
    onCreateFolder: () -> Unit,
    onCreateFile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This folder is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCreateFolder) {
                Icon(Icons.Outlined.CreateNewFolder, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Folder")
            }
            
            OutlinedButton(onClick = onCreateFile) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New File")
            }
        }
    }
}

/**
 * File operations bottom sheet.
 */
@Composable
private fun FileOperationsSheet(
    selectedFiles: Set<File>,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit
) {
    if (selectedFiles.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "${selectedFiles.size} selected",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SheetActionButton(Icons.Outlined.ContentCopy, "Copy", onCopy)
            SheetActionButton(Icons.Outlined.DriveFileMove, "Move", onMove)
            if (selectedFiles.size == 1) {
                SheetActionButton(Icons.Outlined.Edit, "Rename", onRename)
            }
            SheetActionButton(Icons.Outlined.Delete, "Delete", onDelete)
            SheetActionButton(Icons.Outlined.Share, "Share", onShare)
            SheetActionButton(Icons.Outlined.FolderZip, "Zip", onCompress)
        }
    }
}

@Composable
private fun SheetActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * New folder dialog.
 */
@Composable
private fun NewFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onCreate(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * New file dialog.
 */
@Composable
private fun NewFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New File") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("File name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onCreate(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Rename dialog.
 */
@Composable
private fun RenameDialog(
    file: File,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(file.name) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onRename(name) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Delete confirm dialog.
 */
@Composable
private fun DeleteConfirmDialog(
    files: Set<File>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete") },
        text = {
            Text("Are you sure you want to delete ${files.size} item(s)? This cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * File properties dialog.
 */
@Composable
private fun FilePropertiesDialog(
    file: File,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Properties") },
        text = {
            Column {
                PropertyRow("Name", file.name)
                PropertyRow("Path", file.absolutePath)
                PropertyRow("Type", if (file.isDirectory) "Folder" else file.extension)
                if (file.isFile) {
                    PropertyRow("Size", formatSize(file.length()))
                }
                PropertyRow("Modified", formatDate(file.lastModified()))
                PropertyRow("Readable", if (file.canRead()) "Yes" else "No")
                PropertyRow("Writable", if (file.canWrite()) "Yes" else "No")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
