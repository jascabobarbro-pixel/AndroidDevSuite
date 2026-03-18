/**
 * Android Development Suite - Theme
 * منصة تطوير أندرويد الشاملة
 * 
 * Material Design 3 Theme with dynamic colors support
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */
package com.androiddevsuite.ui.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Primary colors
val Primary = androidx.compose.ui.graphics.Color(0xFFFF6B6B)
val PrimaryVariant = androidx.compose.ui.graphics.Color(0xFFE55555)
val Secondary = androidx.compose.ui.graphics.Color(0xFF4ECDC4)
val SecondaryVariant = androidx.compose.ui.graphics.Color(0xFF3DBDB5)
val Tertiary = androidx.compose.ui.graphics.Color(0xFFFFE66D)

// Background colors (Dark theme)
val BackgroundDark = androidx.compose.ui.graphics.Color(0xFF0D1117)
val SurfaceDark = androidx.compose.ui.graphics.Color(0xFF161B22)
val SurfaceVariantDark = androidx.compose.ui.graphics.Color(0xFF21262D)

// Background colors (Light theme)
val BackgroundLight = androidx.compose.ui.graphics.Color(0xFFF6F8FA)
val SurfaceLight = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
val SurfaceVariantLight = androidx.compose.ui.graphics.Color(0xFFF0F0F0)

// Text colors
val OnBackgroundDark = androidx.compose.ui.graphics.Color(0xFFC9D1D9)
val OnSurfaceDark = androidx.compose.ui.graphics.Color(0xFFC9D1D9)
val OnBackgroundLight = androidx.compose.ui.graphics.Color(0xFF1F2328)
val OnSurfaceLight = androidx.compose.ui.graphics.Color(0xFF1F2328)

// Status colors
val Success = androidx.compose.ui.graphics.Color(0xFF3FB950)
val Warning = androidx.compose.ui.graphics.Color(0xFFD29922)
val Error = androidx.compose.ui.graphics.Color(0xFFF85149)
val Info = androidx.compose.ui.graphics.Color(0xFF58A6FF)

// Code editor colors
val CodeKeyword = androidx.compose.ui.graphics.Color(0xFFFF7B72)
val CodeString = androidx.compose.ui.graphics.Color(0xFFA5D6FF)
val CodeComment = androidx.compose.ui.graphics.Color(0xFF8B949E)
val CodeNumber = androidx.compose.ui.graphics.Color(0xFF79C0FF)
val CodeFunction = androidx.compose.ui.graphics.Color(0xFFD2A8FF)
val CodeClass = androidx.compose.ui.graphics.Color(0xFF7EE787)
val CodeVariable = androidx.compose.ui.graphics.Color(0xFFFFA657)

// Block editor colors
val BlockEvent = androidx.compose.ui.graphics.Color(0xFFFF6B6B)
val BlockControl = androidx.compose.ui.graphics.Color(0xFF4ECDC4)
val BlockOperation = androidx.compose.ui.graphics.Color(0xFFFFE66D)
val BlockVariable = androidx.compose.ui.graphics.Color(0xFFA78BFA)
val BlockFunction = androidx.compose.ui.graphics.Color(0xFFF472B6)
val BlockComponent = androidx.compose.ui.graphics.Color(0xFF60A5FA)
val BlockLogic = androidx.compose.ui.graphics.Color(0xFF34D399)

/**
 * Dark color scheme for Material Design 3.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = Secondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = androidx.compose.ui.graphics.Color.White,
    tertiary = Tertiary,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF8B949E),
    error = Error,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = androidx.compose.ui.graphics.Color(0xFF4D1F1F),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    outline = androidx.compose.ui.graphics.Color(0xFF30363D),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF21262D),
    scrim = androidx.compose.ui.graphics.Color.Black,
    inverseSurface = androidx.compose.ui.graphics.Color(0xFFE6EDF3),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFF1F2328),
    inversePrimary = PrimaryVariant
)

/**
 * Light color scheme for Material Design 3.
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF410002),
    secondary = Secondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD0E8E4),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF00201D),
    tertiary = Tertiary,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF5F6368),
    error = Error,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFF410002),
    outline = androidx.compose.ui.graphics.Color(0xFFD0D7DE),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFE5E7EB),
    scrim = androidx.compose.ui.graphics.Color.Black,
    inverseSurface = androidx.compose.ui.graphics.Color(0xFF1F2328),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFFE6EDF3),
    inversePrimary = Primary
)

/**
 * Main application theme with Material Design 3.
 * Supports dynamic colors on Android 12+ and custom color schemes.
 * 
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic colors (Material You)
 * @param content The content to apply the theme to
 */
@Composable
fun AndroidDevSuiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
