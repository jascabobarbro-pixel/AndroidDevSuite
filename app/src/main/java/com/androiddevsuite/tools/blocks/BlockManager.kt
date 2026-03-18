/**
 * Android Development Suite - Block Editor
 * منصة تطوير أندرويد الشاملة
 * 
 * Visual block-based programming (Sketchware-like)
 */
package com.androiddevsuite.tools.blocks

import android.content.Context
import com.androiddevsuite.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Block Manager - Handles block definitions and code generation.
 */
@Singleton
class BlockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val blockDefinitions = mutableMapOf<String, BlockDefinition>()
    private val workspaceBlocks = mutableMapOf<String, MutableList<WorkspaceBlock>>()
    
    init {
        initializeBlockDefinitions()
    }
    
    /**
     * Initialize default block definitions.
     */
    private fun initializeBlockDefinitions() {
        // Event Blocks
        addBlockDefinition(BlockDefinition(
            id = "event_activity_create",
            type = BlockType.EVENT,
            category = BlockCategory.EVENTS,
            label = "When Activity Created",
            color = "#FF6B6B",
            inputs = emptyList(),
            outputs = listOf(BlockOutput("next", "block")),
            code = "override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)\n    {next}\n}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "event_button_click",
            type = BlockType.EVENT,
            category = BlockCategory.EVENTS,
            label = "When Button Clicked",
            color = "#FF6B6B",
            inputs = listOf(BlockInput("button", InputType.COMPONENT, "", true)),
            outputs = listOf(BlockOutput("next", "block")),
            code = "{button}.setOnClickListener {\n    {next}\n}"
        ))
        
        // Control Blocks
        addBlockDefinition(BlockDefinition(
            id = "control_if",
            type = BlockType.CONTROL,
            category = BlockCategory.CONTROL,
            label = "If",
            color = "#4ECDC4",
            inputs = listOf(
                BlockInput("condition", InputType.BOOLEAN, "", true),
                BlockInput("then", InputType.BLOCK, "", false)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "if ({condition}) {\n    {then}\n}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "control_if_else",
            type = BlockType.CONTROL,
            category = BlockCategory.CONTROL,
            label = "If-Else",
            color = "#4ECDC4",
            inputs = listOf(
                BlockInput("condition", InputType.BOOLEAN, "", true),
                BlockInput("then", InputType.BLOCK, "", false),
                BlockInput("else", InputType.BLOCK, "", false)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "if ({condition}) {\n    {then}\n} else {\n    {else}\n}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "control_for",
            type = BlockType.CONTROL,
            category = BlockCategory.CONTROL,
            label = "For Each",
            color = "#4ECDC4",
            inputs = listOf(
                BlockInput("variable", InputType.VARIABLE, "i", true),
                BlockInput("from", InputType.NUMBER, "0", true),
                BlockInput("to", InputType.NUMBER, "10", true),
                BlockInput("do", InputType.BLOCK, "", false)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "for ({variable} in {from}..{to}) {\n    {do}\n}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "control_while",
            type = BlockType.CONTROL,
            category = BlockCategory.CONTROL,
            label = "While",
            color = "#4ECDC4",
            inputs = listOf(
                BlockInput("condition", InputType.BOOLEAN, "", true),
                BlockInput("do", InputType.BLOCK, "", false)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "while ({condition}) {\n    {do}\n}"
        ))
        
        // Variable Blocks
        addBlockDefinition(BlockDefinition(
            id = "var_set",
            type = BlockType.VARIABLE,
            category = BlockCategory.VARIABLES,
            label = "Set Variable",
            color = "#A78BFA",
            inputs = listOf(
                BlockInput("name", InputType.STRING, "", true),
                BlockInput("value", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "var {name} = {value}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "var_get",
            type = BlockType.VARIABLE,
            category = BlockCategory.VARIABLES,
            label = "Get Variable",
            color = "#A78BFA",
            inputs = listOf(BlockInput("name", InputType.STRING, "", true)),
            outputs = listOf(BlockOutput("value", "any")),
            code = "{name}"
        ))
        
        // Math Blocks
        addBlockDefinition(BlockDefinition(
            id = "math_add",
            type = BlockType.MATH,
            category = BlockCategory.MATH,
            label = "Add",
            color = "#FFE66D",
            inputs = listOf(
                BlockInput("a", InputType.NUMBER, "", true),
                BlockInput("b", InputType.NUMBER, "", true)
            ),
            outputs = listOf(BlockOutput("result", "number")),
            code = "({a} + {b})"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "math_subtract",
            type = BlockType.MATH,
            category = BlockCategory.MATH,
            label = "Subtract",
            color = "#FFE66D",
            inputs = listOf(
                BlockInput("a", InputType.NUMBER, "", true),
                BlockInput("b", InputType.NUMBER, "", true)
            ),
            outputs = listOf(BlockOutput("result", "number")),
            code = "({a} - {b})"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "math_multiply",
            type = BlockType.MATH,
            category = BlockCategory.MATH,
            label = "Multiply",
            color = "#FFE66D",
            inputs = listOf(
                BlockInput("a", InputType.NUMBER, "", true),
                BlockInput("b", InputType.NUMBER, "", true)
            ),
            outputs = listOf(BlockOutput("result", "number")),
            code = "({a} * {b})"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "math_divide",
            type = BlockType.MATH,
            category = BlockCategory.MATH,
            label = "Divide",
            color = "#FFE66D",
            inputs = listOf(
                BlockInput("a", InputType.NUMBER, "", true),
                BlockInput("b", InputType.NUMBER, "", true)
            ),
            outputs = listOf(BlockOutput("result", "number")),
            code = "({a} / {b})"
        ))
        
        // Logic Blocks
        addBlockDefinition(BlockDefinition(
            id = "logic_compare",
            type = BlockType.LOGIC,
            category = BlockCategory.LOGIC,
            label = "Compare",
            color = "#34D399",
            inputs = listOf(
                BlockInput("a", InputType.STRING, "", true),
                BlockInput("operator", InputType.DROPDOWN, "==", true),
                BlockInput("b", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("result", "boolean")),
            code = "({a} {operator} {b})"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "logic_and",
            type = BlockType.LOGIC,
            category = BlockCategory.LOGIC,
            label = "And",
            color = "#34D399",
            inputs = listOf(
                BlockInput("a", InputType.BOOLEAN, "", true),
                BlockInput("b", InputType.BOOLEAN, "", true)
            ),
            outputs = listOf(BlockOutput("result", "boolean")),
            code = "({a} && {b})"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "logic_or",
            type = BlockType.LOGIC,
            category = BlockCategory.LOGIC,
            label = "Or",
            color = "#34D399",
            inputs = listOf(
                BlockInput("a", InputType.BOOLEAN, "", true),
                BlockInput("b", InputType.BOOLEAN, "", true)
            ),
            outputs = listOf(BlockOutput("result", "boolean")),
            code = "({a} || {b})"
        ))
        
        // Component Blocks
        addBlockDefinition(BlockDefinition(
            id = "component_set_text",
            type = BlockType.COMPONENT,
            category = BlockCategory.COMPONENTS,
            label = "Set Text",
            color = "#60A5FA",
            inputs = listOf(
                BlockInput("component", InputType.COMPONENT, "", true),
                BlockInput("text", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "{component}.text = {text}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "component_get_text",
            type = BlockType.COMPONENT,
            category = BlockCategory.COMPONENTS,
            label = "Get Text",
            color = "#60A5FA",
            inputs = listOf(
                BlockInput("component", InputType.COMPONENT, "", true)
            ),
            outputs = listOf(BlockOutput("text", "string")),
            code = "{component}.text.toString()"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "component_show_toast",
            type = BlockType.COMPONENT,
            category = BlockCategory.COMPONENTS,
            label = "Show Toast",
            color = "#60A5FA",
            inputs = listOf(
                BlockInput("message", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "Toast.makeText(this, {message}, Toast.LENGTH_SHORT).show()"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "component_navigate",
            type = BlockType.COMPONENT,
            category = BlockCategory.COMPONENTS,
            label = "Navigate to Screen",
            color = "#60A5FA",
            inputs = listOf(
                BlockInput("screen", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("next", "block")),
            code = "startActivity(Intent(this, {screen}::class.java))"
        ))
        
        // Text Blocks
        addBlockDefinition(BlockDefinition(
            id = "text_concat",
            type = BlockType.TEXT,
            category = BlockCategory.TEXT,
            label = "Join Text",
            color = "#F472B6",
            inputs = listOf(
                BlockInput("a", InputType.STRING, "", true),
                BlockInput("b", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("result", "string")),
            code = "{a} + {b}"
        ))
        
        addBlockDefinition(BlockDefinition(
            id = "text_to_string",
            type = BlockType.TEXT,
            category = BlockCategory.TEXT,
            label = "To String",
            color = "#F472B6",
            inputs = listOf(
                BlockInput("value", InputType.STRING, "", true)
            ),
            outputs = listOf(BlockOutput("result", "string")),
            code = "{value}.toString()"
        ))
        
        Timber.d("Initialized ${blockDefinitions.size} block definitions")
    }
    
    private fun addBlockDefinition(definition: BlockDefinition) {
        blockDefinitions[definition.id] = definition
    }
    
    /**
     * Get block definitions by category.
     */
    fun getBlocksByCategory(category: BlockCategory): List<BlockDefinition> {
        return blockDefinitions.values.filter { it.category == category }
    }
    
    /**
     * Get all block definitions.
     */
    fun getAllBlockDefinitions(): List<BlockDefinition> {
        return blockDefinitions.values.toList()
    }
    
    /**
     * Get block definition by ID.
     */
    fun getBlockDefinition(id: String): BlockDefinition? {
        return blockDefinitions[id]
    }
    
    /**
     * Add block to workspace.
     */
    fun addBlockToWorkspace(projectId: String, block: WorkspaceBlock) {
        workspaceBlocks.getOrPut(projectId) { mutableListOf() }.add(block)
    }
    
    /**
     * Remove block from workspace.
     */
    fun removeBlockFromWorkspace(projectId: String, blockId: String) {
        workspaceBlocks[projectId]?.removeIf { it.id == blockId }
    }
    
    /**
     * Get workspace blocks.
     */
    fun getWorkspaceBlocks(projectId: String): List<WorkspaceBlock> {
        return workspaceBlocks[projectId]?.toList() ?: emptyList()
    }
    
    /**
     * Generate Kotlin code from workspace blocks.
     */
    suspend fun generateCode(projectId: String, screenName: String): String = withContext(Dispatchers.Default) {
        val blocks = workspaceBlocks[projectId]?.filter { it.screenName == screenName } ?: emptyList()
        
        // Find root blocks (blocks without parent)
        val rootBlocks = blocks.filter { it.parentBlockId == null }
        
        val codeBuilder = StringBuilder()
        
        // Generate imports
        codeBuilder.appendLine("package com.example.generated")
        codeBuilder.appendLine()
        codeBuilder.appendLine("import android.os.Bundle")
        codeBuilder.appendLine("import android.widget.Toast")
        codeBuilder.appendLine("import android.content.Intent")
        codeBuilder.appendLine("import androidx.appcompat.app.AppCompatActivity")
        codeBuilder.appendLine()
        codeBuilder.appendLine("class ${screenName}Activity : AppCompatActivity() {")
        codeBuilder.appendLine()
        
        // Generate code for each root block
        rootBlocks.sortedBy { it.order }.forEach { block ->
            val generatedCode = generateBlockCode(block, blocks)
            codeBuilder.append(generatedCode)
            codeBuilder.appendLine()
        }
        
        codeBuilder.appendLine("}")
        
        codeBuilder.toString()
    }
    
    /**
     * Generate code for a single block and its children.
     */
    private fun generateBlockCode(block: WorkspaceBlock, allBlocks: List<WorkspaceBlock>): String {
        val definition = blockDefinitions[block.blockDefinitionId] ?: return ""
        
        var code = definition.code
        
        // Replace input values
        block.values.forEach { (inputName, value) ->
            code = code.replace("{$inputName}", value)
        }
        
        // Replace connected blocks
        block.inputConnections.forEach { (inputName, connectedBlockId) ->
            val connectedBlock = allBlocks.find { it.id == connectedBlockId }
            if (connectedBlock != null) {
                val connectedCode = generateBlockCode(connectedBlock, allBlocks)
                code = code.replace("{$inputName}", connectedCode)
            }
        }
        
        return code
    }
    
    /**
     * Export workspace to JSON.
     */
    fun exportWorkspace(projectId: String): String {
        val blocks = workspaceBlocks[projectId] ?: emptyList()
        // JSON serialization
        return blocks.joinToString(",\n") { block ->
            """{
  "id": "${block.id}",
  "blockDefinitionId": "${block.blockDefinitionId}",
  "x": ${block.x},
  "y": ${block.y},
  "parentBlockId": ${block.parentBlockId?.let { "\"$it\"" } ?: "null"},
  "values": {${block.values.entries.joinToString(",") { "\"${it.key}\": \"${it.value}\"" }}}
}"""
        }
    }
    
    /**
     * Import workspace from JSON.
     */
    fun importWorkspace(projectId: String, json: String) {
        // Parse JSON and create blocks
        // Implementation would use proper JSON parser
        Timber.d("Imported workspace for project: $projectId")
    }
    
    /**
     * Clear workspace.
     */
    fun clearWorkspace(projectId: String) {
        workspaceBlocks[projectId]?.clear()
    }
}
