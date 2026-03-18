/**
 * Android Development Suite - Main ViewModel
 * منصة تطوير أندرويد الشاملة
 * 
 * ViewModel for managing main screen state and navigation
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevsuite.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Main ViewModel responsible for:
 * - Theme management
 * - Navigation state
 * - Global app preferences
 * - Error handling
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Load user preferences on initialization.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                _uiState.update { state ->
                    state.copy(
                        isDarkTheme = prefs.isDarkTheme,
                        useDynamicColors = prefs.useDynamicColors
                    )
                }
            }
        }
    }

    /**
     * Toggle between dark and light theme.
     */
    fun toggleTheme() {
        viewModelScope.launch {
            val newTheme = !_uiState.value.isDarkTheme
            preferencesRepository.setDarkTheme(newTheme)
            Timber.d("Theme toggled to: ${if (newTheme) "dark" else "light"}")
        }
    }

    /**
     * Navigate to a specific tool.
     */
    fun navigateToTool(toolRoute: String) {
        _uiState.update { state ->
            state.copy(currentTool = toolRoute)
        }
        Timber.d("Navigating to tool: $toolRoute")
    }

    /**
     * Clear any error message.
     */
    fun clearError() {
        _uiState.update { state ->
            state.copy(error = null)
        }
    }

    /**
     * Show an error message.
     */
    fun showError(message: String) {
        _uiState.update { state ->
            state.copy(error = message)
        }
        Timber.e("Error shown: $message")
    }
}
