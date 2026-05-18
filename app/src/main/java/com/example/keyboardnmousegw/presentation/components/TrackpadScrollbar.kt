package com.example.keyboardnmousegw.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TrackpadScrollbar(modifier: Modifier = Modifier) {
    // Column untuk menyusun panah atas, garis tengah, dan panah bawah
    Column(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Scroll Up Indicator",
            tint = Color.White
        )

        // Garis vertikal di tengah
        Box(
            modifier = Modifier
                .weight(1f)
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.5f))
                .padding(vertical = 8.dp)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Scroll Down Indicator",
            tint = Color.White
        )
    }
}