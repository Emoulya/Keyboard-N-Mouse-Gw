package com.example.keyboardnmousegw.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = AppBackground,
    surface = SurfaceDim,
    surfaceVariant = SurfaceContainer,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainer,
    primary = PrimaryViolet,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextMuted,
    onPrimary = TextWhite,
    secondary = AccentLavender,
    secondaryContainer = PrimaryVioletDark,
    tertiary = AccentLavender,
    error = ErrorRed,
    onError = TextWhite,
    outline = Color(0xFF2A2A40)
)

@Composable
fun KeyboardNMouseGwTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}