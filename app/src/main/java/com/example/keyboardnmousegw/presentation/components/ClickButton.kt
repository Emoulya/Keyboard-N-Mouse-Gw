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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboardnmousegw.presentation.theme.InterFontFamily
import com.example.keyboardnmousegw.presentation.theme.LeftClickGradientEnd
import com.example.keyboardnmousegw.presentation.theme.LeftClickGradientStart
import com.example.keyboardnmousegw.presentation.theme.LeftClickPressed
import com.example.keyboardnmousegw.presentation.theme.PrimaryViolet
import com.example.keyboardnmousegw.presentation.theme.RightClickBorder
import com.example.keyboardnmousegw.presentation.theme.RightClickPressed
import com.example.keyboardnmousegw.presentation.theme.RightClickSurface
import com.example.keyboardnmousegw.presentation.theme.TextMuted
import com.example.keyboardnmousegw.presentation.theme.TextWhite

private val buttonShape = RoundedCornerShape(18.dp)

/**
 * Tombol klik mouse dengan dua varian visual:
 * - Left Click: Gradient terang (primary action)
 * - Right Click: Surface gelap dengan border (secondary action)
 *
 * Menggunakan color shift animation saat ditekan.
 */
@Composable
fun ClickButton(
    text: String,
    icon: ImageVector,
    isLeftClick: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Color shift animation
    val animatedBgColor by animateColorAsState(
        targetValue = when {
            isLeftClick && isPressed -> LeftClickPressed
            isLeftClick -> Color.Transparent // Akan pakai gradient
            isPressed -> RightClickPressed
            else -> RightClickSurface
        },
        animationSpec = tween(durationMillis = if (isPressed) 50 else 200),
        label = "clickButtonBg"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isLeftClick -> Color.Transparent
            isPressed -> PrimaryViolet.copy(alpha = 0.6f)
            else -> RightClickBorder
        },
        animationSpec = tween(durationMillis = if (isPressed) 50 else 200),
        label = "clickButtonBorder"
    )

    val textColor = if (isLeftClick) TextWhite else TextMuted
    val iconTint = if (isLeftClick) TextWhite.copy(alpha = 0.85f) else TextMuted.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .clip(buttonShape)
            .then(
                if (isLeftClick && !isPressed) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            colors = listOf(LeftClickGradientStart, LeftClickGradientEnd)
                        ),
                        buttonShape
                    )
                } else {
                    Modifier.background(animatedBgColor, buttonShape)
                }
            )
            .border(
                width = if (isLeftClick) 0.dp else 1.dp,
                color = animatedBorderColor,
                shape = buttonShape
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
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
                color = textColor
            )
        }
    }
}
