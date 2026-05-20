package com.example.keyboardnmousegw.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keyboardnmousegw.data.bluetooth.BluetoothHidManager
import com.example.keyboardnmousegw.data.haptic.HapticFeedbackHelper
import com.example.keyboardnmousegw.domain.models.HidKeycodes
import com.example.keyboardnmousegw.domain.models.MouseReport
import com.example.keyboardnmousegw.domain.models.KeyboardReport
import com.example.keyboardnmousegw.domain.repository.SettingsRepository
import com.example.keyboardnmousegw.presentation.components.HidKeyboardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val hidManager: BluetoothHidManager,
    private val settingsRepository: SettingsRepository,
    private val hapticHelper: HapticFeedbackHelper
) : ViewModel() {

    private val mouseReport = MouseReport()
    private val keyboardReport = KeyboardReport()

    val isBluetoothEnabled = hidManager.isBluetoothEnabled

    // Settings yang digunakan saat runtime
    private val pointerSpeed = settingsRepository.pointerSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    private val scrollSpeed = settingsRepository.scrollSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    // State vibration
    private val isVibrationEnabled = settingsRepository.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    private companion object {
        const val SMOOTH_FACTOR = 0.45f
    }

    // State EMA
    private var smoothDx = 0f
    private var smoothDy = 0f

    // Accumulator
    private var accDx = 0f
    private var accDy = 0f

    // ==========================================
    // MOUSE — Pergerakan & Klik
    // ==========================================

    /**
     * Pergerakan pointer mouse dengan sub-pixel accumulation + EMA smoothing.
     * Kecepatan di-scale berdasarkan pointerSpeed dari Settings.
     */
    fun moveMouse(dx: Float, dy: Float) {
        // Skala kecepatan: pointerSpeed 0.0–1.0 → multiplier 0.5×–3.0×
        val multiplier = 0.5f + (pointerSpeed.value * 2.5f)
        val scaledDx = dx * multiplier
        val scaledDy = dy * multiplier

        // Langkah 1: EMA — haluskan input yang jittery dari sensor sentuh
        smoothDx = smoothDx * (1f - SMOOTH_FACTOR) + scaledDx * SMOOTH_FACTOR
        smoothDy = smoothDy * (1f - SMOOTH_FACTOR) + scaledDy * SMOOTH_FACTOR

        // Langkah 2: Akumulasi sub-pixel — jangan buang sisa desimal
        accDx += smoothDx
        accDy += smoothDy

        // Langkah 3: Kirim ke HID hanya jika sudah cukup untuk 1 pixel penuh
        val sendDx = accDx.toInt()
        val sendDy = accDy.toInt()

        if (sendDx != 0 || sendDy != 0) {
            accDx -= sendDx
            accDy -= sendDy

            mouseReport.dx = sendDx.coerceIn(-127, 127).toByte()
            mouseReport.dy = sendDy.coerceIn(-127, 127).toByte()
            hidManager.sendMouseReport(mouseReport.toByteArray())
            mouseReport.resetMovement()
        }
    }

    /**
     * Reset state smoothing saat jari diangkat dari trackpad.
     * Penting: tanpa ini, EMA akan "membawa" momentum dari gestur sebelumnya
     * ke awal gestur berikutnya → kursor bergerak sendiri sesaat saat jari
     * baru menyentuh layar.
     */
    fun resetMovementSmoothing() {
        smoothDx = 0f
        smoothDy = 0f
        accDx = 0f
        accDy = 0f
    }

    /**
     * Klik kiri dengan jeda antara press dan release.
     * Delay diperlukan agar host (PC/Laptop) mendeteksi event click dengan benar.
     */
    fun clickLeft() {
        viewModelScope.launch {
            mouseReport.leftClick = true
            hidManager.sendMouseReport(mouseReport.toByteArray())
            triggerHaptic()
            delay(20)
            mouseReport.leftClick = false
            hidManager.sendMouseReport(mouseReport.toByteArray())
        }
    }

    /** Klik kanan dengan jeda. */
    fun clickRight() {
        viewModelScope.launch {
            mouseReport.rightClick = true
            hidManager.sendMouseReport(mouseReport.toByteArray())
            triggerHaptic()
            delay(20)
            mouseReport.rightClick = false
            hidManager.sendMouseReport(mouseReport.toByteArray())
        }
    }

    /** Scroll vertikal. Kecepatan di-scale berdasarkan scrollSpeed dari Settings. */
    fun scroll(dy: Float) {
        val divisor = 10f - (scrollSpeed.value * 8f)
        val scaledScroll = (dy / divisor).toInt()

        if (scaledScroll != 0) {
            mouseReport.vScroll = scaledScroll.coerceIn(-127, 127).toByte()
            hidManager.sendMouseReport(mouseReport.toByteArray())
            mouseReport.resetMovement()
        }
    }

    // ==========================================
    // KEYBOARD — Karakter & Shortcut
    // ==========================================

    fun sendKeyPress(char: Char) {
        val hidKey = HidKeycodes.charToHidKey(char) ?: return
        viewModelScope.launch {
            keyboardReport.modifier = hidKey.first
            keyboardReport.keys[0] = hidKey.second
            hidManager.sendKeyboardReport(keyboardReport.toByteArray())
            delay(10)
            keyboardReport.clearKeys()
            hidManager.sendKeyboardReport(keyboardReport.toByteArray())
        }
    }

    fun sendSpecialKey(androidKeyCode: Int) {
        val hidKeyCode = HidKeyboardView.androidKeyToHid(androidKeyCode) ?: return
        viewModelScope.launch {
            keyboardReport.modifier = HidKeycodes.MOD_NONE
            keyboardReport.keys[0] = hidKeyCode
            hidManager.sendKeyboardReport(keyboardReport.toByteArray())
            delay(10)
            keyboardReport.clearKeys()
            hidManager.sendKeyboardReport(keyboardReport.toByteArray())
        }
    }

    fun sendShortcut(modifier: Byte, keyCode: Byte) {
        viewModelScope.launch {
            keyboardReport.modifier = modifier
            keyboardReport.keys[0] = keyCode
            hidManager.sendKeyboardReport(keyboardReport.toByteArray())
            delay(20)
            keyboardReport.clearKeys()
            hidManager.sendKeyboardReport(keyboardReport.toByteArray())
        }
    }

    // ==========================================
    // HAPTIC FEEDBACK
    // ==========================================

    private fun triggerHaptic() {
        if (isVibrationEnabled.value) {
            hapticHelper.performClick()
        }
    }
}

// Factory untuk Dependency Injection Manual
class MainViewModelFactory(
    private val hidManager: BluetoothHidManager,
    private val settingsRepository: SettingsRepository,
    private val hapticHelper: HapticFeedbackHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(hidManager, settingsRepository, hapticHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}