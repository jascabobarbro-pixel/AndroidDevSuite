/**
 * Android Development Suite - Project Build Configuration
 * منصة تطوير أندرويد الشاملة
 * 
 * Build Configuration for Android 15 (API 35)
 * Targeting modern Android development practices
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    
    dependencies {
        // Navigation Safe Args for fragment navigation
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.4")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
