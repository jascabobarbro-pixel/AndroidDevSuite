/**
 * Android Development Suite - Shapes
 * Material Design 3 Shape configuration
 */
package com.androiddevsuite.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Application shapes following Material Design 3 guidelines.
 * Uses rounded corners for a modern, friendly appearance.
 */
val AppShapes = Shapes(
    // Extra small - for chips, small buttons
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - for text fields, cards
    small = RoundedCornerShape(8.dp),
    
    // Medium - for cards, dialogs
    medium = RoundedCornerShape(12.dp),
    
    // Large - for bottom sheets, large cards
    large = RoundedCornerShape(16.dp),
    
    // Extra large - for navigation drawers
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Block editor specific shapes.
 * Uses more rounded corners for a playful, visual programming feel.
 */
val BlockShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
