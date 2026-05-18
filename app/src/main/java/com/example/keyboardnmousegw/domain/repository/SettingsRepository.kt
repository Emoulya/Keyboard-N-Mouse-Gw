package com.example.keyboardnmousegw.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // Membaca data secara reaktif menggunakan Flow
    val pointerSpeed: Flow<Float>
    val scrollSpeed: Flow<Float>
    val isVibrationEnabled: Flow<Boolean>

    // Fungsi untuk mengubah data
    suspend fun setPointerSpeed(speed: Float)
    suspend fun setScrollSpeed(speed: Float)
    suspend fun setVibrationEnabled(enabled: Boolean)
}