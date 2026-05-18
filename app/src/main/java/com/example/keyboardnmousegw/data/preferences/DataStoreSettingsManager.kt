package com.example.keyboardnmousegw.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.keyboardnmousegw.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Membuat instance DataStore sebagai properti ekstensi di Context (Singleton bawaan)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

class DataStoreSettingsManager(private val context: Context) : SettingsRepository {

    // Kunci (Keys) untuk menyimpan data
    private object PreferencesKeys {
        val POINTER_SPEED = floatPreferencesKey("pointer_speed")
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    // --- MEMBACA DATA ---
    override val pointerSpeed: Flow<Float> = context.dataStore.data
        .handleErrors()
        .map { preferences -> preferences[PreferencesKeys.POINTER_SPEED] ?: 0.5f } // Default 0.5f

    override val scrollSpeed: Flow<Float> = context.dataStore.data
        .handleErrors()
        .map { preferences -> preferences[PreferencesKeys.SCROLL_SPEED] ?: 0.5f } // Default 0.5f

    override val isVibrationEnabled: Flow<Boolean> = context.dataStore.data
        .handleErrors()
        .map { preferences -> preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true } // Default true

    // --- MENYIMPAN DATA ---
    override suspend fun setPointerSpeed(speed: Float) {
        context.dataStore.edit { it[PreferencesKeys.POINTER_SPEED] = speed }
    }

    override suspend fun setScrollSpeed(speed: Float) {
        context.dataStore.edit { it[PreferencesKeys.SCROLL_SPEED] = speed }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    // Ekstensi Helper untuk menangani error saat membaca file
    private fun Flow<Preferences>.handleErrors(): Flow<Preferences> = catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
}