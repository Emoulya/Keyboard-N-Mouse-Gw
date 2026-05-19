package com.example.keyboardnmousegw.domain.models

/**
 * Merepresentasikan state dari Keyboard.
 */
data class KeyboardReport(
    // Byte untuk modifier (Ctrl, Shift, Alt, Windows/Mac key)
    var modifier: Byte = 0,
    // Array maksimal 6 tombol ditekan bersamaan (N-Key Rollover standar)
    val keys: ByteArray = ByteArray(6) { 0 }
) {
    fun toByteArray(): ByteArray {
        val report = ByteArray(8)
        report[0] = modifier
        report[1] = 0 // Byte 1 selalu 0 (Reserved)
        // Salin 6 byte keycode ke dalam report mulai dari index ke-2
        System.arraycopy(keys, 0, report, 2, 6)
        return report
    }

    // Helper untuk membersihkan array setelah tombol dilepas
    fun clearKeys() {
        modifier = 0
        for (i in keys.indices) {
            keys[i] = 0
        }
    }
}