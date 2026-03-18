/**
 * Android Development Suite - Home Screen
 * منصة تطوير أندرويد الشاملة
 * 
 * Main dashboard with quick access to all tools
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.androiddevsuite.R

/**
 * Tool card data class.
 */
data class ToolCard(
    val id: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

/**
 * Home Screen - Main dashboard.
 */
@Composable
fun HomeScreen(
    onNavigateToProject: () -> Unit,
    onNavigateToCodeEditor: () -> Unit,
    onNavigateToBlockEditor: () -> Unit,
    onNavigateToTerminal: () -> Unit,
    onNavigateToApkEditor: () -> Unit
) {
    val tools = listOf(
        ToolCard(
            id = "visual_editor",
            titleRes = R.string.tool_visual_editor,
            descriptionRes = R.string.tool_visual_editor_desc,
            icon = androidx.compose.material.icons.Icons.Filled.ViewInAr,
            color = androidx.compose.ui.graphics.Color(0xFF6366F1)
        ),
        ToolCard(
            id = "block_editor",
            titleRes = R.string.tool_block_editor,
            descriptionRes = R.string.tool_block_editor_desc,
            icon = androidx.compose.material.icons.Icons.Filled.Extension,
            color = androidx.compose.ui.graphics.Color(0xFFEC4899)
        ),
        ToolCard(
            id = "code_editor",
            titleRes = R.string.tool_code_editor,
            descriptionRes = R.string.tool_code_editor_desc,
            icon = androidx.compose.material.icons.Icons.Filled.Code,
            color = androidx.compose.ui.graphics.Color(0xFF10B981)
        ),
        ToolCard(
            id = "apk_editor",
            titleRes = R.string.tool_apk_editor,
            descriptionRes = R.string.tool_apk_editor_desc,
            icon = androidx.compose.material.icons.Icons.Filled.Android,
            color = androidx.compose.ui.graphics.Color(0xFF3B82F6)
        ),
        ToolCard(
            id = "terminal",
            titleRes = R.string.tool_terminal,
            descriptionRes = R.string.tool_terminal_desc,
            icon = androidx.compose.material.icons.Icons.Filled.Terminal,
            color = androidx.compose.ui.graphics.Color(0xFF8B5CF6)
        ),
        ToolCard(
            id = "file_manager",
            titleRes = R.string.tool_file_manager,
            descriptionRes = R.string.tool_file_manager_desc,
            icon = androidx.compose.material.icons.Icons.Filled.Folder,
            color = androidx.compose.ui.graphics.Color(0xFFF59E0B)
        ),
        ToolCard(
            id = "ai_assistant",
            titleRes = R.string.tool_ai_assistant,
            descriptionRes = R.string.tool_ai_assistant_desc,
            icon = androidx.compose.material.icons.Icons.Filled.AutoAwesome,
            color = androidx.compose.ui.graphics.Color(0xFFEF4444)
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Welcome to Android Dev Suite",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your all-in-one Android development platform",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tools Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tools) { tool ->
                ToolCardItem(
                    tool = tool,
                    onClick = {
                        when (tool.id) {
                            "visual_editor", "file_manager", "projects" -> onNavigateToProject()
                            "block_editor" -> onNavigateToBlockEditor()
                            "code_editor" -> onNavigateToCodeEditor()
                            "terminal" -> onNavigateToTerminal()
                            "apk_editor" -> onNavigateToApkEditor()
                        }
                    }
                )
            }
        }
        
        // Quick Stats Section
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Projects", "0")
                    StatItem("APKs Built", "0")
                    StatItem("Lines of Code", "0")
                }
            }
        }
    }
}

@Composable
private fun ToolCardItem(
    tool: ToolCard,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = tool.color,
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = getString(tool.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                Text(
                    text = getString(tool.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

// Helper function (in real app, this would come from LocalContext)
@Composable
private fun getString(resId: Int): String {
    // Placeholder - in real app use: LocalContext.current.getString(resId)
    return when (resId) {
        R.string.tool_visual_editor -> "Visual Editor"
        R.string.tool_visual_editor_desc -> "Drag & drop UI design"
        R.string.tool_block_editor -> "Block Editor"
        R.string.tool_block_editor_desc -> "Visual code blocks"
        R.string.tool_code_editor -> "Code Editor"
        R.string.tool_code_editor_desc -> "Advanced code editing"
        R.string.tool_apk_editor -> "APK Editor"
        R.string.tool_apk_editor_desc -> "Modify APK files"
        R.string.tool_terminal -> "Terminal"
        R.string.tool_terminal_desc -> "Linux environment"
        R.string.tool_file_manager -> "File Manager"
        R.string.tool_file_manager_desc -> "Manage project files"
        R.string.tool_ai_assistant -> "AI Assistant"
        R.string.tool_ai_assistant_desc -> "Smart code suggestions"
        else -> ""
    }
}
