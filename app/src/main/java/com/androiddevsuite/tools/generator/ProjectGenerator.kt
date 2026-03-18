/**
 * Android Development Suite - Project Generator
 * منصة تطوير أندرويد الشاملة
 */
package com.androiddevsuite.tools.generator

import android.content.Context
import com.androiddevsuite.data.model.Project
import com.androiddevsuite.data.model.ProjectTemplate
import com.androiddevsuite.tools.templates.CodeTemplates
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Project configuration.
 */
data class ProjectConfig(
    val name: String,
    val packageName: String,
    val path: String,
    val template: ProjectTemplate,
    val minSdk: Int = 26,
    val targetSdk: Int = 35
)

/**
 * Project Generator.
 */
@Singleton
class ProjectGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun createProject(config: ProjectConfig): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val projectId = UUID.randomUUID().toString()
            val projectDir = File(config.path, config.name)
            
            createDirectoryStructure(projectDir, config)
            generateGradleFiles(projectDir, config)
            generateSourceFiles(projectDir, config)
            generateResourceFiles(projectDir, config)
            generateManifest(projectDir, config)
            
            val project = Project(
                id = projectId,
                name = config.name,
                path = projectDir.absolutePath,
                packageName = config.packageName,
                minSdk = config.minSdk,
                targetSdk = config.targetSdk,
                template = config.template
            )
            
            Timber.i("Project created: ${project.path}")
            Result.success(project)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create project")
            Result.failure(e)
        }
    }
    
    private fun createDirectoryStructure(projectDir: File, config: ProjectConfig) {
        val packagePath = config.packageName.replace('.', '/')
        listOf(
            "app/src/main/java/$packagePath",
            "app/src/main/res/layout",
            "app/src/main/res/values",
            "app/src/main/res/drawable",
            "gradle/wrapper"
        ).forEach { File(projectDir, it).mkdirs() }
    }
    
    private fun generateGradleFiles(projectDir: File, config: ProjectConfig) {
        File(projectDir, "settings.gradle.kts").writeText(
            CodeTemplates.applyTemplate(CodeTemplates.settingsGradle, mapOf("PROJECT_NAME" to config.name))
        )
        File(projectDir, "build.gradle.kts").writeText(CodeTemplates.buildGradleProject)
        File(projectDir, "app/build.gradle.kts").writeText(
            CodeTemplates.applyTemplate(CodeTemplates.buildGradleApp, mapOf("PACKAGE_NAME" to config.packageName))
        )
    }
    
    private fun generateSourceFiles(projectDir: File, config: ProjectConfig) {
        val packagePath = config.packageName.replace('.', '/')
        val srcDir = File(projectDir, "app/src/main/java/$packagePath")
        
        File(srcDir, "MainActivity.kt").writeText(
            CodeTemplates.applyTemplate(
                CodeTemplates.getActivityTemplate(config.template),
                mapOf(
                    "PACKAGE_NAME" to config.packageName,
                    "PROJECT_NAME_CAMEL" to CodeTemplates.toCamelCase(config.name)
                )
            )
        )
    }
    
    private fun generateResourceFiles(projectDir: File, config: ProjectConfig) {
        val resDir = File(projectDir, "app/src/main/res")
        
        File(resDir, "values/strings.xml").writeText(
            """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">${config.name}</string>
</resources>"""
        )
        
        File(resDir, "values/colors.xml").writeText(
            """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#6B6BFF</color>
    <color name="secondary">#4ECDC4</color>
</resources>"""
        )
    }
    
    private fun generateManifest(projectDir: File, config: ProjectConfig) {
        File(projectDir, "app/src/main/AndroidManifest.xml").writeText(
            CodeTemplates.applyTemplate(
                CodeTemplates.manifestTemplate,
                mapOf(
                    "PACKAGE_NAME" to config.packageName,
                    "PROJECT_NAME_CAMEL" to CodeTemplates.toCamelCase(config.name)
                )
            )
        )
    }
    
    suspend fun deleteProject(project: Project): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            File(project.path).deleteRecursively()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
