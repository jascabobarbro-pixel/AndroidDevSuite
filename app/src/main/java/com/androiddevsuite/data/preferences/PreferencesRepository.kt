/**
 * Android Development Suite - Preferences Repository
 * منصة تطوير أندرويد الشاملة
 * 
 * DataStore-based preferences management
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Application preferences.
 */
data class AppPreferences(
    val isDarkTheme: Boolean = true,
    val useDynamicColors: Boolean = true,
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val autoSave: Boolean = true,
    val autoSaveIntervalMs: Long = 30000,
    val aiEnabled: Boolean = true,
    val aiOfflineMode: Boolean = false,
    val buildOutputPath: String = "",
    val lastOpenedProject: String = "",
    val githubToken: String = "",
    val terminalFont: String = "JetBrains Mono",
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val highlightCurrentLine: Boolean = true,
    val codeCompletionEnabled: Boolean = true,
    val errorHighlightingEnabled: Boolean = true
)

/**
 * Preferences Repository using DataStore.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val FONT_SIZE = intPreferencesKey("font_size")
        val TAB_SIZE = intPreferencesKey("tab_size")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val AI_OFFLINE_MODE = booleanPreferencesKey("ai_offline_mode")
        val BUILD_OUTPUT_PATH = stringPreferencesKey("build_output_path")
        val LAST_OPENED_PROJECT = stringPreferencesKey("last_opened_project")
        val GITHUB_TOKEN = stringPreferencesKey("github_token")
        val TERMINAL_FONT = stringPreferencesKey("terminal_font")
        val SHOW_LINE_NUMBERS = booleanPreferencesKey("show_line_numbers")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val HIGHLIGHT_CURRENT_LINE = booleanPreferencesKey("highlight_current_line")
        val CODE_COMPLETION_ENABLED = booleanPreferencesKey("code_completion_enabled")
        val ERROR_HIGHLIGHTING_ENABLED = booleanPreferencesKey("error_highlighting_enabled")
    }
    
    /**
     * Flow of application preferences.
     */
    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            isDarkTheme = prefs[PreferencesKeys.IS_DARK_THEME] ?: true,
            useDynamicColors = prefs[PreferencesKeys.USE_DYNAMIC_COLORS] ?: true,
            fontSize = prefs[PreferencesKeys.FONT_SIZE] ?: 14,
            tabSize = prefs[PreferencesKeys.TAB_SIZE] ?: 4,
            autoSave = prefs[PreferencesKeys.AUTO_SAVE] ?: true,
            aiEnabled = prefs[PreferencesKeys.AI_ENABLED] ?: true,
            aiOfflineMode = prefs[PreferencesKeys.AI_OFFLINE_MODE] ?: false,
            buildOutputPath = prefs[PreferencesKeys.BUILD_OUTPUT_PATH] ?: "",
            lastOpenedProject = prefs[PreferencesKeys.LAST_OPENED_PROJECT] ?: "",
            githubToken = prefs[PreferencesKeys.GITHUB_TOKEN] ?: "",
            terminalFont = prefs[PreferencesKeys.TERMINAL_FONT] ?: "JetBrains Mono",
            showLineNumbers = prefs[PreferencesKeys.SHOW_LINE_NUMBERS] ?: true,
            wordWrap = prefs[PreferencesKeys.WORD_WRAP] ?: false,
            highlightCurrentLine = prefs[PreferencesKeys.HIGHLIGHT_CURRENT_LINE] ?: true,
            codeCompletionEnabled = prefs[PreferencesKeys.CODE_COMPLETION_ENABLED] ?: true,
            errorHighlightingEnabled = prefs[PreferencesKeys.ERROR_HIGHLIGHTING_ENABLED] ?: true
        )
    }
    
    /**
     * Set dark theme preference.
     */
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_DARK_THEME] = enabled
        }
        Timber.d("Dark theme set to: $enabled")
    }
    
    /**
     * Set dynamic colors preference.
     */
    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USE_DYNAMIC_COLORS] = enabled
        }
        Timber.d("Dynamic colors set to: $enabled")
    }
    
    /**
     * Set font size.
     */
    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FONT_SIZE] = size
        }
        Timber.d("Font size set to: $size")
    }
    
    /**
     * Set tab size.
     */
    suspend fun setTabSize(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TAB_SIZE] = size
        }
        Timber.d("Tab size set to: $size")
    }
    
    /**
     * Set auto save preference.
     */
    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_SAVE] = enabled
        }
        Timber.d("Auto save set to: $enabled")
    }
    
    /**
     * Set AI enabled preference.
     */
    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AI_ENABLED] = enabled
        }
        Timber.d("AI enabled set to: $enabled")
    }
    
    /**
     * Set AI offline mode.
     */
    suspend fun setAiOfflineMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AI_OFFLINE_MODE] = enabled
        }
        Timber.d("AI offline mode set to: $enabled")
    }
    
    /**
     * Set build output path.
     */
    suspend fun setBuildOutputPath(path: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BUILD_OUTPUT_PATH] = path
        }
        Timber.d("Build output path set to: $path")
    }
    
    /**
     * Set last opened project.
     */
    suspend fun setLastOpenedProject(path: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_OPENED_PROJECT] = path
        }
        Timber.d("Last opened project set to: $path")
    }
    
    /**
     * Set GitHub token.
     */
    suspend fun setGitHubToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GITHUB_TOKEN] = token
        }
        Timber.d("GitHub token updated")
    }
    
    /**
     * Set terminal font.
     */
    suspend fun setTerminalFont(font: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TERMINAL_FONT] = font
        }
        Timber.d("Terminal font set to: $font")
    }
    
    /**
     * Set show line numbers preference.
     */
    suspend fun setShowLineNumbers(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SHOW_LINE_NUMBERS] = enabled
        }
        Timber.d("Show line numbers set to: $enabled")
    }
    
    /**
     * Set word wrap preference.
     */
    suspend fun setWordWrap(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.WORD_WRAP] = enabled
        }
        Timber.d("Word wrap set to: $enabled")
    }
    
    /**
     * Set code completion preference.
     */
    suspend fun setCodeCompletionEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CODE_COMPLETION_ENABLED] = enabled
        }
        Timber.d("Code completion set to: $enabled")
    }
}
