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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.keyboardnmousegw.presentation.theme.AccentLavender
import com.example.keyboardnmousegw.presentation.theme.TextMuted

@Composable
fun TrackpadScrollbar(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Scroll Up",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )

        // Garis vertikal gradient yang fade di ujung
        Box(
            modifier = Modifier
                .weight(1f)
                .width(1.5.dp)
                .padding(vertical = 4.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AccentLavender.copy(alpha = 0.4f),
                            AccentLavender.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Scroll Down",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}