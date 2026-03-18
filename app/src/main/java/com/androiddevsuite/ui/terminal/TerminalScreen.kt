/**
 * Android Development Suite - Terminal Screen
 * منصة تطوير أندرويد الشاملة
 * 
 * Linux terminal emulator (Termux-like)
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Terminal output line.
 */
data class TerminalLine(
    val text: String,
    val isOutput: Boolean = true
)

/**
 * Terminal Screen - Linux shell emulator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onCommandExecute: (String) -> Unit
) {
    var commandInput by remember { mutableStateOf("") }
    var terminalOutput by remember { 
        mutableStateOf(
            listOf(
                TerminalLine("Android Dev Suite Terminal v1.0.0", false),
                TerminalLine("Type 'help' for available commands.", false),
                TerminalLine("", false),
                TerminalLine("user@androiddevsuite:~$ ", false)
            )
        ) 
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Terminal toolbar
        TopAppBar(
            title = { 
                Text(
                    "Terminal",
                    color = Color(0xFFC9D1D9)
                ) 
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF161B22)
            ),
            actions = {
                IconButton(onClick = { /* New session */ }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "New Session",
                        tint = Color(0xFFC9D1D9)
                    )
                }
                IconButton(onClick = { /* Clear */ }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Clear",
                        tint = Color(0xFFC9D1D9)
                    )
                }
                IconButton(onClick = { /* Copy */ }) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFFC9D1D9)
                    )
                }
            }
        )
        
        // Terminal output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(terminalOutput) { line ->
                Text(
                    text = line.text,
                    color = if (line.isOutput) Color(0xFF58A6FF) else Color(0xFFC9D1D9),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Command input
        Divider(color = Color(0xFF30363D), thickness = 1.dp)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(8.dp)
        ) {
            Text(
                text = "$ ",
                color = Color(0xFF4ECDC4),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
            
            BasicTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFFC9D1D9),
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(Color(0xFF58A6FF)),
                singleLine = true,
                onKeyEvent = { keyEvent ->
                    // Handle Enter key to execute command
                    false
                }
            )
            
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        terminalOutput = terminalOutput + 
                            TerminalLine("$ ${commandInput}", false)
                        
                        // Simulate command output
                        when (commandInput.lowercase().trim()) {
                            "help" -> {
                                terminalOutput = terminalOutput + listOf(
                                    TerminalLine("Available commands:", true),
                                    TerminalLine("  help     - Show this help", true),
                                    TerminalLine("  ls       - List files", true),
                                    TerminalLine("  cd       - Change directory", true),
                                    TerminalLine("  cat      - Read file", true),
                                    TerminalLine("  clear    - Clear terminal", true),
                                    TerminalLine("  gradle   - Run Gradle build", true),
                                    TerminalLine("  git      - Git commands", true)
                                )
                            }
                            "clear" -> {
                                terminalOutput = listOf(
                                    TerminalLine("user@androiddevsuite:~$ ", false)
                                )
                            }
                            "ls" -> {
                                terminalOutput = terminalOutput + listOf(
                                    TerminalLine("app/  build.gradle  settings.gradle  gradle/", true)
                                )
                            }
                            else -> {
                                terminalOutput = terminalOutput + 
                                    TerminalLine("Command: $commandInput", true)
                            }
                        }
                        
                        terminalOutput = terminalOutput + 
                            TerminalLine("", false) +
                            TerminalLine("user@androiddevsuite:~$ ", false)
                        
                        commandInput = ""
                    }
                    true
                }
            ) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = "Execute",
                    tint = Color(0xFF4ECDC4)
                )
            }
        }
    }
}
