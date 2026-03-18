/**
 * Android Development Suite - Code Templates
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.tools.templates

import com.androiddevsuite.data.model.ProjectTemplate

/**
 * Code template manager for generating project scaffolding.
 */
object CodeTemplates {
    
    val settingsGradle = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "{{PROJECT_NAME}}"
include(":app")
""".trimIndent()

    val buildGradleProject = """
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
""".trimIndent()

    val emptyActivity = """
package {{PACKAGE_NAME}}

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
""".trimIndent()

    fun applyTemplate(template: String, variables: Map<String, String>): String {
        var result = template
        variables.forEach { (key, value) ->
            result = result.replace("{{${key}}}", value)
        }
        return result
    }

    fun getActivityTemplate(template: ProjectTemplate): String = emptyActivity

    fun toCamelCase(name: String): String {
        return name.split(" ", "_", "-")
            .joinToString("") { 
                it.replaceFirstChar { char -> char.uppercase() }
            }
    }
}
