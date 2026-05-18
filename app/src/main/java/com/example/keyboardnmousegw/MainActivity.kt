package com.example.keyboardnmousegw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.keyboardnmousegw.presentation.main.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Membuat aplikasi fullscreen (transparan di status bar & navigation bar)
        enableEdgeToEdge()

        setContent {
            // Kita bisa wrap dengan Theme khusus nanti,
            // untuk sekarang panggil langsung MainScreen
            MainScreen()
        }
    }
}