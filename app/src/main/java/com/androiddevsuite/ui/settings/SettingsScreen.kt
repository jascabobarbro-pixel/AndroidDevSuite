/**
 * Android Development Suite - Settings Screen
 * منصة تطوير أندرويد الشاملة
 * 
 * Application settings and preferences
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Settings Screen - Application preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChange: () -> Unit,
    currentTheme: String
) {
    var darkTheme by remember { mutableStateOf(currentTheme == "dark") }
    var dynamicColors by remember { mutableStateOf(true) }
    var autoSave by remember { mutableStateOf(true) }
    var aiEnabled by remember { mutableStateOf(true) }
    var aiOffline by remember { mutableStateOf(false) }
    var fontSize by remember { mutableIntStateOf(14) }
    var tabSize by remember { mutableIntStateOf(4) }
    var lineNumbers by remember { mutableStateOf(true) }
    var wordWrap by remember { mutableStateOf(false) }
    var codeCompletion by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Appearance Section
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsSwitch(
                    title = "Dark Theme",
                    subtitle = "Enable dark color scheme",
                    checked = darkTheme,
                    onCheckedChange = { 
                        darkTheme = it
                        onThemeChange()
                    }
                )
                
                Divider()
                
                SettingsSwitch(
                    title = "Dynamic Colors",
                    subtitle = "Use Material You colors (Android 12+)",
                    checked = dynamicColors,
                    onCheckedChange = { dynamicColors = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Editor Section
        Text(
            text = "Editor",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsSlider(
                    title = "Font Size",
                    value = fontSize,
                    valueRange = 8..24,
                    onValueChange = { fontSize = it }
                )
                
                Divider()
                
                SettingsSlider(
                    title = "Tab Size",
                    value = tabSize,
                    valueRange = 2..8,
                    onValueChange = { tabSize = it }
                )
                
                Divider()
                
                SettingsSwitch(
                    title = "Show Line Numbers",
                    subtitle = "Display line numbers in editor",
                    checked = lineNumbers,
                    onCheckedChange = { lineNumbers = it }
                )
                
                Divider()
                
                SettingsSwitch(
                    title = "Word Wrap",
                    subtitle = "Wrap long lines",
                    checked = wordWrap,
                    onCheckedChange = { wordWrap = it }
                )
                
                Divider()
                
                SettingsSwitch(
                    title = "Code Completion",
                    subtitle = "Enable intelligent code suggestions",
                    checked = codeCompletion,
                    onCheckedChange = { codeCompletion = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Auto Save Section
        Text(
            text = "Auto Save",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsSwitch(
                title = "Enable Auto Save",
                subtitle = "Automatically save files every 30 seconds",
                checked = autoSave,
                onCheckedChange = { autoSave = it }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AI Section
        Text(
            text = "AI Assistant",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsSwitch(
                    title = "Enable AI Assistant",
                    subtitle = "Get intelligent code suggestions",
                    checked = aiEnabled,
                    onCheckedChange = { aiEnabled = it }
                )
                
                Divider()
                
                SettingsSwitch(
                    title = "Offline Mode",
                    subtitle = "Use on-device AI only (TensorFlow Lite)",
                    checked = aiOffline,
                    onCheckedChange = { aiOffline = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Build Section
        Text(
            text = "Build",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsItem(
                title = "Build Output Directory",
                subtitle = "/storage/emulated/0/AndroidDevSuite/Builds",
                onClick = { /* Open directory picker */ }
            )
            
            Divider()
            
            SettingsItem(
                title = "Keystore Management",
                subtitle = "Manage signing keys",
                onClick = { /* Open keystore manager */ }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // About Section
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Android Dev Suite",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Target SDK: 35 (Android 15)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { subtitle?.let { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title)
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { subtitle?.let { Text(it) } },
        trailingContent = {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}
