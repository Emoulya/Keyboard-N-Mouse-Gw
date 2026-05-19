package com.example.keyboardnmousegw.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keyboardnmousegw.data.bluetooth.BluetoothHidManager
import com.example.keyboardnmousegw.domain.models.MouseReport
import com.example.keyboardnmousegw.domain.models.KeyboardReport

class MainViewModel(
    private val hidManager: BluetoothHidManager
) : ViewModel() {

    private val mouseReport = MouseReport()
    private val keyboardReport = KeyboardReport()

    // Fungsi untuk pergerakan mouse (Trackpad)
    fun moveMouse(dx: Float, dy: Float) {
        val byteDx = dx.toInt().coerceIn(-127, 127).toByte()
        val byteDy = dy.toInt().coerceIn(-127, 127).toByte()

        mouseReport.dx = byteDx
        mouseReport.dy = byteDy

        // Kirim data ke Bluetooth Manager
        hidManager.sendMouseReport(mouseReport.toByteArray())

        // Reset X & Y agar kursor tidak jalan terus setelah jari berhenti
        mouseReport.resetMovement()
    }

    // Fungsi untuk Klik
    fun setLeftClick(isPressed: Boolean) {
        mouseReport.leftClick = isPressed
        hidManager.sendMouseReport(mouseReport.toByteArray())
    }

    fun setRightClick(isPressed: Boolean) {
        mouseReport.rightClick = isPressed
        hidManager.sendMouseReport(mouseReport.toByteArray())
    }

    // --- LOGIKA SCROLL ---
    fun scroll(dy: Float) {
        val scaledScroll = (dy / 5).toInt()

        if (scaledScroll != 0) {
            mouseReport.vScroll = scaledScroll.coerceIn(-127, 127).toByte()
            hidManager.sendMouseReport(mouseReport.toByteArray())
            mouseReport.resetMovement()
        }
    }

    // --- LOGIKA KEYBOARD SHORTCUT ---
    fun sendShortcut(modifier: Byte, keyCode: Byte) {
        keyboardReport.modifier = modifier
        keyboardReport.keys[0] = keyCode
        hidManager.sendKeyboardReport(keyboardReport.toByteArray())
        keyboardReport.clearKeys()
        hidManager.sendKeyboardReport(keyboardReport.toByteArray())
    }
}

// Factory untuk Dependency Injection Manual
class MainViewModelFactory(
    private val hidManager: BluetoothHidManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(hidManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}