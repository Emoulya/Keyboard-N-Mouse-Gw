package com.example.keyboardnmousegw.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.keyboardnmousegw.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    // Mengonversi Flow menjadi StateFlow agar mudah diamati oleh UI (Jetpack Compose)
    val pointerSpeed: StateFlow<Float> = repository.pointerSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val scrollSpeed: StateFlow<Float> = repository.scrollSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val isVibrationEnabled: StateFlow<Boolean> = repository.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun updatePointerSpeed(speed: Float) {
        viewModelScope.launch { repository.setPointerSpeed(speed) }
    }

    fun updateScrollSpeed(speed: Float) {
        viewModelScope.launch { repository.setScrollSpeed(speed) }
    }

    fun updateVibration(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }
}

// Factory manual karena kita belum menggunakan Dependency Injection library (seperti Hilt/Dagger)
class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}