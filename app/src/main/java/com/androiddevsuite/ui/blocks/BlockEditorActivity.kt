/**
 * Android Development Suite - Block Editor Activity
 * منصة تطوير أندرويد الشاملة
 * 
 * Full-screen visual block programming activity
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.blocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.androiddevsuite.ui.theme.AndroidDevSuiteTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Block types with colors.
 */
enum class BlockCategory(val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EVENTS(Color(0xFFFF6B6B), Icons.Filled.Notifications),
    CONTROL(Color(0xFF4ECDC4), Icons.Filled.Refresh),
    VARIABLES(Color(0xFFA78BFA), Icons.Filled.Variable),
    FUNCTIONS(Color(0xFFF472B6), Icons.Filled.Functions),
    COMPONENTS(Color(0xFF60A5FA), Icons.Filled.Widgets),
    LOGIC(Color(0xFF34D399), Icons.Filled.CallSplit),
    MATH(Color(0xFFFFE66D), Icons.Filled.Calculate),
    TEXT(Color(0xFFFB923C), Icons.Filled.TextFields)
}

/**
 * Programming block.
 */
data class ProgrammingBlock(
    val id: String,
    val category: BlockCategory,
    val type: String,
    val label: String,
    val code: String,
    val inputs: List<BlockInput> = emptyList(),
    val children: List<ProgrammingBlock> = emptyList()
)

/**
 * Block input parameter.
 */
data class BlockInput(
    val name: String,
    val type: InputType,
    val defaultValue: String = ""
)

/**
 * Input types for blocks.
 */
enum class InputType {
    TEXT,
    NUMBER,
    BOOLEAN,
    VARIABLE,
    DROPDOWN,
    COLOR
}

/**
 * Block Editor Activity - Full-screen visual programming.
 */
@AndroidEntryPoint
class BlockEditorActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidDevSuiteTheme {
                BlockEditorScreenContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockEditorScreenContent() {
    var selectedCategory by remember { mutableStateOf(BlockCategory.EVENTS) }
    var showCodePreview by remember { mutableStateOf(false) }
    var workspaceBlocks by remember { mutableStateOf<List<ProgrammingBlock>>(emptyList()) }
    
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left panel - Block palette
        Card(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = BlockCategory.values().indexOf(selectedCategory),
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 0.dp
                ) {
                    BlockCategory.values().forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            icon = {
                                Icon(
                                    category.icon,
                                    contentDescription = null,
                                    tint = category.color
                                )
                            }
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Block list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(getBlocksForCategory(selectedCategory).size) { index ->
                        val block = getBlocksForCategory(selectedCategory)[index]
                        DraggableBlockItem(block = block)
                    }
                }
            }
        }
        
        // Right panel - Workspace
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Toolbar
            TopAppBar(
                title = { Text("Block Editor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { showCodePreview = !showCodePreview }) {
                        Icon(Icons.Filled.Code, "View Code")
                    }
                    IconButton(onClick = { /* Run */ }) {
                        Icon(Icons.Filled.PlayArrow, "Run")
                    }
                    IconButton(onClick = { /* Save */ }) {
                        Icon(Icons.Filled.Save, "Save")
                    }
                    IconButton(onClick = { /* Export */ }) {
                        Icon(Icons.Filled.IosShare, "Export")
                    }
                }
            )
            
            // Workspace area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (workspaceBlocks.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Drag blocks here to start building",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // Show workspace blocks
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workspaceBlocks.size) { index ->
                            WorkspaceBlockItem(block = workspaceBlocks[index])
                        }
                    }
                }
            }
            
            // Code preview (if shown)
            if (showCodePreview) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            "Generated Code",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                        HorizontalDivider()
                        Text(
                            text = generateCode(workspaceBlocks),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableBlockItem(block: ProgrammingBlock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = block.category.color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(block.category.color)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = block.label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun WorkspaceBlockItem(block: ProgrammingBlock, depth: Int = 0) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp),
        colors = CardDefaults.cardColors(
            containerColor = block.category.color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = block.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            block.children.forEach { child ->
                Spacer(modifier = Modifier.height(8.dp))
                WorkspaceBlockItem(child, depth + 1)
            }
        }
    }
}

private fun getBlocksForCategory(category: BlockCategory): List<ProgrammingBlock> {
    return when (category) {
        BlockCategory.EVENTS -> listOf(
            ProgrammingBlock("e1", category, "onCreate", "When Activity Created", 
                "override fun onCreate(savedInstanceState: Bundle?) {\n  super.onCreate(savedInstanceState)\n}"),
            ProgrammingBlock("e2", category, "onStart", "When Activity Starts",
                "override fun onStart() {\n  super.onStart()\n}"),
            ProgrammingBlock("e3", category, "onClick", "When Button Clicked",
                "button.setOnClickListener {\n\n}")
        )
        BlockCategory.CONTROL -> listOf(
            ProgrammingBlock("c1", category, "if", "If ... then",
                "if (condition) {\n\n}"),
            ProgrammingBlock("c2", category, "ifElse", "If ... then ... else",
                "if (condition) {\n\n} else {\n\n}"),
            ProgrammingBlock("c3", category, "for", "For each ... in ...",
                "for (item in list) {\n\n}"),
            ProgrammingBlock("c4", category, "while", "While ... do",
                "while (condition) {\n\n}")
        )
        BlockCategory.VARIABLES -> listOf(
            ProgrammingBlock("v1", category, "setVar", "Set variable",
                "var variableName = value"),
            ProgrammingBlock("v2", category, "getVar", "Get variable",
                "variableName"),
            ProgrammingBlock("v3", category, "increment", "Increment by 1",
                "variableName++")
        )
        BlockCategory.FUNCTIONS -> listOf(
            ProgrammingBlock("f1", category, "defineFunc", "Define function",
                "fun functionName() {\n\n}"),
            ProgrammingBlock("f2", category, "callFunc", "Call function",
                "functionName()"),
            ProgrammingBlock("f3", category, "return", "Return value",
                "return value")
        )
        BlockCategory.COMPONENTS -> listOf(
            ProgrammingBlock("co1", category, "setText", "Set Text",
                "textView.text = \"text\""),
            ProgrammingBlock("co2", category, "showToast", "Show Toast",
                "Toast.makeText(context, \"message\", Toast.LENGTH_SHORT).show()"),
            ProgrammingBlock("co3", category, "navigate", "Navigate to Screen",
                "// Navigate to screen")
        )
        BlockCategory.LOGIC -> listOf(
            ProgrammingBlock("l1", category, "and", "And",
                "condition1 && condition2"),
            ProgrammingBlock("l2", category, "or", "Or",
                "condition1 || condition2"),
            ProgrammingBlock("l3", category, "not", "Not",
                "!condition"),
            ProgrammingBlock("l4", category, "equals", "Equals",
                "value1 == value2")
        )
        BlockCategory.MATH -> listOf(
            ProgrammingBlock("m1", category, "add", "Add",
                "a + b"),
            ProgrammingBlock("m2", category, "subtract", "Subtract",
                "a - b"),
            ProgrammingBlock("m3", category, "multiply", "Multiply",
                "a * b"),
            ProgrammingBlock("m4", category, "divide", "Divide",
                "a / b")
        )
        BlockCategory.TEXT -> listOf(
            ProgrammingBlock("t1", category, "concat", "Join text",
                "text1 + text2"),
            ProgrammingBlock("t2", category, "length", "Text length",
                "text.length"),
            ProgrammingBlock("t3", category, "substring", "Get substring",
                "text.substring(start, end)")
        )
    }
}

private fun generateCode(blocks: List<ProgrammingBlock>): String {
    val sb = StringBuilder()
    sb.appendLine("// Auto-generated code from Block Editor")
    sb.appendLine("package com.example.generated")
    sb.appendLine()
    
    blocks.forEach { block ->
        sb.appendLine(block.code)
        sb.appendLine()
    }
    
    return sb.toString()
}

// LazyColumn from Material3
@Composable
private fun LazyColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
