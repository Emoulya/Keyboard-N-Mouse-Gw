package com.example.keyboardnmousegw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.keyboardnmousegw.data.preferences.DataStoreSettingsManager
import com.example.keyboardnmousegw.data.haptic.HapticFeedbackHelper
import com.example.keyboardnmousegw.presentation.components.RequireBluetoothPermissions
import com.example.keyboardnmousegw.presentation.main.MainScreen
import com.example.keyboardnmousegw.presentation.settings.SettingsViewModelFactory
import com.example.keyboardnmousegw.presentation.theme.KeyboardNMouseGwTheme
import com.example.keyboardnmousegw.data.bluetooth.BluetoothHidManager
import com.example.keyboardnmousegw.presentation.main.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothHidManager: BluetoothHidManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 1. Inisialisasi Data Layer yang aman tanpa permission
        val settingsRepository = DataStoreSettingsManager(applicationContext)
        val hapticHelper = HapticFeedbackHelper(applicationContext)

        setContent {
            KeyboardNMouseGwTheme {
                RequireBluetoothPermissions {
                    // Inisialisasi BluetoothHidManager HANYA setelah permission diberikan
                    if (!::bluetoothHidManager.isInitialized) {
                        bluetoothHidManager = BluetoothHidManager(applicationContext)
                    }

                    // Inisialisasi Factories
                    val settingsViewModelFactory = SettingsViewModelFactory(settingsRepository, bluetoothHidManager)
                    val mainViewModelFactory = MainViewModelFactory(bluetoothHidManager, settingsRepository, hapticHelper)

                    MainScreen(
                        settingsViewModelFactory = settingsViewModelFactory,
                        mainViewModelFactory = mainViewModelFactory
                    )
                }
            }
        }
    }

    // Membersihkan resource Bluetooth untuk mencegah Memory Leak saat aplikasi ditutup
    override fun onDestroy() {
        super.onDestroy()
        if (::bluetoothHidManager.isInitialized) {
            bluetoothHidManager.onDestroy()
        }
    }
}