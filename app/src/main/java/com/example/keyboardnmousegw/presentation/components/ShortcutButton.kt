package com.example.keyboardnmousegw.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboardnmousegw.presentation.theme.AccentLavender
import com.example.keyboardnmousegw.presentation.theme.InterFontFamily
import com.example.keyboardnmousegw.presentation.theme.PrimaryViolet
import com.example.keyboardnmousegw.presentation.theme.ShortcutBorder
import com.example.keyboardnmousegw.presentation.theme.ShortcutPressed
import com.example.keyboardnmousegw.presentation.theme.ShortcutSurface
import com.example.keyboardnmousegw.presentation.theme.TextMuted

private val chipShape = RoundedCornerShape(12.dp)

/**
 * Tombol shortcut bergaya chip dengan ikon + teks.
 * Menggunakan color shift animation saat ditekan:
 * background berubah lebih gelap dan border menyala violet.
 */
@Composable
fun ShortcutButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Color shift: background
    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) ShortcutPressed else ShortcutSurface,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 180),
        label = "shortcutBg"
    )

    // Color shift: border glow saat ditekan
    val animatedBorder by animateColorAsState(
        targetValue = if (isPressed) PrimaryViolet.copy(alpha = 0.5f) else ShortcutBorder,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 180),
        label = "shortcutBorder"
    )

    // Color shift: ikon menyala saat ditekan
    val animatedIconTint by animateColorAsState(
        targetValue = if (isPressed) AccentLavender else TextMuted.copy(alpha = 0.65f),
        animationSpec = tween(durationMillis = if (isPressed) 40 else 180),
        label = "shortcutIcon"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isPressed) AccentLavender else TextMuted,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 180),
        label = "shortcutText"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(chipShape)
            .background(animatedBg, chipShape)
            .border(
                width = 1.dp,
                color = animatedBorder,
                shape = chipShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = animatedIconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.3.sp,
                color = animatedTextColor,
                maxLines = 1
            )
        }
    }
}
