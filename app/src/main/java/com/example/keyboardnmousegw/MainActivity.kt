package com.example.keyboardnmousegw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.keyboardnmousegw.presentation.main.MainScreen
import com.example.keyboardnmousegw.presentation.theme.KeyboardNMouseGwTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            KeyboardNMouseGwTheme {
                MainScreen()
            }
        }
    }
}