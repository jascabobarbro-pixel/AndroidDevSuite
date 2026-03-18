/**
 * Android Development Suite - Main Activity
 * منصة تطوير أندرويد الشاملة
 * 
 * Main entry point with Material Design 3 UI
 * Jetpack Compose based interface with bottom navigation
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.androiddevsuite.BuildConfig
import com.androiddevsuite.ui.apk.ApkEditorScreen
import com.androiddevsuite.ui.blocks.BlockEditorScreen
import com.androiddevsuite.ui.code.CodeEditorScreen
import com.androiddevsuite.ui.home.HomeScreen
import com.androiddevsuite.ui.projects.ProjectsScreen
import com.androiddevsuite.ui.settings.SettingsScreen
import com.androiddevsuite.ui.terminal.TerminalScreen
import com.androiddevsuite.ui.theme.AndroidDevSuiteTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main Activity - Entry point for the application.
 * 
 * Features:
 * - Material Design 3 with dynamic colors
 * - Bottom navigation for main tools
 * - Permission handling for storage access
 * - File intent handling for APK and code files
 * - Edge-to-edge display support
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Permission launcher for Android 13+ media permissions
    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Timber.d("Media permissions granted")
            checkManageStoragePermission()
        } else {
            Timber.w("Media permissions denied")
            showPermissionRationale()
        }
    }

    // Permission launcher for manage external storage (MT Manager-like functionality)
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Timber.d("Manage storage permission granted")
            } else {
                Timber.w("Manage storage permission denied")
                showPermissionRationale()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Handle incoming intents (file opens)
        handleIntent(intent)
        
        // Check and request permissions
        checkPermissions()
        
        // Set up the Compose UI
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            
            AndroidDevSuiteTheme(
                darkTheme = uiState.isDarkTheme,
                dynamicColor = uiState.useDynamicColors
            ) {
                MainScreen(
                    uiState = uiState,
                    onToggleTheme = mainViewModel::toggleTheme,
                    onNavigateToTool = mainViewModel::navigateToTool
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handle incoming file intents for APK and code files.
     * Enables opening files from external file managers.
     */
    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            Timber.d("Received intent with URI: $uri")
            when (intent.type) {
                "application/vnd.android.package-archive" -> {
                    // Open APK in editor
                    Timber.d("Opening APK file: ${uri.lastPathSegment}")
                    // Navigate to APK editor with the file
                }
                "text/x-java", "text/x-kotlin" -> {
                    // Open code in editor
                    Timber.d("Opening code file: ${uri.lastPathSegment}")
                    // Navigate to code editor with the file
                }
            }
        }
    }

    /**
     * Check and request necessary permissions based on Android version.
     */
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ requires MANAGE_EXTERNAL_STORAGE for full file access
            checkManageStoragePermission()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            checkMediaPermissions()
        } else {
            // Legacy storage permissions
            checkLegacyStoragePermissions()
        }
    }

    /**
     * Check manage external storage permission for Android 11+.
     * Required for MT Manager-like file operations.
     */
    private fun checkManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                manageStorageLauncher.launch(intent)
            }
        }
    }

    /**
     * Check media permissions for Android 13+.
     */
    private fun checkMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            
            val needsRequest = permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            
            if (needsRequest) {
                mediaPermissionLauncher.launch(permissions)
            } else {
                checkManageStoragePermission()
            }
        }
    }

    /**
     * Check legacy storage permissions for Android 10 and below.
     */
    private fun checkLegacyStoragePermissions() {
        // Legacy permissions are handled differently
        Timber.d("Using legacy storage permissions")
    }

    /**
     * Show rationale dialog for denied permissions.
     */
    private fun showPermissionRationale() {
        // In production, show a dialog explaining why permissions are needed
        Timber.w("Permission rationale should be shown")
    }
}

/**
 * Navigation items for bottom navigation bar.
 * Each item represents a main tool in the suite.
 */
sealed class NavigationItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val iconFilled: ImageVector
) {
    data object Home : NavigationItem(
        route = "home",
        titleRes = R.string.nav_home,
        icon = androidx.compose.material.icons.Icons.Outlined.Home,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Home
    )
    
    data object Projects : NavigationItem(
        route = "projects",
        titleRes = R.string.nav_projects,
        icon = androidx.compose.material.icons.Icons.Outlined.Folder,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Folder
    )
    
    data object CodeEditor : NavigationItem(
        route = "code_editor",
        titleRes = R.string.nav_code,
        icon = androidx.compose.material.icons.Icons.Outlined.Code,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Code
    )
    
    data object BlockEditor : NavigationItem(
        route = "block_editor",
        titleRes = R.string.nav_blocks,
        icon = androidx.compose.material.icons.Icons.Outlined.Extension,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Extension
    )
    
    data object Terminal : NavigationItem(
        route = "terminal",
        titleRes = R.string.nav_terminal,
        icon = androidx.compose.material.icons.Icons.Outlined.Terminal,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Terminal
    )
    
    data object ApkEditor : NavigationItem(
        route = "apk_editor",
        titleRes = R.string.nav_apk,
        icon = androidx.compose.material.icons.Icons.Outlined.Android,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Android
    )
    
    data object Settings : NavigationItem(
        route = "settings",
        titleRes = R.string.nav_settings,
        icon = androidx.compose.material.icons.Icons.Outlined.Settings,
        iconFilled = androidx.compose.material.icons.Icons.Filled.Settings
    )
}

/**
 * Main Compose screen with bottom navigation and content area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onToggleTheme: () -> Unit,
    onNavigateToTool: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val navigationItems = listOf(
        NavigationItem.Home,
        NavigationItem.Projects,
        NavigationItem.CodeEditor,
        NavigationItem.BlockEditor,
        NavigationItem.Terminal,
        NavigationItem.ApkEditor
    )
    
    val bottomBarVisible = remember(currentDestination) {
        navigationItems.any { it.route == currentDestination?.route }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Android Dev Suite",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Theme toggle
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) {
                                androidx.compose.material.icons.Icons.Outlined.LightMode
                            } else {
                                androidx.compose.material.icons.Icons.Outlined.DarkMode
                            },
                            contentDescription = "Toggle theme"
                        )
                    }
                    
                    // Settings
                    IconButton(
                        onClick = { navController.navigate(NavigationItem.Settings.route) }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (bottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    navigationItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.iconFilled else item.icon,
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    text = getString(item.titleRes),
                                    maxLines = 1
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = {
            // Snackbar for showing messages
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationItem.Home.route) {
                HomeScreen(
                    onNavigateToProject = { navController.navigate(NavigationItem.Projects.route) },
                    onNavigateToCodeEditor = { navController.navigate(NavigationItem.CodeEditor.route) },
                    onNavigateToBlockEditor = { navController.navigate(NavigationItem.BlockEditor.route) },
                    onNavigateToTerminal = { navController.navigate(NavigationItem.Terminal.route) },
                    onNavigateToApkEditor = { navController.navigate(NavigationItem.ApkEditor.route) }
                )
            }
            
            composable(NavigationItem.Projects.route) {
                ProjectsScreen(
                    onProjectClick = { projectId ->
                        // Navigate to project editor
                    },
                    onCreateProject = {
                        // Show create project dialog
                    }
                )
            }
            
            composable(NavigationItem.CodeEditor.route) {
                CodeEditorScreen(
                    onOpenFile = { uri ->
                        // Handle file open
                    }
                )
            }
            
            composable(NavigationItem.BlockEditor.route) {
                BlockEditorScreen(
                    onExportCode = { code ->
                        // Export generated code
                    }
                )
            }
            
            composable(NavigationItem.Terminal.route) {
                TerminalScreen(
                    onCommandExecute = { command ->
                        // Execute terminal command
                    }
                )
            }
            
            composable(NavigationItem.ApkEditor.route) {
                ApkEditorScreen(
                    onApkOpen = { uri ->
                        // Open APK for editing
                    }
                )
            }
            
            composable(NavigationItem.Settings.route) {
                SettingsScreen(
                    onThemeChange = onToggleTheme,
                    currentTheme = if (uiState.isDarkTheme) "dark" else "light"
                )
            }
        }
    }
}

/**
 * UI State for the main screen.
 */
data class MainUiState(
    val isDarkTheme: Boolean = true,
    val useDynamicColors: Boolean = true,
    val currentTool: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
