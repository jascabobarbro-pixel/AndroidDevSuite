/**
 * Android Development Suite - Dependency Injection Modules
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.core.di

import android.content.Context
import com.androiddevsuite.ai.AIManager
import com.androiddevsuite.data.local.AppDatabase
import com.androiddevsuite.data.local.dao.ProjectDao
import com.androiddevsuite.data.local.dao.BuildHistoryDao
import com.androiddevsuite.data.preferences.PreferencesRepository
import com.androiddevsuite.data.remote.github.GitHubApiClient
import com.androiddevsuite.services.BuildExecutor
import com.androiddevsuite.tools.apk.ApkEditor
import com.androiddevsuite.tools.blocks.BlockManager
import com.androiddevsuite.tools.editor.CodeEditorManager
import com.androiddevsuite.tools.filemanager.FileManager
import com.androiddevsuite.tools.git.GitManager
import com.androiddevsuite.tools.terminal.TerminalManager
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
 * Application-level DI module.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    
    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }
    
    @Provides
    fun provideBuildHistoryDao(database: AppDatabase): BuildHistoryDao {
        return database.buildHistoryDao()
    }
    
    @Provides
    @Singleton
    fun provideAIManager(
        @ApplicationContext context: Context
    ): AIManager {
        return AIManager(context)
    }
    
    @Provides
    @Singleton
    fun provideGitHubApiClient(): GitHubApiClient {
        return GitHubApiClient()
    }
    
    @Provides
    @Singleton
    fun provideSandboxManager(
        @ApplicationContext context: Context
    ): SandboxManager {
        return SandboxManager(context)
    }
    
    @Provides
    @Singleton
    fun provideBuildExecutor(
        @ApplicationContext context: Context
    ): BuildExecutor {
        return BuildExecutor(context)
    }
    
    @Provides
    @Singleton
    fun provideTerminalManager(
        @ApplicationContext context: Context
    ): TerminalManager {
        return TerminalManager(context)
    }
    
    @Provides
    @Singleton
    fun provideApkEditor(
        @ApplicationContext context: Context,
        sandboxManager: SandboxManager
    ): ApkEditor {
        return ApkEditor(context, sandboxManager)
    }
    
    @Provides
    @Singleton
    fun provideCodeEditorManager(
        @ApplicationContext context: Context,
        aiManager: AIManager
    ): CodeEditorManager {
        return CodeEditorManager(context, aiManager)
    }
    
    @Provides
    @Singleton
    fun provideBlockManager(
        @ApplicationContext context: Context
    ): BlockManager {
        return BlockManager(context)
    }
    
    @Provides
    @Singleton
    fun provideFileManager(
        @ApplicationContext context: Context
    ): FileManager {
        return FileManager(context)
    }
    
    @Provides
    @Singleton
    fun provideGitManager(
        @ApplicationContext context: Context
    ): GitManager {
        return GitManager(context)
    }
}

/**
 * Repository DI module.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    // Add repository providers here
}

/**
 * Network DI module.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Network related providers
}
