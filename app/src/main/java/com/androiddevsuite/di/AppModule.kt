/**
 * Android Development Suite - Application Module
 * منصة تطوير أندرويد الشاملة
 * 
 * Hilt dependency injection modules
 * 
 * @author Lead Systems Architect
 * @version 2.0.0
 */
package com.androiddevsuite.di

import android.content.Context
import androidx.room.Room
import com.androiddevsuite.ai.AIManager
import com.androiddevsuite.ai.OllamaAIManager
import com.androiddevsuite.build.BuildManager
import com.androiddevsuite.data.local.AppDatabase
import com.androiddevsuite.data.local.ProjectDao
import com.androiddevsuite.data.local.BuildHistoryDao
import com.androiddevsuite.data.local.TerminalHistoryDao
import com.androiddevsuite.data.local.WorkspaceBlockDao
import com.androiddevsuite.data.local.FileBookmarkDao
import com.androiddevsuite.data.local.RecentFileDao
import com.androiddevsuite.data.local.ProjectRepository
import com.androiddevsuite.data.preferences.PreferencesRepository
import com.androiddevsuite.git.GitManager
import com.androiddevsuite.sandbox.SandboxManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Application-level Hilt module.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    
    @Singleton
    @Provides
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }
    
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "android_dev_suite.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Singleton
    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }
    
    @Singleton
    @Provides
    fun provideBuildHistoryDao(database: AppDatabase): BuildHistoryDao {
        return database.buildHistoryDao()
    }
    
    @Singleton
    @Provides
    fun provideTerminalHistoryDao(database: AppDatabase): TerminalHistoryDao {
        return database.terminalHistoryDao()
    }
    
    @Singleton
    @Provides
    fun provideWorkspaceBlockDao(database: AppDatabase): WorkspaceBlockDao {
        return database.workspaceBlockDao()
    }
    
    @Singleton
    @Provides
    fun provideFileBookmarkDao(database: AppDatabase): FileBookmarkDao {
        return database.fileBookmarkDao()
    }
    
    @Singleton
    @Provides
    fun provideRecentFileDao(database: AppDatabase): RecentFileDao {
        return database.recentFileDao()
    }
    
    @Singleton
    @Provides
    fun provideOllamaAIManager(
        @ApplicationContext context: Context
    ): OllamaAIManager {
        return OllamaAIManager(context)
    }
    
    @Singleton
    @Provides
    fun provideAIManager(
        @ApplicationContext context: Context,
        ollamaManager: OllamaAIManager
    ): AIManager {
        return AIManager(context, ollamaManager)
    }
    
    @Singleton
    @Provides
    fun provideGitManager(
        @ApplicationContext context: Context
    ): GitManager {
        return GitManager(context)
    }
    
    @Singleton
    @Provides
    fun provideBuildManager(
        @ApplicationContext context: Context,
        projectDao: ProjectDao
    ): BuildManager {
        return BuildManager(context, projectDao)
    }
    
    @Singleton
    @Provides
    fun provideSandboxManager(
        @ApplicationContext context: Context
    ): SandboxManager {
        return SandboxManager(context)
    }
}

/**
 * Network module for API clients.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // GitHub API client would be configured here
    // OkHttpClient, Retrofit instances
    
    @Singleton
    @Provides
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
}

/**
 * Service module for background services.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    // Service-related dependencies
}
