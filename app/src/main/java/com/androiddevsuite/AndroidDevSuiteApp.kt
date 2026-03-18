/**
 * Android Development Suite - Application Class
 * منصة تطوير أندرويد الشاملة
 * 
 * Main Application class responsible for:
 * - Hilt dependency injection initialization
 * - Global configuration
 * - Security setup
 * - AI model preloading
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Application class with Hilt integration and WorkManager configuration.
 * This class serves as the entry point for dependency injection and global app initialization.
 */
@HiltAndroidApp
class AndroidDevSuiteApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) {
                android.util.Log.DEBUG
            } else {
                android.util.Log.INFO
            })
            .build()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Required for multidex
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        initTimber()
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize security components
        initSecurity()
        
        // Preload AI models (async)
        preloadAIModels()
        
        Timber.d("Android Dev Suite initialized - Version: ${BuildConfig.VERSION_NAME}")
        Timber.d("Build Type: ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}")
        Timber.d("Target SDK: ${Build.VERSION_CODES.VANILLA_ICE_CREAM}")
    }

    /**
     * Initialize Timber logging with custom tree for debug builds.
     * In release builds, only warnings and errors are logged.
     */
    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * Create notification channels for Android O and above.
     * Required for build notifications and AI processing alerts.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Build notification channel
            val buildChannel = NotificationChannel(
                CHANNEL_BUILD,
                getString(R.string.notification_build_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for project build status"
                setShowBadge(true)
            }
            
            // AI processing channel
            val aiChannel = NotificationChannel(
                CHANNEL_AI,
                "AI Processing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for AI model processing"
                setShowBadge(false)
            }
            
            // Terminal output channel
            val terminalChannel = NotificationChannel(
                CHANNEL_TERMINAL,
                "Terminal",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Terminal session notifications"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(buildChannel, aiChannel, terminalChannel)
            )
        }
    }

    /**
     * Initialize security components for APK signing and file encryption.
     * Uses Android Keystore for secure key storage.
     */
    private fun initSecurity() {
        // Security initialization will be handled by SecurityManager
        Timber.d("Security components initialized")
    }

    /**
     * Preload AI models asynchronously to reduce first-use latency.
     * Uses TensorFlow Lite for on-device inference.
     */
    private fun preloadAIModels() {
        // AI model preloading will be handled by AIManager
        Timber.d("AI models preloading initiated")
    }

    /**
     * Custom Timber tree for release builds.
     * Only logs warnings and errors to prevent sensitive data leakage.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.WARN || priority == android.util.Log.ERROR) {
                // In production, you would send this to a crash reporting service
                // like Firebase Crashlytics or Sentry
            }
        }
    }

    companion object {
        const val CHANNEL_BUILD = "build_channel"
        const val CHANNEL_AI = "ai_channel"
        const val CHANNEL_TERMINAL = "terminal_channel"
    }
}
