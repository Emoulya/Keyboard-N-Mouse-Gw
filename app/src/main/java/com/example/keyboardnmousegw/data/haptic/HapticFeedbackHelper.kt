package com.example.keyboardnmousegw.data.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Helper untuk memberikan haptic feedback saat interaksi touch.
 * Menggunakan createOneShot() agar tidak di-ignore oleh system setting "Touch Feedback".
 */
class HapticFeedbackHelper(context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /** Efek klik singkat — cocok untuk tap dan button press. */
    fun performClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30L)
            }
        } catch (_: SecurityException) {
            // Abaikan jika VIBRATE permission belum diberikan
        }
    }
}
