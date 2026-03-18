/**
 * Android Development Suite - Application Module
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-level Hilt module.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Dependencies are provided by @Inject constructors
}

/**
 * Network module for API clients.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule

/**
 * Service module for background services.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule
