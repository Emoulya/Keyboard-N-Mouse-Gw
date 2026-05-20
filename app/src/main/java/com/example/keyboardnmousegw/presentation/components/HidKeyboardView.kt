package com.example.keyboardnmousegw.presentation.components

import android.content.Context
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Custom View yang berfungsi sebagai penerima input dari Software Keyboard (IME) Android.
 *
 * View ini bersifat invisible — ukurannya 1dp dan transparan.
 * Tugasnya hanya mengintersep setiap karakter dan key event dari IME,
 * lalu meneruskannya ke ViewModel melalui callback.
 *
 * Keunggulan dibanding TextField biasa:
 * - Menangkap input di level InputConnection (lebih rendah & akurat)
 * - Mendukung special keys (Backspace, Enter, Tab) secara native
 * - Tidak memiliki visual state (cursor, selection) yang tidak diperlukan
 */
class HidKeyboardView(context: Context) : View(context) {

    /** Callback saat user mengetik karakter biasa (a-z, 0-9, simbol). */
    var onCharInput: ((Char) -> Unit)? = null

    /** Callback saat user menekan special key (Backspace, Enter, dll). */
    var onSpecialKey: ((Int) -> Unit)? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onCheckIsTextEditor(): Boolean = true

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_ACTION_NONE
        return HidInputConnection(this, false)
    }

    /**
     * Custom InputConnection yang mengintersep semua input dari IME.
     * Setiap karakter yang di-commit langsung diteruskan sebagai HID report.
     */
    private inner class HidInputConnection(
        targetView: View,
        fullEditor: Boolean
    ) : BaseInputConnection(targetView, fullEditor) {

        override fun commitText(text: CharSequence, newCursorPosition: Int): Boolean {
            text.forEach { char -> onCharInput?.invoke(char) }
            return true
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            if (beforeLength > 0) {
                repeat(beforeLength) {
                    onSpecialKey?.invoke(KeyEvent.KEYCODE_DEL)
                }
            }
            return true
        }

        override fun sendKeyEvent(event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_DEL -> onSpecialKey?.invoke(KeyEvent.KEYCODE_DEL)
                    KeyEvent.KEYCODE_ENTER -> onSpecialKey?.invoke(KeyEvent.KEYCODE_ENTER)
                    KeyEvent.KEYCODE_TAB -> onSpecialKey?.invoke(KeyEvent.KEYCODE_TAB)
                    else -> {
                        // Jika ada unicode char, teruskan sebagai char input
                        val unicodeChar = event.unicodeChar
                        if (unicodeChar != 0) {
                            onCharInput?.invoke(unicodeChar.toChar())
                        }
                    }
                }
            }
            return true
        }

    }

    companion object {
        /** Mapping Android KeyEvent → HID keycode untuk special keys. */
        fun androidKeyToHid(androidKeyCode: Int): Byte? = when (androidKeyCode) {
            KeyEvent.KEYCODE_DEL -> 0x2A   // HID Backspace
            KeyEvent.KEYCODE_ENTER -> 0x28 // HID Enter
            KeyEvent.KEYCODE_TAB -> 0x2B   // HID Tab
            KeyEvent.KEYCODE_ESCAPE -> 0x29
            KeyEvent.KEYCODE_DPAD_RIGHT -> 0x4F
            KeyEvent.KEYCODE_DPAD_LEFT -> 0x50
            KeyEvent.KEYCODE_DPAD_DOWN -> 0x51
            KeyEvent.KEYCODE_DPAD_UP -> 0x52
            KeyEvent.KEYCODE_FORWARD_DEL -> 0x4C // HID Delete
            else -> null
        }
    }
}
