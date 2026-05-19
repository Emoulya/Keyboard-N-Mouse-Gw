package com.example.keyboardnmousegw.domain.models

/**
 * Merepresentasikan state dari Mouse.
 * Memenuhi prinsip DRY: Logika konversi bitwise dilakukan secara terpusat di sini.
 */
data class MouseReport(
    var leftClick: Boolean = false,
    var rightClick: Boolean = false,
    var middleClick: Boolean = false,
    var dx: Byte = 0, // Pergerakan sumbu X (-127 sampai 127)
    var dy: Byte = 0, // Pergerakan sumbu Y (-127 sampai 127)
    var vScroll: Byte = 0 // Scroll wheel
) {
    fun toByteArray(): ByteArray {
        var buttons = 0
        // Menyusun bit: Kiri = bit 0, Kanan = bit 1, Tengah = bit 2
        if (leftClick) buttons = buttons or 0x01
        if (rightClick) buttons = buttons or 0x02
        if (middleClick) buttons = buttons or 0x04

        return byteArrayOf(
            buttons.toByte(), // Byte 0: Status Tombol
            dx,               // Byte 1: X (Horizontal)
            dy,               // Byte 2: Y (Vertical)
            vScroll           // Byte 3: Scroll
        )
    }

    // Fungsi helper agar data bisa di-reset dengan cepat setelah dikirim
    fun resetMovement() {
        dx = 0
        dy = 0
        vScroll = 0
    }
}