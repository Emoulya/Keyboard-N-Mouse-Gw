package com.example.keyboardnmousegw.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pemetaan warna ke standar Material 3
private val DarkColorScheme = darkColorScheme(
    background = AppBackground,
    surface = SettingsBackground,
    surfaceVariant = TrackpadSurface,
    primary = ActionButtonColor,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onPrimary = TextWhite,
    tertiary = AccentCyan
)

@Composable
fun KeyboardNMouseGwTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    // Mewarnai status bar sesuai background jika sedang tidak fullscreen
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
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