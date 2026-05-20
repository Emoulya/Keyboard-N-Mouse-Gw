package com.example.keyboardnmousegw.domain.models

/**
 * Mapping lengkap USB HID Usage Table untuk keyboard.
 * Referensi: USB HID Usage Tables v1.4 (Section 10 — Keyboard/Keypad Page 0x07)
 */
object HidKeycodes {

    // ==========================================
    // MODIFIER KEYS (Byte 0 dari Keyboard Report)
    // ==========================================
    const val MOD_NONE: Byte = 0x00
    const val MOD_LEFT_CTRL: Byte = 0x01
    const val MOD_LEFT_SHIFT: Byte = 0x02
    const val MOD_LEFT_ALT: Byte = 0x04
    const val MOD_LEFT_GUI: Byte = 0x08 // Windows / Command key
    const val MOD_RIGHT_CTRL: Byte = 0x10
    const val MOD_RIGHT_SHIFT: Byte = 0x20
    const val MOD_RIGHT_ALT: Byte = 0x40

    // ==========================================
    // LETTER KEYS (a–z → 0x04–0x1D)
    // ==========================================
    const val KEY_A: Byte = 0x04
    const val KEY_B: Byte = 0x05
    const val KEY_C: Byte = 0x06
    const val KEY_D: Byte = 0x07
    const val KEY_E: Byte = 0x08
    const val KEY_F: Byte = 0x09
    const val KEY_G: Byte = 0x0A
    const val KEY_H: Byte = 0x0B
    const val KEY_I: Byte = 0x0C
    const val KEY_J: Byte = 0x0D
    const val KEY_K: Byte = 0x0E
    const val KEY_L: Byte = 0x0F
    const val KEY_M: Byte = 0x10
    const val KEY_N: Byte = 0x11
    const val KEY_O: Byte = 0x12
    const val KEY_P: Byte = 0x13
    const val KEY_Q: Byte = 0x14
    const val KEY_R: Byte = 0x15
    const val KEY_S: Byte = 0x16
    const val KEY_T: Byte = 0x17
    const val KEY_U: Byte = 0x18
    const val KEY_V: Byte = 0x19
    const val KEY_W: Byte = 0x1A
    const val KEY_X: Byte = 0x1B
    const val KEY_Y: Byte = 0x1C
    const val KEY_Z: Byte = 0x1D

    // ==========================================
    // NUMBER KEYS (1–9, 0 → 0x1E–0x27)
    // ==========================================
    const val KEY_1: Byte = 0x1E
    const val KEY_2: Byte = 0x1F
    const val KEY_3: Byte = 0x20
    const val KEY_4: Byte = 0x21
    const val KEY_5: Byte = 0x22
    const val KEY_6: Byte = 0x23
    const val KEY_7: Byte = 0x24
    const val KEY_8: Byte = 0x25
    const val KEY_9: Byte = 0x26
    const val KEY_0: Byte = 0x27

    // ==========================================
    // SPECIAL KEYS
    // ==========================================
    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESCAPE: Byte = 0x29
    const val KEY_BACKSPACE: Byte = 0x2A
    const val KEY_TAB: Byte = 0x2B
    const val KEY_SPACE: Byte = 0x2C

    // ==========================================
    // SYMBOL KEYS (tanpa Shift / dengan Shift)
    // ==========================================
    const val KEY_MINUS: Byte = 0x2D         // -  / _
    const val KEY_EQUAL: Byte = 0x2E         // =  / +
    const val KEY_LEFT_BRACKET: Byte = 0x2F  // [  / {
    const val KEY_RIGHT_BRACKET: Byte = 0x30 // ]  / }
    const val KEY_BACKSLASH: Byte = 0x31     // \  / |
    const val KEY_SEMICOLON: Byte = 0x33     // ;  / :
    const val KEY_APOSTROPHE: Byte = 0x34    // '  / "
    const val KEY_GRAVE: Byte = 0x35         // `  / ~
    const val KEY_COMMA: Byte = 0x36         // ,  / <
    const val KEY_PERIOD: Byte = 0x37        // .  / >
    const val KEY_SLASH: Byte = 0x38         // /  / ?

    // ==========================================
    // NAVIGATION & FUNCTION KEYS
    // ==========================================
    const val KEY_CAPS_LOCK: Byte = 0x39
    const val KEY_DELETE: Byte = 0x4C
    const val KEY_RIGHT_ARROW: Byte = 0x4F
    const val KEY_LEFT_ARROW: Byte = 0x50
    const val KEY_DOWN_ARROW: Byte = 0x51
    const val KEY_UP_ARROW: Byte = 0x52

    /**
     * Konversi karakter ke pasangan (modifier, keycode) untuk HID Report.
     * @return Pair(modifier, keycode) atau null jika karakter tidak memiliki mapping.
     */
    fun charToHidKey(char: Char): Pair<Byte, Byte>? {
        return when (char) {
            // Huruf kecil
            in 'a'..'z' -> MOD_NONE to (KEY_A + (char - 'a')).toByte()
            // Huruf besar (perlu Shift)
            in 'A'..'Z' -> MOD_LEFT_SHIFT to (KEY_A + (char - 'A')).toByte()
            // Angka
            in '1'..'9' -> MOD_NONE to (KEY_1 + (char - '1')).toByte()
            '0' -> MOD_NONE to KEY_0
            // Whitespace
            ' ' -> MOD_NONE to KEY_SPACE
            '\n' -> MOD_NONE to KEY_ENTER
            '\t' -> MOD_NONE to KEY_TAB
            // Simbol tanpa Shift
            '-' -> MOD_NONE to KEY_MINUS
            '=' -> MOD_NONE to KEY_EQUAL
            '[' -> MOD_NONE to KEY_LEFT_BRACKET
            ']' -> MOD_NONE to KEY_RIGHT_BRACKET
            '\\' -> MOD_NONE to KEY_BACKSLASH
            ';' -> MOD_NONE to KEY_SEMICOLON
            '\'' -> MOD_NONE to KEY_APOSTROPHE
            '`' -> MOD_NONE to KEY_GRAVE
            ',' -> MOD_NONE to KEY_COMMA
            '.' -> MOD_NONE to KEY_PERIOD
            '/' -> MOD_NONE to KEY_SLASH
            // Simbol dengan Shift
            '!' -> MOD_LEFT_SHIFT to KEY_1
            '@' -> MOD_LEFT_SHIFT to KEY_2
            '#' -> MOD_LEFT_SHIFT to KEY_3
            '$' -> MOD_LEFT_SHIFT to KEY_4
            '%' -> MOD_LEFT_SHIFT to KEY_5
            '^' -> MOD_LEFT_SHIFT to KEY_6
            '&' -> MOD_LEFT_SHIFT to KEY_7
            '*' -> MOD_LEFT_SHIFT to KEY_8
            '(' -> MOD_LEFT_SHIFT to KEY_9
            ')' -> MOD_LEFT_SHIFT to KEY_0
            '_' -> MOD_LEFT_SHIFT to KEY_MINUS
            '+' -> MOD_LEFT_SHIFT to KEY_EQUAL
            '{' -> MOD_LEFT_SHIFT to KEY_LEFT_BRACKET
            '}' -> MOD_LEFT_SHIFT to KEY_RIGHT_BRACKET
            '|' -> MOD_LEFT_SHIFT to KEY_BACKSLASH
            ':' -> MOD_LEFT_SHIFT to KEY_SEMICOLON
            '"' -> MOD_LEFT_SHIFT to KEY_APOSTROPHE
            '~' -> MOD_LEFT_SHIFT to KEY_GRAVE
            '<' -> MOD_LEFT_SHIFT to KEY_COMMA
            '>' -> MOD_LEFT_SHIFT to KEY_PERIOD
            '?' -> MOD_LEFT_SHIFT to KEY_SLASH
            else -> null
        }
    }
}