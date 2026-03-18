/**
 * Android Development Suite - Block Editor Screen
 * منصة تطوير أندرويد الشاملة
 * 
 * Visual block-based programming (Sketchware-like)
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.blocks

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

/**
 * Block types for visual programming.
 */
enum class BlockType(val color: Color) {
    EVENT(Color(0xFFFF6B6B)),
    CONTROL(Color(0xFF4ECDC4)),
    OPERATION(Color(0xFFFFE66D)),
    VARIABLE(Color(0xFFA78BFA)),
    FUNCTION(Color(0xFFF472B6)),
    COMPONENT(Color(0xFF60A5FA)),
    LOGIC(Color(0xFF34D399))
}

/**
 * Block data class.
 */
data class Block(
    val id: String,
    val type: BlockType,
    val label: String,
    val children: List<Block> = emptyList()
)

/**
 * Block Editor Screen - Visual programming interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockEditorScreen(
    onExportCode: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val blockCategories = listOf(
        "Events" to BlockType.EVENT,
        "Control" to BlockType.CONTROL,
        "Variables" to BlockType.VARIABLE,
        "Functions" to BlockType.FUNCTION,
        "Components" to BlockType.COMPONENT,
        "Logic" to BlockType.LOGIC
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Block Editor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { /* Export */ }) {
                        Icon(Icons.Filled.Code, contentDescription = "View Code")
                    }
                    IconButton(onClick = { /* Run */ }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Run")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Block palette (left panel)
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Category tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        edgePadding = 0.dp
                    ) {
                        blockCategories.forEachIndexed { index, (name, type) ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { 
                                    Text(
                                        name,
                                        color = type.color
                                    ) 
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Block list
                    val blocks = getBlocksForCategory(blockCategories[selectedTab].second)
                    
                    blocks.forEach { block ->
                        DraggableBlock(block = block)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            
            // Workspace (right panel)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Drag blocks here to build your program",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sample placed block
                    PlacedBlock(
                        block = Block(
                            id = "1",
                            type = BlockType.EVENT,
                            label = "When Activity Created",
                            children = listOf(
                                Block(
                                    id = "2",
                                    type = BlockType.CONTROL,
                                    label = "If true then"
                                )
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableBlock(block: Block) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = block.type.color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(block.type.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = block.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlacedBlock(block: Block, depth: Int = 0) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 24).dp),
        colors = CardDefaults.cardColors(
            containerColor = block.type.color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = block.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (block.children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                block.children.forEach { child ->
                    PlacedBlock(child, depth + 1)
                }
            }
        }
    }
}

private fun getBlocksForCategory(type: BlockType): List<Block> {
    return when (type) {
        BlockType.EVENT -> listOf(
            Block("e1", BlockType.EVENT, "When Activity Created"),
            Block("e2", BlockType.EVENT, "When Button Clicked"),
            Block("e3", BlockType.EVENT, "When App Starts")
        )
        BlockType.CONTROL -> listOf(
            Block("c1", BlockType.CONTROL, "If ... then"),
            Block("c2", BlockType.CONTROL, "If ... then ... else"),
            Block("c3", BlockType.CONTROL, "For each ... in ..."),
            Block("c4", BlockType.CONTROL, "While ... do")
        )
        BlockType.VARIABLE -> listOf(
            Block("v1", BlockType.VARIABLE, "Set variable"),
            Block("v2", BlockType.VARIABLE, "Get variable"),
            Block("v3", BlockType.VARIABLE, "Increment")
        )
        BlockType.FUNCTION -> listOf(
            Block("f1", BlockType.FUNCTION, "Define function"),
            Block("f2", BlockType.FUNCTION, "Call function"),
            Block("f3", BlockType.FUNCTION, "Return value")
        )
        BlockType.COMPONENT -> listOf(
            Block("co1", BlockType.COMPONENT, "Set Text"),
            Block("co2", BlockType.COMPONENT, "Show Toast"),
            Block("co3", BlockType.COMPONENT, "Navigate to Screen")
        )
        BlockType.LOGIC -> listOf(
            Block("l1", BlockType.LOGIC, "And"),
            Block("l2", BlockType.LOGIC, "Or"),
            Block("l3", BlockType.LOGIC, "Not"),
            Block("l4", BlockType.LOGIC, "Equals")
        )
        BlockType.OPERATION -> listOf(
            Block("o1", BlockType.OPERATION, "Add"),
            Block("o2", BlockType.OPERATION, "Subtract"),
            Block("o3", BlockType.OPERATION, "Multiply")
        )
    }
}
