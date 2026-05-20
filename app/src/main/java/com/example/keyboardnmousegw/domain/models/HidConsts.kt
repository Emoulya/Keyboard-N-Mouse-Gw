package com.example.keyboardnmousegw.domain.models

object HidConsts {
    const val ID_KEYBOARD = 1
    const val ID_MOUSE = 2

    // Peta standar industri (USB HID Class) untuk Keyboard & Mouse.
    val HID_REPORT_DESCRIPTOR = byteArrayOf(
        // --- KEYBOARD (Report ID 1) ---
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x06, // Usage (Keyboard)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x85.toByte(), ID_KEYBOARD.toByte(), // Report ID (1)

        // Modifiers (Ctrl, Shift, Alt, dll) - 1 Byte
        0x05, 0x07, // Usage Page (Key Codes)
        0x19, 0xE0.toByte(), // Usage Minimum (224)
        0x29, 0xE7.toByte(), // Usage Maximum (231)
        0x15, 0x00, // Logical Minimum (0)
        0x25, 0x01, // Logical Maximum (1)
        0x75, 0x01, // Report Size (1 bit)
        0x95.toByte(), 0x08, // Report Count (8 bit)
        0x81.toByte(), 0x02, // Input (Data, Variable, Absolute)

        // Reserved byte - 1 Byte
        0x95.toByte(), 0x01, // Report Count (1)
        0x75, 0x08, // Report Size (8 bit)
        0x81.toByte(), 0x01, // Input (Constant)

        // Key arrays (Tombol yang ditekan) - 6 Byte
        0x95.toByte(), 0x06, // Report Count (6)
        0x75, 0x08, // Report Size (8)
        0x15, 0x00, // Logical Minimum (0)
        0x25, 0x65, // Logical Maximum (101)
        0x05, 0x07, // Usage Page (Key Codes)
        0x19, 0x00, // Usage Minimum (0)
        0x29, 0x65, // Usage Maximum (101)
        0x81.toByte(), 0x00, // Input (Data, Array)
        0xC0.toByte(), // End Collection

        // --- MOUSE (Report ID 2) ---
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x02, // Usage (Mouse)
        0xA1.toByte(), 0x01, // Collection (Application)
        0x09, 0x01, // Usage (Pointer)
        0xA1.toByte(), 0x00, // Collection (Physical)
        0x85.toByte(), ID_MOUSE.toByte(), // Report ID (2)

        // Buttons (Kiri, Kanan, Tengah) - 1 Byte (kita pakai 3 bit pertama)
        0x05, 0x09, // Usage Page (Button)
        0x19, 0x01, // Usage Minimum (1)
        0x29, 0x03, // Usage Maximum (3)
        0x15, 0x00, // Logical Minimum (0)
        0x25, 0x01, // Logical Maximum (1)
        0x95.toByte(), 0x03, // Report Count (3 buttons)
        0x75, 0x01, // Report Size (1 bit)
        0x81.toByte(), 0x02, // Input (Data, Variable, Absolute)
        0x95.toByte(), 0x01, // Report Count (1)
        0x75, 0x05, // Report Size (5 bit padding)
        0x81.toByte(), 0x03, // Input (Constant)

        // X, Y, Wheel - 3 Byte
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x30, // Usage (X)
        0x09, 0x31, // Usage (Y)
        0x09, 0x38, // Usage (Wheel)
        0x15, 0x81.toByte(), // Logical Minimum (-127)
        0x25, 0x7F, // Logical Maximum (127)
        0x75, 0x08, // Report Size (8 bit)
        0x95.toByte(), 0x03, // Report Count (3)
        0x81.toByte(), 0x06, // Input (Data, Variable, Relative)
        0xC0.toByte(), // End Collection (Physical)
        0xC0.toByte()  // End Collection (Application)
    )
}