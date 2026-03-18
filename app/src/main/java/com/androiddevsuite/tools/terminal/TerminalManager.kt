/**
 * Android Development Suite - Terminal Emulator
 * منصة تطوير أندرويد الشاملة
 * 
 * Full terminal emulator like Termux
 */
package com.androiddevsuite.tools.terminal

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Terminal session manager.
 */
@Singleton
class TerminalManager @Inject constructor(
    private val context: Context
) {
    private val sessions = mutableMapOf<String, TerminalSessionImpl>()
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()
    
    fun createSession(
        name: String = "Terminal",
        workingDirectory: String? = null,
        shellPath: String? = null
    ): TerminalSessionImpl {
        val sessionId = "term_${System.currentTimeMillis()}_${(0..9999).random()}"
        
        val session = TerminalSessionImpl(
            id = sessionId,
            name = name,
            context = context,
            workingDirectory = workingDirectory ?: context.filesDir.absolutePath,
            shellPath = shellPath ?: "/system/bin/sh"
        )
        
        sessions[sessionId] = session
        _activeSessionId.value = sessionId
        Timber.d("Created terminal session: $sessionId")
        return session
    }
    
    fun getSession(sessionId: String): TerminalSessionImpl? = sessions[sessionId]
    fun getAllSessions(): List<TerminalSessionImpl> = sessions.values.toList()
    
    fun destroySession(sessionId: String) {
        sessions[sessionId]?.destroy()
        sessions.remove(sessionId)
        if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = sessions.keys.firstOrNull()
        }
        Timber.d("Destroyed terminal session: $sessionId")
    }
    
    fun setActiveSession(sessionId: String) {
        if (sessions.containsKey(sessionId)) {
            _activeSessionId.value = sessionId
        }
    }
    
    fun destroyAllSessions() {
        sessions.values.forEach { it.destroy() }
        sessions.clear()
        _activeSessionId.value = null
    }
}

/**
 * Terminal session implementation.
 */
class TerminalSessionImpl(
    val id: String,
    val name: String,
    private val context: Context,
    private val workingDirectory: String,
    private val shellPath: String
) {
    private var process: Process? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var errorStream: InputStream? = null
    
    private val _output = MutableStateFlow<String>("")
    val output: StateFlow<String> = _output.asStateFlow()
    
    private val commandQueue = ConcurrentLinkedQueue<String>()
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    
    private val environment = mutableMapOf(
        "HOME" to context.filesDir.absolutePath,
        "PATH" to "/system/bin:/system/xbin:/data/data/com.androiddevsuite/files/usr/bin",
        "TERM" to "xterm-256color",
        "LANG" to "en_US.UTF-8",
        "ANDROID_ROOT" to "/system",
        "ANDROID_DATA" to "/data",
        "TMPDIR" to context.cacheDir.absolutePath,
        "PWD" to workingDirectory
    )
    
    private val history = mutableListOf<String>()
    
    fun start() {
        if (isRunning) return
        
        try {
            val workingDir = File(workingDirectory)
            if (!workingDir.exists()) workingDir.mkdirs()
            
            val builder = ProcessBuilder(shellPath)
            builder.directory(workingDir)
            builder.environment().putAll(environment)
            process = builder.start()
            
            outputStream = process?.outputStream
            inputStream = process?.inputStream
            errorStream = process?.errorStream
            isRunning = true
            
            startOutputReader()
            startErrorReader()
            
            appendOutput("Android Dev Suite Terminal\n")
            appendOutput("Session: $name\n")
            appendOutput("Working Directory: $workingDirectory\n\n")
            appendOutput("$ ")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to start terminal session")
            appendOutput("Error starting terminal: ${e.message}\n")
        }
    }
    
    fun executeCommand(command: String) {
        if (!isRunning) start()
        
        try {
            if (command.isNotBlank()) history.add(command)
            outputStream?.write("$command\n".toByteArray(StandardCharsets.UTF_8))
            outputStream?.flush()
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute command: $command")
            appendOutput("Error: ${e.message}\n")
        }
    }
    
    fun getHistory(): List<String> = history.toList()
    fun clearOutput() { _output.value = "" }
    
    fun destroy() {
        isRunning = false
        try {
            outputStream?.close()
            inputStream?.close()
            errorStream?.close()
            process?.destroy()
        } catch (e: Exception) {
            Timber.e(e, "Error destroying terminal session")
        }
    }
    
    fun setEnvironment(key: String, value: String) { environment[key] = value }
    fun getEnvironment(): Map<String, String> = environment.toMap()
    
    private fun startOutputReader() {
        Thread {
            try {
                val buffer = ByteArray(4096)
                while (isRunning && inputStream != null) {
                    val read = inputStream?.read(buffer) ?: -1
                    if (read > 0) {
                        val text = String(buffer, 0, read, StandardCharsets.UTF_8)
                        handler.post { appendOutput(text) }
                    }
                }
            } catch (e: Exception) {
                if (isRunning) Timber.e(e, "Error reading terminal output")
            }
        }.start()
    }
    
    private fun startErrorReader() {
        Thread {
            try {
                val buffer = ByteArray(4096)
                while (isRunning && errorStream != null) {
                    val read = errorStream?.read(buffer) ?: -1
                    if (read > 0) {
                        val text = String(buffer, 0, read, StandardCharsets.UTF_8)
                        handler.post { appendOutput(text) }
                    }
                }
            } catch (e: Exception) {
                if (isRunning) Timber.e(e, "Error reading terminal error stream")
            }
        }.start()
    }
    
    private fun appendOutput(text: String) { _output.value += text }
}

data class CommandResult(val success: Boolean, val output: String, val exitCode: Int)

object BuiltinCommands {
    val COMMANDS = mapOf(
        "help" to "Show available commands",
        "clear" to "Clear terminal screen",
        "cd" to "Change directory",
        "ls" to "List files",
        "pwd" to "Print working directory",
        "echo" to "Print text",
        "cat" to "Read file",
        "mkdir" to "Create directory",
        "rm" to "Remove file or directory",
        "cp" to "Copy file",
        "mv" to "Move file",
        "touch" to "Create empty file",
        "grep" to "Search text in files",
        "find" to "Find files",
        "chmod" to "Change permissions",
        "export" to "Set environment variable",
        "env" to "Show environment variables",
        "history" to "Show command history",
        "exit" to "Exit terminal",
        "gradle" to "Run Gradle build",
        "git" to "Git version control"
    )
    
    fun getHelp(): String = buildString {
        appendLine("Available commands:")
        appendLine()
        COMMANDS.entries.sortedBy { it.key }.forEach { (cmd, desc) ->
            appendLine("  ${cmd.padEnd(10)} - $desc")
        }
    }
}
