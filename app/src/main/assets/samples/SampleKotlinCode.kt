# Android Dev Suite - Sample Kotlin Code
# This file contains sample Kotlin code for testing the code editor

package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =====================================================
// DATA CLASSES
// =====================================================

/**
 * User data class representing a user in the system.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Project data class representing a development project.
 */
data class Project(
    val id: String,
    val name: String,
    val description: String,
    val language: String,
    val stars: Int,
    val forks: Int,
    val owner: User
)

/**
 * Repository data class for Git repositories.
 */
data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val htmlUrl: String,
    val cloneUrl: String,
    val sshUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val defaultBranch: String,
    val owner: User
)

// =====================================================
// SEAL CLASSES
// =====================================================

/**
 * UI state sealed class for handling different states.
 */
sealed class UiState<out T> {
    data class Loading(val message: String = "Loading...") : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/**
 * Navigation destination sealed class.
 */
sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object Projects : NavDestination("projects")
    object Editor : NavDestination("editor/{projectId}") {
        fun createRoute(projectId: String) = "editor/$projectId"
    }
    object Settings : NavDestination("settings")
    object Profile : NavDestination("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
}

// =====================================================
// ENUMS
// =====================================================

/**
 * Programming language enum.
 */
enum class ProgrammingLanguage(
    val displayName: String,
    val extension: String,
    val color: Color
) {
    KOTLIN("Kotlin", "kt", Color(0xFF7F52FF)),
    JAVA("Java", "java", Color(0xFFED8B00)),
    PYTHON("Python", "py", Color(0xFF3572A5)),
    JAVASCRIPT("JavaScript", "js", Color(0xFFF1E05A)),
    TYPESCRIPT("TypeScript", "ts", Color(0xFF3178C6)),
    GO("Go", "go", Color(0xFF00ADD8)),
    RUST("Rust", "rs", Color(0xFFDEA584)),
    CPP("C++", "cpp", Color(0xFFF34B7D)),
    C_SHARP("C#", "cs", Color(0xFF178600)),
    SWIFT("Swift", "swift", Color(0xFFF05138));
    
    companion object {
        fun fromExtension(ext: String): ProgrammingLanguage {
            return values().find { it.extension.equals(ext, ignoreCase = true) } ?: KOTLIN
        }
    }
}

/**
 * Build status enum.
 */
enum class BuildStatus {
    IDLE,
    QUEUED,
    PREPARING,
    COMPILING,
    PACKAGING,
    SIGNING,
    SUCCESS,
    FAILED,
    CANCELLED;
    
    val isRunning: Boolean
        get() = this in listOf(QUEUED, PREPARING, COMPILING, PACKAGING, SIGNING)
    
    val isComplete: Boolean
        get() = this in listOf(SUCCESS, FAILED, CANCELLED)
}

// =====================================================
// VIEWMODEL
// =====================================================

/**
 * Main screen ViewModel.
 */
class MainViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<Project>>>(UiState.Loading())
    val uiState: StateFlow<UiState<List<Project>>> = _uiState.asStateFlow()
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow<ProgrammingLanguage?>(null)
    val selectedLanguage: StateFlow<ProgrammingLanguage?> = _selectedLanguage.asStateFlow()
    
    init {
        loadProjects()
    }
    
    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading()
            try {
                // Simulate loading
                kotlinx.coroutines.delay(1000)
                
                val sampleProjects = listOf(
                    Project(
                        id = "1",
                        name = "AndroidDevSuite",
                        description = "A comprehensive Android development platform",
                        language = "Kotlin",
                        stars = 1500,
                        forks = 320,
                        owner = User("1", "AndroidDevSuite", "contact@androiddevsuite.com")
                    ),
                    Project(
                        id = "2",
                        name = "ComposeUI",
                        description = "Modern UI toolkit for Android",
                        language = "Kotlin",
                        stars = 2500,
                        forks = 450,
                        owner = User("2", "Google", "contact@google.com")
                    ),
                    Project(
                        id = "3",
                        name = "Retrofit",
                        description = "Type-safe HTTP client for Android",
                        language = "Java",
                        stars = 4200,
                        forks = 780,
                        owner = User("3", "Square", "contact@squareup.com")
                    )
                )
                
                _projects.value = sampleProjects
                _uiState.value = UiState.Success(sampleProjects)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error", e)
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSelectedLanguage(language: ProgrammingLanguage?) {
        _selectedLanguage.value = language
    }
    
    fun refresh() {
        loadProjects()
    }
}

// =====================================================
// COMPOSABLES
// =====================================================

/**
 * Main screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Dev Suite") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Project") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search projects...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            // Content
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(projects.size) { index ->
                            ProjectCard(project = projects[index])
                        }
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is UiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No projects found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Project card composable.
 */
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                AssistChip(
                    onClick = { },
                    label = { Text(project.language) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " ${project.stars}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.Default.CallSplit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " ${project.forks}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Placeholder for LazyColumn
@Composable
fun LazyColumn(
    modifier: Modifier,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier)
}

// =====================================================
// EXTENSION FUNCTIONS
// =====================================================

/**
 * Extension function to format file size.
 */
fun Long.formatFileSize(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> String.format("%.1f KB", this / 1024.0)
        this < 1024 * 1024 * 1024 -> String.format("%.1f MB", this / (1024.0 * 1024))
        else -> String.format("%.1f GB", this / (1024.0 * 1024 * 1024))
    }
}

/**
 * Extension function to capitalize first letter.
 */
fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
}

/**
 * Extension function to check if string is a valid email.
 */
fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return matches(emailRegex)
}

/**
 * Extension function to truncate string.
 */
fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) this
    else take(maxLength - suffix.length) + suffix
}

// =====================================================
// SINGLETONS
// =====================================================

/**
 * App configuration singleton.
 */
object AppConfig {
    const val APP_NAME = "Android Dev Suite"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
    const val MIN_SDK = 26
    const val TARGET_SDK = 35
    
    var isDebugMode = false
    var isDarkTheme = true
    var isDynamicColors = true
    
    fun initialize() {
        // Initialize app configuration
    }
}

/**
 * Logger singleton.
 */
object Logger {
    private const val TAG = "AndroidDevSuite"
    
    fun d(message: String) {
        if (AppConfig.isDebugMode) {
            println("[$TAG] DEBUG: $message")
        }
    }
    
    fun i(message: String) {
        println("[$TAG] INFO: $message")
    }
    
    fun w(message: String) {
        println("[$TAG] WARN: $message")
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        println("[$TAG] ERROR: $message")
        throwable?.printStackTrace()
    }
}
