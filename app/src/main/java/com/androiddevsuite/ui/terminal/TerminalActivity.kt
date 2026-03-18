/**
 * Android Development Suite - Terminal Activity
 * منصة تطوير أندرويد الشاملة
 * 
 * Full-screen terminal emulator activity
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.terminal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevsuite.terminal.TerminalManager
import com.androiddevsuite.terminal.OutputType
import com.androiddevsuite.ui.theme.AndroidDevSuiteTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Terminal Activity ViewModel.
 */
@AndroidEntryPoint
class TerminalActivityViewModel @Inject constructor(
    private val terminalManager: TerminalManager
) : ViewModel() {
    
    val output = terminalManager.output
    val state = terminalManager.state
    
    init {
        viewModelScope.launch {
            terminalManager.initialize()
        }
    }
    
    fun executeCommand(command: String) {
        viewModelScope.launch {
            terminalManager.executeCommand(command)
        }
    }
    
    fun getHistoryUp(): String? = terminalManager.getHistoryUp()
    fun getHistoryDown(): String? = terminalManager.getHistoryDown()
    
    override fun onCleared() {
        super.onCleared()
        terminalManager.close()
    }
}

/**
 * Terminal Activity - Full-screen terminal emulator.
 */
@AndroidEntryPoint
class TerminalActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidDevSuiteTheme {
                TerminalScreenContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreenContent(
    viewModel: TerminalActivityViewModel = hiltViewModel()
) {
    val output by viewModel.output.collectAsState()
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    
    var commandInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Auto-scroll to bottom when new output arrives
    LaunchedEffect(output.size) {
        if (output.isNotEmpty()) {
            listState.animateScrollToItem(output.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        // Top bar
        TopAppBar(
            title = { 
                Text("Terminal", color = Color(0xFFC9D1D9))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF161B22)
            ),
            actions = {
                IconButton(onClick = { /* Settings */ }) {
                    Icon(Icons.Filled.Settings, "Settings", tint = Color(0xFFC9D1D9))
                }
            }
        )
        
        // Terminal output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            state = listState
        ) {
            items(output) { line ->
                Text(
                    text = line.text,
                    color = when (line.type) {
                        OutputType.STDOUT -> Color(0xFFC9D1D9)
                        OutputType.STDERR -> Color(0xFFF85149)
                        OutputType.COMMAND -> Color(0xFF58A6FF)
                        OutputType.SYSTEM -> Color(0xFF8B949E)
                    },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
        
        // Command input
        HorizontalDivider(color = Color(0xFF30363D))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161B22))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = Color(0xFF4ECDC4),
                fontFamily = FontFamily.Monospace
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
                singleLine = true
            )
            
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.executeCommand(commandInput)
                        commandInput = ""
                    }
                }
            ) {
                Icon(Icons.Filled.ArrowForward, "Execute", tint = Color(0xFF4ECDC4))
            }
        }
    }
}
