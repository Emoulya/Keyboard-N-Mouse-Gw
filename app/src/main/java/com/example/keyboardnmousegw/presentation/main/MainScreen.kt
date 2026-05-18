package com.example.keyboardnmousegw.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.keyboardnmousegw.R
import com.example.keyboardnmousegw.presentation.components.ActionButton
import com.example.keyboardnmousegw.presentation.components.TrackpadScrollbar

@Composable
fun MainScreen() {
    // Background utama aplikasi (Dark Blue)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F3D5E))
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
// 1. Area Trackpad (Mengambil porsi layar terbesar dengan weight 1f)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0B4C75))
        ) {
            // TODO: Ikon Mouse di Tengah
            // Untuk sementara kita gunakan teks jika Anda belum memiliki file drawable (ic_mouse.xml).
            // Nanti bisa diganti dengan: Icon(painterResource(id = R.drawable.ic_mouse), ...)
            Text(
                text = "🖱️",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.displayLarge
            )

            // Area Scrollbar di sisi kanan
            TrackpadScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // 2. Tombol Klik Kiri & Kanan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // weight(1f) membagi lebar menjadi persis 50:50
            ActionButton(text = "", modifier = Modifier.weight(1f)) { /* TODO: Left Click Event */ }
            ActionButton(text = "", modifier = Modifier.weight(1f)) { /* TODO: Right Click Event */ }
        }

        // 3. Tombol Shortcut (Undo, Redo, Copy, Paste)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(text = "Undo", modifier = Modifier.weight(1f)) { /* TODO: Undo Event */ }
            ActionButton(text = "Redo", modifier = Modifier.weight(1f)) { /* TODO: Redo Event */ }
            ActionButton(text = "Copy", modifier = Modifier.weight(1f)) { /* TODO: Copy Event */ }
            ActionButton(text = "Paste", modifier = Modifier.weight(1f)) { /* TODO: Paste Event */ }
        }
    }
}