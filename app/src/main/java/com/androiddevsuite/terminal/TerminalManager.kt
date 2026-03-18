/**
 * Android Development Suite - Terminal Manager
 * منصة تطوير أندرويد الشاملة
 * 
 * Linux terminal emulator backend with shell execution
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.terminal

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Terminal state.
 */
data class TerminalState(
    val workingDirectory: String,
    val environment: Map<String, String>,
    val history: List<String>,
    val isRunning: Boolean
)

/**
 * Terminal output line.
 */
data class TerminalOutput(
    val text: String,
    val type: OutputType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Output types.
 */
enum class OutputType {
    STDOUT,
    STDERR,
    COMMAND,
    SYSTEM
}

/**
 * Shell process wrapper.
 */
class ShellProcess(
    val process: Process,
    val inputWriter: OutputStreamWriter,
    val outputReader: BufferedReader,
    val errorReader: BufferedReader
)

/**
 * Terminal Manager - Singleton for terminal operations.
 * 
 * Features:
 * - Bash shell execution
 * - Command history
 * - Environment variables
 * - Working directory management
 * - Process management
 * - Terminal emulation
 */
@Singleton
class TerminalManager @Inject constructor(
    private val context: Context
) {
    private var shellProcess: ShellProcess? = null
    
    private val _output = MutableStateFlow<List<TerminalOutput>>(emptyList())
    val output: StateFlow<List<TerminalOutput>> = _output.asStateFlow()
    
    private val _state = MutableStateFlow(TerminalState(
        workingDirectory = System.getProperty("user.home") ?: "/",
        environment = getDefaultEnvironment(),
        history = emptyList(),
        isRunning = false
    ))
    val state: StateFlow<TerminalState> = _state.asStateFlow()
    
    private val commandHistory = mutableListOf<String>()
    private var historyIndex = -1
    
    /**
     * Initialize the terminal shell.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Start shell process
            val processBuilder = ProcessBuilder("sh")
            processBuilder.redirectErrorStream(false)
            processBuilder.directory(File(_state.value.workingDirectory))
            
            // Set environment
            val env = processBuilder.environment()
            env.putAll(_state.value.environment)
            
            val process = processBuilder.start()
            
            shellProcess = ShellProcess(
                process = process,
                inputWriter = OutputStreamWriter(process.outputStream, Charset.defaultCharset()),
                outputReader = BufferedReader(InputStreamReader(process.inputStream, Charset.defaultCharset())),
                errorReader = BufferedReader(InputStreamReader(process.errorStream, Charset.defaultCharset()))
            )
            
            _state.value = _state.value.copy(isRunning = true)
            
            // Start output readers
            startOutputReaders()
            
            addOutput("Terminal initialized", OutputType.SYSTEM)
            addOutput("Type 'help' for available commands.", OutputType.SYSTEM)
            
            Timber.i("Terminal initialized")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize terminal")
            Result.failure(e)
        }
    }
    
    /**
     * Start reading output from shell process.
     */
    private fun startOutputReaders() {
        val shell = shellProcess ?: return
        
        // Read stdout
        Thread {
            try {
                var line: String?
                while (shell.outputReader.readLine().also { line = it } != null) {
                    addOutput(line ?: "", OutputType.STDOUT)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error reading stdout")
            }
        }.start()
        
        // Read stderr
        Thread {
            try {
                var line: String?
                while (shell.errorReader.readLine().also { line = it } != null) {
                    addOutput(line ?: "", OutputType.STDERR)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error reading stderr")
            }
        }.start()
    }
    
    /**
     * Execute a command.
     */
    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        val shell = shellProcess
        
        if (shell == null || !shell.process.isAlive) {
            // Try to reinitialize
            initialize().getOrThrow()
        }
        
        try {
            // Add command to output
            addOutput("${getPrompt()}$command", OutputType.COMMAND)
            
            // Add to history
            if (command.isNotBlank()) {
                commandHistory.add(command)
                historyIndex = commandHistory.size
                _state.value = _state.value.copy(history = commandHistory.toList())
            }
            
            // Handle built-in commands
            handleBuiltinCommand(command)?.let { result ->
                addOutput(result, OutputType.STDOUT)
                return@withContext Result.success(result)
            }
            
            // Send command to shell
            shellProcess?.inputWriter?.apply {
                write("$command\n")
                flush()
            }
            
            // Wait a bit for output
            Thread.sleep(100)
            
            Result.success("")
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute command: $command")
            addOutput("Error: ${e.message}", OutputType.STDERR)
            Result.failure(e)
        }
    }
    
    /**
     * Handle built-in commands.
     */
    private fun handleBuiltinCommand(command: String): String? {
        val parts = command.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return null
        
        return when (parts[0]) {
            "help" -> """
                Available Commands:
                
                Navigation:
                  cd <dir>      - Change directory
                  pwd           - Print working directory
                  ls            - List files
                
                File Operations:
                  cat <file>    - Display file contents
                  mkdir <dir>   - Create directory
                  rm <file>     - Remove file
                  cp <src> <dst> - Copy file
                  mv <src> <dst> - Move file
                
                Development:
                  gradle        - Run Gradle build
                  git           - Git commands
                
                Terminal:
                  clear         - Clear screen
                  history       - Show command history
                  env           - Show environment variables
                  exit          - Exit terminal
                
                Help:
                  help          - Show this help
            """.trimIndent()
            
            "clear" -> {
                _output.value = emptyList()
                ""
            }
            
            "pwd" -> _state.value.workingDirectory
            
            "cd" -> {
                val dir = if (parts.size > 1) parts[1] else System.getProperty("user.home") ?: "/"
                val newDir = File(_state.value.workingDirectory, dir).canonicalPath
                val file = File(newDir)
                
                if (file.exists() && file.isDirectory) {
                    _state.value = _state.value.copy(workingDirectory = newDir)
                    null // Let shell handle it
                } else {
                    "cd: $dir: No such file or directory"
                }
            }
            
            "history" -> {
                commandHistory.mapIndexed { index, cmd ->
                    "${index + 1}  $cmd"
                }.joinToString("\n")
            }
            
            "env" -> {
                _state.value.environment.entries.joinToString("\n") { (k, v) -> "$k=$v" }
            }
            
            "exit" -> {
                close()
                "Terminal closed"
            }
            
            else -> null
        }
    }
    
    /**
     * Get command from history.
     */
    fun getHistoryUp(): String? {
        if (historyIndex <= 0) return null
        historyIndex--
        return commandHistory.getOrNull(historyIndex)
    }
    
    /**
     * Get next command from history.
     */
    fun getHistoryDown(): String? {
        if (historyIndex >= commandHistory.size - 1) return null
        historyIndex++
        return commandHistory.getOrNull(historyIndex)
    }
    
    /**
     * Get shell prompt.
     */
    private fun getPrompt(): String {
        val dir = _state.value.workingDirectory
        val home = System.getProperty("user.home") ?: "/storage/emulated/0"
        val displayDir = if (dir.startsWith(home)) {
            "~${dir.removePrefix(home)}"
        } else dir
        
        return "user@androiddevsuite:$displayDir$ "
    }
    
    /**
     * Add output to terminal.
     */
    private fun addOutput(text: String, type: OutputType) {
        _output.value = _output.value + TerminalOutput(text, type)
    }
    
    /**
     * Set environment variable.
     */
    fun setEnvironmentVariable(key: String, value: String) {
        val newEnv = _state.value.environment.toMutableMap()
        newEnv[key] = value
        _state.value = _state.value.copy(environment = newEnv)
    }
    
    /**
     * Get environment variable.
     */
    fun getEnvironmentVariable(key: String): String? {
        return _state.value.environment[key]
    }
    
    /**
     * Close the terminal.
     */
    fun close() {
        shellProcess?.apply {
            inputWriter.close()
            outputReader.close()
            errorReader.close()
            process.destroy()
        }
        shellProcess = null
        _state.value = _state.value.copy(isRunning = false)
        Timber.i("Terminal closed")
    }
    
    /**
     * Get default environment variables.
     */
    private fun getDefaultEnvironment(): Map<String, String> {
        return mapOf(
            "HOME" to (System.getProperty("user.home") ?: "/storage/emulated/0"),
            "PATH" to "/system/bin:/system/xbin:/data/data/com.androiddevsuite/files/usr/bin",
            "USER" to "user",
            "SHELL" to "/bin/sh",
            "TERM" to "xterm-256color",
            "LANG" to "en_US.UTF-8",
            "ANDROID_ROOT" to "/system",
            "ANDROID_DATA" to "/data",
            "EXTERNAL_STORAGE" to "/storage/emulated/0",
            "ANDROID_DEV_SUITE" to "true"
        )
    }
    
    companion object {
        /**
         * Check if terminal is supported.
         */
        fun isSupported(): Boolean {
            return try {
                Runtime.getRuntime().exec("sh")
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
