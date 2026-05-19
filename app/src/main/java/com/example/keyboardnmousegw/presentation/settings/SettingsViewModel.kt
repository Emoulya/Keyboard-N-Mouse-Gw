package com.example.keyboardnmousegw.presentation.settings

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keyboardnmousegw.data.bluetooth.BluetoothHidManager
import com.example.keyboardnmousegw.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val hidManager: BluetoothHidManager
) : ViewModel() {

    // ==========================================
    // 1. DATASTORE STATE (Konfigurasi Aplikasi)
    // ==========================================

    val pointerSpeed: StateFlow<Float> = repository.pointerSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val scrollSpeed: StateFlow<Float> = repository.scrollSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val isVibrationEnabled: StateFlow<Boolean> = repository.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


    // ==========================================
    // 2. BLUETOOTH STATE (Pemindaian & Koneksi)
    // ==========================================

    val scannedDevices: StateFlow<Set<BluetoothDevice>> = hidManager.scannedDevices
    val isScanning: StateFlow<Boolean> = hidManager.isScanning
    val connectionState: StateFlow<Int> = hidManager.connectionState
    val connectedDevice: StateFlow<BluetoothDevice?> = hidManager.connectedDevice


    // ==========================================
    // 3. EVENT HANDLERS (Untuk dipanggil oleh UI)
    // ==========================================

    // --- DataStore Handlers ---
    fun updatePointerSpeed(speed: Float) {
        viewModelScope.launch { repository.setPointerSpeed(speed) }
    }

    fun updateScrollSpeed(speed: Float) {
        viewModelScope.launch { repository.setScrollSpeed(speed) }
    }

    fun updateVibration(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }

    // --- Bluetooth Handlers ---
    fun startBluetoothScan() {
        hidManager.startScanning()
    }

    fun stopBluetoothScan() {
        hidManager.stopScanning()
    }

    fun connectToDevice(device: BluetoothDevice) {
        hidManager.connectDevice(device)
    }
}

// ==========================================
// FACTORY (Dependency Injection Manual)
// ==========================================
class SettingsViewModelFactory(
    private val repository: SettingsRepository,
    private val hidManager: BluetoothHidManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, hidManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}