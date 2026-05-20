package com.example.keyboardnmousegw.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboardnmousegw.presentation.theme.InterFontFamily
import com.example.keyboardnmousegw.presentation.theme.PrimaryViolet
import com.example.keyboardnmousegw.presentation.theme.PrimaryVioletDark

private val buttonShape = RoundedCornerShape(16.dp)

private val gradientBrush = Brush.horizontalGradient(
    colors = listOf(PrimaryViolet, PrimaryVioletDark)
)

@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .shadow(8.dp, buttonShape, ambientColor = PrimaryViolet.copy(alpha = 0.3f)),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
